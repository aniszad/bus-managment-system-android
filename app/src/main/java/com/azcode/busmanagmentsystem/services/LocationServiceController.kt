package com.azcode.busmanagmentsystem.services

interface LocationServiceController {
    fun startTracking()
    fun stopTracking()
    fun checkPermissions(): Boolean
}