package com.azcode.busmanagmentsystem.presentation.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.azcode.busmanagmentsystem.R
import com.azcode.busmanagmentsystem.domain.model.RouteData
import com.azcode.busmanagmentsystem.domain.model.RouteStop
import com.azcode.busmanagmentsystem.presentation.home.viewmodel.BusTrackingViewModel
import com.azcode.busmanagmentsystem.services.LocationServiceController
import com.azcode.busmanagmentsystem.ui.theme.BusTrackingTheme
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point



@Composable
fun HomeScreen(
    busTrackingViewModel: BusTrackingViewModel = hiltViewModel(),
    locationServiceController: LocationServiceController
) {
    val busesPositions by busTrackingViewModel.busesLocations.collectAsState()
    val context = LocalContext.current
    val TAG = "HomeScreen_Tracking"

    // State to hold the dynamically loaded routes
    var routes by remember { mutableStateOf<List<RouteData>>(emptyList()) }

    // Register BroadcastReceiver properly
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.e(TAG, "onReceive: ${intent?.getStringExtra("busName")} - ${intent?.getDoubleExtra("latitude", 0.0)} - ${intent?.getDoubleExtra("longitude", 0.0)}")
                val busName = intent?.getStringExtra("busName") ?: "unknown bus"
                val latitude = intent?.getDoubleExtra("latitude", 0.0) ?: 0.0
                val longitude = intent?.getDoubleExtra("longitude", 0.0) ?: 0.0
                busTrackingViewModel.updateLocation(busName, latitude, longitude)
            }
        }

        val intentFilter = IntentFilter("com.azcode.busmanagmentsystem.LOCATION_UPDATE")
        // handling this line of code depending if sdk is lower then 33 or 33 and above
        ContextCompat.registerReceiver(
            context,
            receiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Start location tracking only when permissions are granted
    LaunchedEffect(Unit) {
        if (!locationServiceController.checkPermissions()) {
            // This will call requestLocationPermissions indirectly
            Log.e(TAG, "Location permissions not granted")
            locationServiceController.stopTracking()
        } else {
            Log.d(TAG, "Starting location tracking")
            locationServiceController.startTracking()
        }
    }

    BusTrackingTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            // Map view takes most of the screen
            Box(modifier = Modifier.weight(1f)) {
                MapView(
                    routes = routes,
                    busesPositions = busesPositions,
                    onRoutesLoaded = { loadedRoutes ->
                        routes = loadedRoutes
                    }
                )
            }
        }
    }
}

