package com.azcode.busmanagmentsystem.domain.model

import org.maplibre.geojson.Point

// Data class to represent a route with its path and stops
data class RouteData(
    val id: String,
    val name: String,
    val path: List<Point>,
    val stops: List<RouteStop>
)
