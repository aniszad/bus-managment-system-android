package com.azcode.busmanagmentsystem.presentation.auth.state

import com.azcode.busmanagmentsystem.data.remote.UserAuthRequest

data class SignInFormState (
    var credentials: String = "",
    var password: String = "",
    var passwordVisible: Boolean = false
)
fun SignInFormState.toUserAuthRequest() : UserAuthRequest{
    return UserAuthRequest(email = credentials, password = password)
}