@Composable
fun MapView(
    routes: List<RouteData> = emptyList(),
    busesPositions: Map<String, Point> = emptyMap(),
    onRoutesLoaded: (List<RouteData>) -> Unit
) {
    val mapView = remember { mutableStateOf<org.maplibre.android.maps.MapView?>(null) }
    val mapInstance = remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }
    val context = LocalContext.current

    // Keep track of which bus icons we've already created
    val createdBusIcons = remember { mutableStateOf(setOf<String>()) }

    // Define route colors - will cycle through these for multiple routes
    val routeColors = remember {
        listOf(
            "#FF0000", // Red
            "#0000FF", // Blue
            "#00AA00", // Green
            "#AA00AA", // Purple
            "#FF6600", // Orange
            "#00AAAA"  // Teal
        )
    }

    val createBusMarker = { busName: String ->
        // Load the marker image from resources - store it in a variable
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_bus_marker)

        // Check if bitmap was loaded successfully
        if (originalBitmap != null) {
            val desiredSize = 60 // Set your desired size
            // Create a scaled version
            Bitmap.createScaledBitmap(originalBitmap, desiredSize, desiredSize, true)
        } else {
            // Fallback to a simple drawn marker if image loading fails
            val markerSize = 60
            val bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                color = Color.RED
                isAntiAlias = true
            }
            canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 2f - 4, paint)
            bitmap
        }

    }

    // Update bus positions on the map
    LaunchedEffect(busesPositions) {
        val map = mapInstance.value ?: return@LaunchedEffect
        val style = map.style ?: return@LaunchedEffect

        // Check and add any new bus icons that haven't been created yet
        busesPositions.keys.forEach { busName ->
            val iconId = "bus-marker-$busName"
            if (!createdBusIcons.value.contains(busName)) {
                style.addImage(iconId, createBusMarker(busName))
                createdBusIcons.value = createdBusIcons.value + busName
            }
        }

        // Create features for all buses
        val busFeatures = busesPositions.map { (busName, point) ->
            Feature.fromGeometry(point).apply {
                addStringProperty("name", busName)
                addStringProperty("icon_id", "bus-marker-$busName")
            }
        }.toTypedArray()

        val busesCollection = FeatureCollection.fromFeatures(busFeatures)

        // Update or create the source for buses
        if (style.getSource("buses-source") != null) {
            (style.getSource("buses-source") as? GeoJsonSource)?.setGeoJson(busesCollection)
        } else {
            // Add source if it doesn't exist
            val busesSource = GeoJsonSource("buses-source", busesCollection)
            style.addSource(busesSource)

            // Add buses layer
            val busesLayer = SymbolLayer("buses-layer", "buses-source").apply {
                setProperties(
                    PropertyFactory.iconImage("{icon_id}"),
                    PropertyFactory.iconSize(1.0f),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.textField("{name}"),
                    PropertyFactory.textSize(12f),
                    PropertyFactory.textOffset(arrayOf(0f, 1.5f)),
                    PropertyFactory.textColor(Color.BLACK),
                    PropertyFactory.textHaloColor(Color.WHITE),
                    PropertyFactory.textHaloWidth(1f)
                )
            }
            style.addLayer(busesLayer)
        }
    }

    AndroidView(
        factory = { context ->
            MapLibre.getInstance(context)
            val view = org.maplibre.android.maps.MapView(context)
            view.onCreate(null)
            view.getMapAsync { map ->
                mapInstance.value = map

                map.setStyle(
                    Style.Builder()
                        .fromUri("https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json")
                ) { style ->
                    // Collection to store all routes for bounds calculation
                    val allRoutePoints = mutableListOf<Point>()
                    val allStopPoints = mutableListOf<Point>()

                    // If we have routes passed in, display those
                    if (routes.isNotEmpty()) {
                        displayRoutes(routes, style, routeColors, allRoutePoints, allStopPoints)
                    } else {
                        // Otherwise load routes from assets
                        loadRoutesFromAssets(context, style, routeColors, allRoutePoints, allStopPoints, onRoutesLoaded)
                    }

                    // Create stops marker icon
                    val markerSize = 40
                    val bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    val paint = Paint().apply {
                        color = Color.BLUE
                        isAntiAlias = true
                    }

                    // Draw a simple circle with border
                    canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 2f - 2, paint)

                    // Add white border
                    paint.color = Color.WHITE
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 4f
                    canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 2f - 4, paint)

                    // Add the bitmap to the style
                    style.addImage("bus-stop-marker", bitmap)

                    // If we have stops, add them as a layer
                    if (allStopPoints.isNotEmpty()) {
                        // Create features for all stops
                        val stopFeatures = allStopPoints.map { point ->
                            Feature.fromGeometry(point)
                        }.toTypedArray()

                        val stopsCollection = FeatureCollection.fromFeatures(stopFeatures)
                        val stopsSource = GeoJsonSource("stops-source", stopsCollection.toJson())
                        style.addSource(stopsSource)

                        // Add stops layer
                        val stopsLayer = SymbolLayer("stops-layer", "stops-source").apply {
                            setProperties(
                                PropertyFactory.iconImage("bus-stop-marker"),
                                PropertyFactory.iconSize(1.0f),
                                PropertyFactory.iconAllowOverlap(true)
                            )
                        }
                        style.addLayer(stopsLayer)
                    }

                    // Create initial bus markers for each bus
                    busesPositions.forEach { (busName, _) ->
                        val iconId = "bus-marker-$busName"
                        style.addImage(iconId, createBusMarker(busName))
                        createdBusIcons.value = createdBusIcons.value + busName
                    }

                    // Create features for all buses
                    val busFeatures = busesPositions.map { (busName, point) ->
                        Feature.fromGeometry(point).apply {
                            addStringProperty("name", busName)
                            addStringProperty("icon_id", "bus-marker-$busName")
                        }
                    }.toTypedArray()

                    val busesCollection = FeatureCollection.fromFeatures(busFeatures)
                    val busesSource = GeoJsonSource("buses-source", busesCollection)
                    style.addSource(busesSource)

                    // Add buses layer
                    val busesLayer = SymbolLayer("buses-layer", "buses-source").apply {
                        setProperties(
                            PropertyFactory.iconImage("{icon_id}"),
                            PropertyFactory.iconSize(1.0f),
                            PropertyFactory.iconAllowOverlap(true),
                            PropertyFactory.textField("{name}"),
                            PropertyFactory.textSize(12f),
                            PropertyFactory.textOffset(arrayOf(0f, 1.5f)),
                            PropertyFactory.textColor(Color.BLACK),
                            PropertyFactory.textHaloColor(Color.WHITE),
                            PropertyFactory.textHaloWidth(1f)
                        )
                    }
                    style.addLayer(busesLayer)

                    // Include bus positions in bounds calculation if available
                    if (busesPositions.isNotEmpty()) {
                        busesPositions.values.forEach { point ->
                            allRoutePoints.add(point)
                        }
                    }

                    // If we have points, set the camera to show all of them
                    if (allRoutePoints.isNotEmpty()) {
                        val bounds = LatLngBounds.Builder()
                        allRoutePoints.forEach { point ->
                            bounds.include(LatLng(point.latitude(), point.longitude()))
                        }

                        // Set camera to show all routes with padding
                        map.easeCamera(
                            CameraUpdateFactory.newLatLngBounds(bounds.build(), 50),
                            1000
                        )
                    }

                    // Enable user interaction
                    map.uiSettings.isRotateGesturesEnabled = true
                    map.uiSettings.isTiltGesturesEnabled = true
                    map.uiSettings.isZoomGesturesEnabled = true
                    map.uiSettings.isScrollGesturesEnabled = true
                }
            }
            mapView.value = view
            view.onStart()
            view.onResume()
            view
        },
        modifier = Modifier.fillMaxSize()
    )

    // Handle the cleanup of map resources
    DisposableEffect(Unit) {
        onDispose {
            mapView.value?.onPause()
            mapView.value?.onStop()
            mapView.value?.onDestroy()
        }
    }
}

