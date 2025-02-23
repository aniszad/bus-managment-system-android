package com.azcode.busmanagmentsystem.data.remote

data class UserRegistrationRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String?,
    val password: String,
    val role: Role
)


