package com.azcode.busmanagmentsystem.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.maplibre.geojson.Point

class BusTrackingViewModel : ViewModel() {
    // Route coordinates from your GeoJSON
    private lateinit var routeCoordinates: List<Point>

    // Current position of the bus along the route
    private val _busesLocations = MutableStateFlow<Map<String, Point>>(mutableMapOf())
    val busesLocations: StateFlow<Map<String, Point>> = _busesLocations

    // Simulation status
    private val _isSimulationActive = MutableStateFlow(false)
    val isSimulationActive: StateFlow<Boolean> = _isSimulationActive

    fun updateLocation(busName: String, latitude: Double, longitude: Double) {
        // Create a new map with the updated bus location
        val updatedLocations = _busesLocations.value.toMutableMap()
        updatedLocations[busName] = Point.fromLngLat(longitude, latitude)

        // Update the StateFlow with the new map
        _busesLocations.value = updatedLocations
    }
}