// Function to display the routes passed in directly
private fun displayRoutes(
    routes: List<RouteData>,
    style: Style,
    routeColors: List<String>,
    allRoutePoints: MutableList<Point>,
    allStopPoints: MutableList<Point>
) {
    // Add each route as a separate source and layer
    routes.forEachIndexed { index, routeData ->
        // Skip empty routes
        if (routeData.path.isEmpty()) return@forEachIndexed

        // Create route feature
        val lineString = LineString.fromLngLats(routeData.path)
        val routeFeature = Feature.fromGeometry(lineString)
        val routeCollection = FeatureCollection.fromFeatures(arrayOf(routeFeature))

        // Add source
        val sourceId = "route-source-${routeData.id}"
        val routeSource = GeoJsonSource(sourceId, routeCollection.toJson())
        style.addSource(routeSource)

        // Add layer with a color from our palette (cycling through colors)
        val colorHex = routeColors[index % routeColors.size]
        val routeLayer = LineLayer("route-layer-${routeData.id}", sourceId).apply {
            setProperties(
                PropertyFactory.lineColor(Color.parseColor(colorHex)),
                PropertyFactory.lineWidth(5f),
                PropertyFactory.lineOpacity(0.8f)
            )
        }
        style.addLayer(routeLayer)

        // Add all points to the bounds collection
        allRoutePoints.addAll(routeData.path)

        // Add stops to the collection
        allStopPoints.addAll(routeData.stops.map { it.location })
    }
}

