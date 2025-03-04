package com.azcode.busmanagmentsystem.data.remote.mqtt

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class MqttHelper(private val brokerUrl: String, private val topic: String) {

    private val clientId = "AndroidClient_" + UUID.randomUUID().toString()
    private var mqttClient: MqttClient? = null
    private val TAG = "MqttHelper"
    var onConnected: (() -> Unit)? = null
    var onMessageReceived: ((String, Double, Double) -> Unit)? = null
    private val isConnecting = AtomicBoolean(false)

    private val options = MqttConnectOptions().apply {
        isAutomaticReconnect = true
        isCleanSession = true
        connectionTimeout = 30
        keepAliveInterval = 60
    }

    init {
        connectToMqttBroker()
    }

    fun connect() {
        if (isConnecting.getAndSet(true)) {
            Log.d(TAG, "Already attempting to connect, skipping duplicate request")
            return
        }

        connectToMqttBroker()
    }

    private fun connectToMqttBroker() {
        Thread {
            try {
                Log.d(TAG, "Connecting to MQTT broker: $brokerUrl")

                if (mqttClient == null) {
                    mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())
                }

                mqttClient?.setCallback(object : MqttCallbackExtended {
                    override fun connectComplete(reconnect: Boolean, serverURI: String) {
                        Log.d(TAG, "Connected to MQTT broker${if (reconnect) " (reconnected)" else ""}")
                        subscribeToTopic()
                        isConnecting.set(false)
                        // Notify on main thread
                        Handler(Looper.getMainLooper()).post {
                            onConnected?.invoke()
                        }
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Log.e(TAG, "Connection to MQTT broker lost", cause)
                        isConnecting.set(false)
                        // Try to reconnect after a delay
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (!isConnected()) {
                                connect()
                            }
                        }, 5000)
                    }

                    override fun messageArrived(topic: String, message: MqttMessage) {
                        processMessage(message)
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken) {
                        Log.d(TAG, "Message delivered successfully")
                    }
                })

                if (!mqttClient!!.isConnected) {
                    mqttClient?.connect(options)
                } else {
                    isConnecting.set(false)
                    Handler(Looper.getMainLooper()).post {
                        onConnected?.invoke()
                    }
                }
            } catch (e: MqttException) {
                Log.e(TAG, "Error connecting to MQTT broker", e)
                isConnecting.set(false)
                // Try to reconnect after a delay
                Handler(Looper.getMainLooper()).postDelayed({
                    connect()
                }, 5000)
            }
        }.start()
    }

    private fun processMessage(message: MqttMessage) {
        Thread {
            val payload = message.toString()
            Log.d(TAG, "Received MQTT Message: $payload")
            try {
                val json = payload.replace("[{}\"]".toRegex(), "").split(",")
                val busName = json.find { it.startsWith("busName:") }?.split(":")?.get(1)?.trim() ?: "Unknown"
                val lat = json.find { it.startsWith("lat:") }?.split(":")?.get(1)?.toDoubleOrNull()
                val lng = json.find { it.startsWith("lng:") }?.split(":")?.get(1)?.toDoubleOrNull()

                if (lat != null && lng != null) {
                    Handler(Looper.getMainLooper()).post {
                        onMessageReceived?.invoke(busName, lat, lng)
                    }
                } else {
                    Log.e(TAG, "Invalid message format")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing message: ${e.message}")
            }
        }.start()
    }

    fun isConnected(): Boolean = mqttClient?.isConnected ?: false

    fun publishMessage(busName: String, latitude: Double, longitude: Double) {
        Thread {
            try {
                if (!isConnected()) {
                    Log.d(TAG, "MQTT client not connected, attempting to reconnect...")
                    connect()
                    // Don't publish now, wait for reconnection
                    return@Thread
                }

                val payload = """{"busName":"$busName","lat":$latitude,"lng":$longitude,"timestamp":${System.currentTimeMillis()}}"""
                val message = MqttMessage(payload.toByteArray(StandardCharsets.UTF_8)).apply {
                    qos = 1
                    isRetained = false
                }

                mqttClient?.publish(topic, message)
                Log.d(TAG, "Published location: $payload")
            } catch (e: MqttException) {
                Log.e(TAG, "Error publishing message", e)
                // Try to reconnect
                connect()
            }
        }.start()
    }

    private fun subscribeToTopic() {
        try {
            mqttClient?.subscribe(topic, 1)
            Log.d(TAG, "Subscribed to topic: $topic")
        } catch (e: MqttException) {
            Log.e(TAG, "Error subscribing to topic", e)
        }
    }

    fun closeConnection() {
        try {
            mqttClient?.disconnect()
            mqttClient?.close()
            Log.d(TAG, "MQTT connection closed")
        } catch (e: MqttException) {
            Log.e(TAG, "Error closing MQTT connection", e)
        }
    }
}