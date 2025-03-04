package com.azcode.busmanagmentsystem.domain.model

import org.maplibre.geojson.Point

// Data class to represent a bus stop
data class RouteStop(
    val id: String,
    val name: String,
    val location: Point
)