// Function to load routes from assets folder
private fun loadRoutesFromAssets(
    context: android.content.Context,
    style: Style,
    routeColors: List<String>,
    allRoutePoints: MutableList<Point>,
    allStopPoints: MutableList<Point>,
    onRoutesLoaded: (List<RouteData>) -> Unit
) {
    val loadedRoutes = mutableListOf<RouteData>()

    try {
        context.assets.list("")?.filter { it.startsWith("route") && it.endsWith(".geojson") }?.forEach { fileName ->
            val geoJsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val featureCollection = FeatureCollection.fromJson(geoJsonString)

            // Extract route ID and name from filename or properties
            val routeId = fileName.substringBefore(".geojson")
            var routeName = routeId

            // Extract route coordinates
            val routeFeature = featureCollection.features()?.find {
                it.geometry() is LineString
            }

            val routePath = mutableListOf<Point>()

            if (routeFeature != null) {
                val lineString = routeFeature.geometry() as LineString
                routePath.addAll(lineString.coordinates())

                // Try to get route name from properties
                if (routeFeature.hasProperty("name")) {
                    routeName = routeFeature.getStringProperty("name")
                }

                // Add all coordinates to the bounds collection
                allRoutePoints.addAll(routePath)
            }

            // Extract stops
            val stops = mutableListOf<RouteStop>()
            featureCollection.features()?.filter {
                it.geometry() is Point
            }?.forEach { stopFeature ->
                val stopPoint = stopFeature.geometry() as Point
                val stopId = stopFeature.id() ?: "stop-${stops.size}"
                val stopName = if (stopFeature.hasProperty("name")) {
                    stopFeature.getStringProperty("name")
                } else {
                    "Stop ${stops.size + 1}"
                }

                stops.add(RouteStop(id = stopId, name = stopName, location = stopPoint))
                allStopPoints.add(stopPoint)
            }

            // Add the route to our collection
            loadedRoutes.add(RouteData(routeId, routeName, routePath, stops))

            // Add the route to the map
            if (routePath.isNotEmpty()) {
                val lineString = LineString.fromLngLats(routePath)
                val feature = Feature.fromGeometry(lineString)
                val routeCollection = FeatureCollection.fromFeatures(arrayOf(feature))

                // Add source
                val sourceId = "route-source-$routeId"
                val routeSource = GeoJsonSource(sourceId, routeCollection.toJson())
                style.addSource(routeSource)

                // Add layer with a color from our palette
                val colorHex = routeColors[loadedRoutes.size % routeColors.size]
                val routeLayer = LineLayer("route-layer-$routeId", sourceId).apply {
                    setProperties(
                        PropertyFactory.lineColor(Color.parseColor(colorHex)),
                        PropertyFactory.lineWidth(5f),
                        PropertyFactory.lineOpacity(0.8f)
                    )
                }
                style.addLayer(routeLayer)
            }
        }
    } catch (e: Exception) {
        Log.e("MapView", "Error loading GeoJSON files", e)
    }

    // Notify callback with all loaded routes
    onRoutesLoaded(loadedRoutes)
}

// Helper function to parse GeoJSON string to RouteData
fun parseGeoJsonToRouteData(routeId: String, geoJsonString: String): RouteData {
    val featureCollection = FeatureCollection.fromJson(geoJsonString)
    val routePath = mutableListOf<Point>()
    val stops = mutableListOf<RouteStop>()

    var routeName = routeId

    // Extract route coordinates
    featureCollection.features()?.find {
        it.geometry() is LineString
    }?.let { routeFeature ->
        val lineString = routeFeature.geometry() as LineString
        routePath.addAll(lineString.coordinates())

        // Try to get route name from properties
        if (routeFeature.hasProperty("name")) {
            routeName = routeFeature.getStringProperty("name")
        }
    }

    // Extract stops
    featureCollection.features()?.filter {
        it.geometry() is Point
    }?.forEach { stopFeature ->
        val stopPoint = stopFeature.geometry() as Point
        val stopId = stopFeature.id() ?: "stop-${stops.size}"
        val stopName = if (stopFeature.hasProperty("name")) {
            stopFeature.getStringProperty("name")
        } else {
            "Stop ${stops.size + 1}"
        }

        stops.add(RouteStop(id = stopId, name = stopName, location = stopPoint))
    }

    return RouteData(routeId, routeName, routePath, stops)
}

// Extension function to check if a feature has a property
fun Feature.hasProperty(property: String): Boolean {
    return try {
        properties()?.get(property) != null
    } catch (e: Exception) {
        false
    }
}

// Extension function to get a string property
fun Feature.getStringProperty(property: String): String {
    return try {
        properties()?.get(property)?.asString ?: ""
    } catch (e: Exception) {
        ""
    }
}

// Extension to add a string property to a feature
fun Feature.addStringProperty(key: String, value: String): Feature {
    properties()?.addProperty(key, value)
    return this
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    //HomeScreen(locationServiceController = locationServiceController)
}