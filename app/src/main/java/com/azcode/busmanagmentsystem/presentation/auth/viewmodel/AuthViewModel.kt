package com.azcode.busmanagmentsystem.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azcode.busmanagmentsystem.data.remote.Result
import com.azcode.busmanagmentsystem.data.remote.UserAuthRequest
import com.azcode.busmanagmentsystem.data.remote.UserAuthResponse
import com.azcode.busmanagmentsystem.data.remote.UserRegistrationRequest
import com.azcode.busmanagmentsystem.data.remote.UserRegistrationResponse
import com.azcode.busmanagmentsystem.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
open class AuthViewModel @Inject constructor(private val authRepo: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<Result<UserAuthResponse>>(Result.Idle)
    val loginState: StateFlow<Result<UserAuthResponse>> = _loginState

    private val _registerState = MutableStateFlow<Result<UserRegistrationResponse>>(Result.Idle)
    val registerState: MutableStateFlow<Result<UserRegistrationResponse>> = _registerState

    private val _isSignupLoading = MutableStateFlow(false)
    val isSignupLoading : StateFlow<Boolean> = _isSignupLoading

    private val _isSigninLoading = MutableStateFlow(false)
    val isSigninLoading : StateFlow<Boolean> = _isSigninLoading

    // credentials : can be phone num or an email
    fun loginUser(userAuthRequest: UserAuthRequest) =
        viewModelScope.launch(Dispatchers.IO) {
            _loginState.value = Result.Loading
            val result = authRepo.loginUser(userAuthRequest)
            _loginState.value = result
        }

    fun registerUser(userRegistrationRequest: UserRegistrationRequest) {
        _registerState.value = Result.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result =
                authRepo.registerUser(
                    userRegistrationRequest = userRegistrationRequest
                )
            // Add a delay to ensure loading shows for at least 1.5 seconds
            val startTime = System.currentTimeMillis()
            val minLoadingTime = 1500 // 1.5 seconds in milliseconds
            val elapsedTime = System.currentTimeMillis() - startTime

            if (elapsedTime < minLoadingTime) {
                kotlinx.coroutines.delay(minLoadingTime - elapsedTime)
            }
            _registerState.value = result

        }
    }

    fun updateIsSignUpLoading(isLoading: Boolean) {
        _isSignupLoading.value = isLoading
    }
    fun updateSignInLoading(isLoading: Boolean) {
        _isSignupLoading.value = isLoading
    }


}