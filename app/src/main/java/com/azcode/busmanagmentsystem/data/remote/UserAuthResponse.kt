package com.azcode.busmanagmentsystem.data.remote

import java.util.UUID

data class UserAuthResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val role: Role,
    val accessToken: String,
)
