package com.azcode.busmanagmentsystem.presentation.auth.state

import com.azcode.busmanagmentsystem.data.remote.Role

data class RegistrationFormState (
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String? = "",
    val password: String = "",
    val role: Role = Role.USER,
    val errors : Map<String, String> = mapOf()
)