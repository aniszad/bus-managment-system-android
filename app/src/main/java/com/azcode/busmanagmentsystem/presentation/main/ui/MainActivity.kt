package com.azcode.busmanagmentsystem.presentation.main.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.azcode.busmanagmentsystem.presentation.main.viewmodel.MainViewModel
import com.azcode.busmanagmentsystem.presentation.navigation.AppNavGraph
import com.azcode.busmanagmentsystem.services.LocationService
import com.azcode.busmanagmentsystem.services.LocationServiceController
import com.azcode.busmanagmentsystem.ui.theme.BusTrackingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Base location permissions needed on all supported Android versions
    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Service permission only needed on Android 9+ (API 28+)
    private val servicePermission =
        arrayOf(Manifest.permission.FOREGROUND_SERVICE)

    // Background permission only needed on Android 10+ (API 29+)
    private val backgroundLocationPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        } else {
            null
        }

    // Activity result launcher for location permissions
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startLocationService()
        } else {
            Toast.makeText(
                this,
                "Location permissions are required for tracking",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Activity result launcher for background location permission
    private val backgroundLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(
                this,
                "Background location permission granted",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "App will only track location when in foreground",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return locationPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun hasServicePermission(): Boolean {
        return servicePermission.isEmpty() || servicePermission.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun hasBackgroundLocationPermission(): Boolean {
        return backgroundLocationPermission == null ||
                ContextCompat.checkSelfPermission(this, backgroundLocationPermission) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        val permissionsToRequest = locationPermissions + servicePermission
        locationPermissionLauncher.launch(permissionsToRequest)
    }

    private fun requestBackgroundLocationPermission() {
        if (backgroundLocationPermission != null) {
            if (shouldShowRequestPermissionRationale(backgroundLocationPermission)) {
                AlertDialog.Builder(this)
                    .setTitle("Background Location Required")
                    .setMessage("This app needs background location access to track the bus position even when the app is closed.")
                    .setPositiveButton("Grant Permission") { _, _ ->
                        backgroundLocationPermissionLauncher.launch(backgroundLocationPermission)
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                        Toast.makeText(
                            this,
                            "Background location is needed for full functionality",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .create()
                    .show()
            } else {
                backgroundLocationPermissionLauncher.launch(backgroundLocationPermission)
            }
        }
    }

    fun startLocationService() {
        if (hasLocationPermissions() && hasServicePermission()) {
            val intent = Intent(this, LocationService::class.java).apply {
                action = LocationService.ACTION_START
                putExtra("BUS_NAME", "Bus 101")
            }
            startForegroundService(intent)

            Toast.makeText(this, "Location tracking started", Toast.LENGTH_SHORT).show()

            if (!hasBackgroundLocationPermission()) {
                requestBackgroundLocationPermission()
            }
        } else {
            requestLocationPermissions()
        }
    }

    fun stopLocationService() {
        val intent = Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
        }
        startService(intent)
        Toast.makeText(this, "Location tracking stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!hasLocationPermissions() || !hasServicePermission()) {
            requestLocationPermissions()
        }
        // Use the non-deprecated approach for window insets
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            val viewModel: MainViewModel = hiltViewModel()
            val signOutState by viewModel.signOutState.collectAsState()

            // Observe logout and navigate to login screen
            LaunchedEffect(signOutState) {
                if (signOutState) {
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true } // Clear back stack
                    }
                }
            }

            // Set up a way to share the location service controls with Compose screens
            val locationServiceController = remember {
                object : LocationServiceController {
                    override fun startTracking() = startLocationService()
                    override fun stopTracking() = stopLocationService()
                    override fun checkPermissions() = hasLocationPermissions() &&
                            hasServicePermission() &&
                            hasBackgroundLocationPermission()
                }
            }

            BusTrackingTheme {
                // Pass the location service controller to the AppNavGraph
                AppNavGraph(
                    navController = navController,
                    locationServiceController = locationServiceController
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BusTrackingTheme {
        Greeting("Android")
    }
}