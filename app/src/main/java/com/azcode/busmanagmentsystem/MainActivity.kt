package com.azcode.busmanagmentsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsControllerCompat
import com.azcode.busmanagmentsystem.presentation.auth.ui.AuthScreen
import com.azcode.busmanagmentsystem.ui.theme.BusTrackingTheme
import dagger.hilt.android.AndroidEntryPoint
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        controller.isAppearanceLightStatusBars = false
        setContent {
            AuthScreen()
//            BusTrackingTheme {
//                // A surface container using the 'background' color from the theme
//                        Surface(
//                            modifier = Modifier.fillMaxSize(),
//                            color = MaterialTheme.colorScheme.background
//                        ) {
//                            MapView()
//                        }
//            }
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
@Composable
fun MapView() {
    AndroidView(
        factory = { context ->
            MapLibre.getInstance(context)

            val mapView = org.maplibre.android.maps.MapView(context)
            mapView.onCreate(null)
            mapView.getMapAsync { map ->
                map.setStyle(Style.Builder().fromJson("{}")) { style ->

                    // add OpenStreetMap source
                    val rasterSource = RasterSource(
                        "osm-source",
                        TileSet("2.0", "https://tile.openstreetmap.org/{z}/{x}/{y}.png")
                    )
                    style.addSource(rasterSource)

                    // Adding a raster lyaer
                    val rasterLayer = RasterLayer("osm-layer", "osm-source")
                    style.addLayer(rasterLayer)

                    // adjust the ui
                    map.uiSettings.setAttributionMargins(15, 0, 0, 15)

                    // setting the camera up
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(36.7508896, 5.0567333)) // Your location
                        .zoom(10.0)
                        .bearing(2.0)
                        .build()
                }
            }
            mapView
        }
    )
}

