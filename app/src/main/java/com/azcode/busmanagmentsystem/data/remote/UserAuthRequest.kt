package com.azcode.busmanagmentsystem.data.remote

data class UserAuthRequest (
    val credentials: String,
    val password: String
)