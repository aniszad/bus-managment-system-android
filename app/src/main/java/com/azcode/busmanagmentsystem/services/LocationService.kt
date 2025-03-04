package com.azcode.busmanagmentsystem.services

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.azcode.busmanagmentsystem.data.remote.mqtt.MqttHelper
import com.google.android.gms.location.*
import java.util.concurrent.atomic.AtomicBoolean

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var mqttHelper: MqttHelper
    private var busName: String? = null
    private val isRequestingLocation = AtomicBoolean(false)
    private val handler = Handler(Looper.getMainLooper())
    private var locationUpdateCounter = 0

    companion object {
        private const val TAG = "LocationService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "tracking_channel"
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        private const val LOCATION_REQUEST_RETRY_DELAY = 5000L
        private const val HEALTH_CHECK_INTERVAL = 30000L // 30 seconds
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Starting service")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        initializeLocationAndMqtt()
        scheduleHealthCheck()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Received intent with action: ${intent?.action}")
        busName = intent?.getStringExtra("BUS_NAME")
        Log.d(TAG, "Bus name set to: $busName")

        when (intent?.action) {
            ACTION_STOP -> stopSelf()
            ACTION_START -> {
                // Force a location update immediately to verify everything works
                getLastKnownLocation()
            }
        }
        return START_STICKY
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted for getLastKnownLocation")
            return
        }

        Log.d(TAG, "Requesting last known location")
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Log.d(TAG, "Last known location: ${location.latitude}, ${location.longitude}")
                    if (mqttHelper.isConnected()) {
                        mqttHelper.publishMessage(busName ?: "unknown bus", location.latitude, location.longitude)
                        Log.d(TAG, "Published last known location")
                    } else {
                        Log.e(TAG, "MQTT not connected when trying to publish last known location")
                    }
                } else {
                    Log.d(TAG, "Last known location is null, waiting for location updates")
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Failed to get last known location: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getLastKnownLocation: ${e.message}")
        }
    }

    private fun scheduleHealthCheck() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                performHealthCheck()
                handler.postDelayed(this, HEALTH_CHECK_INTERVAL)
            }
        }, HEALTH_CHECK_INTERVAL)
    }

    private fun performHealthCheck() {
        Log.d(TAG, "Health check - Location updates received: $locationUpdateCounter")
        Log.d(TAG, "Health check - MQTT connected: ${mqttHelper.isConnected()}")

        if (locationUpdateCounter == 0) {
            Log.w(TAG, "No location updates received, restarting location updates")
            stopLocationUpdates()
            requestLocationUpdates()
        }

        if (!mqttHelper.isConnected()) {
            Log.w(TAG, "MQTT not connected, attempting to reconnect")
            mqttHelper.connect()
        }
    }

    private fun initializeLocationAndMqtt() {
        Log.d(TAG, "Initializing location and MQTT")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mqttHelper = MqttHelper(
            brokerUrl = "tcp://192.168.234.250:1883",
            topic = "tracking/coordinates"
        )

        mqttHelper.onConnected = {
            Log.d(TAG, "MQTT Connected! Now requesting location updates...")
            requestLocationUpdates()
        }

        mqttHelper.onMessageReceived = { busName, latitude, longitude ->
            Log.d(TAG, "Received location: $busName: $latitude, $longitude")
            sendLocationBroadcast(busName, latitude, longitude)
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationUpdateCounter++
                val locations = locationResult.locations
                Log.d(TAG, "onLocationResult: Received ${locations.size} locations, total updates: $locationUpdateCounter")

                locationResult.lastLocation?.let { location ->
                    Log.d(TAG, "Location update: ${location.latitude}, ${location.longitude}, accuracy: ${location.accuracy}m")

                    if (mqttHelper.isConnected()) {
                        mqttHelper.publishMessage(busName ?: "unknown bus", location.latitude, location.longitude)
                        Log.d(TAG, "Location Update Published: ${location.latitude}, ${location.longitude}")
                    } else {
                        Log.e(TAG, "MQTT Not Connected - Skipping Location Publish")
                        // Try to reconnect MQTT
                        mqttHelper.connect()
                    }
                } ?: Log.e(TAG, "Last location is null in location result")
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                val isLocationAvailable = locationAvailability.isLocationAvailable
                Log.d(TAG, "onLocationAvailability: Location available = $isLocationAvailable")

                if (!isLocationAvailable) {
                    Log.w(TAG, "Location is not available, will retry location request")
                    retryLocationRequest()
                }
            }
        }
    }

    private fun stopLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Log.d(TAG, "Location updates stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping location updates: ${e.message}")
        }
    }

    private fun requestLocationUpdates() {
        if (isRequestingLocation.getAndSet(true)) {
            Log.d(TAG, "Already requesting location updates, skipping")
            return
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted, will retry later")
            retryLocationRequest()
            return
        }

        val locationRequest = LocationRequest.Builder(2000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(2000)
            .setMaxUpdateDelayMillis(10000)
            .setWaitForAccurateLocation(false)
            .build()

        try {
            Log.d(TAG, "Requesting location updates with interval: 5000ms")
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Location updates requested successfully")
            isRequestingLocation.set(false)
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception requesting location updates: ${e.message}")
            retryLocationRequest()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error requesting location updates: ${e.message}")
            retryLocationRequest()
        }
    }

    private fun retryLocationRequest() {
        isRequestingLocation.set(false)
        Log.d(TAG, "Will retry location request in ${LOCATION_REQUEST_RETRY_DELAY}ms")
        handler.postDelayed({
            Log.d(TAG, "Retrying location request now")
            if (mqttHelper.isConnected()) {
                requestLocationUpdates()
            } else {
                Log.d(TAG, "MQTT not connected during retry, connecting first")
                mqttHelper.connect()
            }
        }, LOCATION_REQUEST_RETRY_DELAY)
    }

    private fun sendLocationBroadcast(busName: String, latitude: Double, longitude: Double) {
        val intent = Intent("com.azcode.busmanagmentsystem.LOCATION_UPDATE").apply {
            putExtra("busName", busName)
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
        }
        sendBroadcast(intent)
        Log.d(TAG, "Broadcast sent for $busName location")
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Tracking Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking Active")
            .setContentText("Your location is being shared")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Service being destroyed")
        handler.removeCallbacksAndMessages(null)
        stopLocationUpdates()
        mqttHelper.closeConnection()
        Log.d(TAG, "Location service destroyed")
    }
}