package com.azcode.busmanagmentsystem.presentation.auth.state

import com.azcode.busmanagmentsystem.data.remote.Role
import com.azcode.busmanagmentsystem.data.remote.UserRegistrationRequest

data class RegistrationFormState(
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var phoneNumber: String? = "",
    var password: String = "",
    var passwordVisible: Boolean = false,
    var role: Role = Role.USER,
    var errors: Map<String, String> = mapOf()
)

fun RegistrationFormState.toUserRegistrationRequest(): UserRegistrationRequest {
    return UserRegistrationRequest(
        firstName = firstName,
        lastName = lastName,
        email = email,
        phoneNumber = phoneNumber,
        password = password,
        role = role
    )
}
