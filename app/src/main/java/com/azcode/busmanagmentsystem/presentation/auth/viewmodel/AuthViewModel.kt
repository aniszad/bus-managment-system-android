package com.azcode.busmanagmentsystem.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azcode.busmanagmentsystem.data.remote.Result
import com.azcode.busmanagmentsystem.data.remote.Role
import com.azcode.busmanagmentsystem.data.remote.UserAuthResponse
import com.azcode.busmanagmentsystem.data.remote.UserRegistrationResponse
import com.azcode.busmanagmentsystem.domain.repository.AuthRepository
import com.azcode.busmanagmentsystem.presentation.auth.state.RegistrationFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
open class AuthViewModel @Inject constructor(private val authRepo: AuthRepository) : ViewModel() {
    private val _loginState = MutableStateFlow<Result<UserAuthResponse>>(Result.Idle)
    val loginState: StateFlow<Result<UserAuthResponse>> = _loginState

    private val _registerState = MutableSharedFlow<Result<UserRegistrationResponse>>(replay = 0)
    val registerState: SharedFlow<Result<UserRegistrationResponse>> = _registerState.asSharedFlow()

    private val _userRegistrationData =
        MutableStateFlow(RegistrationFormState())
    val userRegistrationData: StateFlow<RegistrationFormState> = _userRegistrationData


    // credentials : can be phone num or an email
    fun loginUser(credentials: String, password: String) =
        viewModelScope.launch(Dispatchers.IO) {
            _loginState.value = Result.Loading
            val result = authRepo.loginUser(credentials, password)
        }

    fun registerUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String,
        role: Role
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            _registerState.emit(Result.Loading)
            _registerState.emit(
                authRepo.registerUser(
                    firstName,
                    lastName,
                    email,
                    password,
                    phoneNumber,
                    role
                )
            )
        }

    fun onPasswordChange(password: String) {
        _userRegistrationData.update { it.copy(
            password = password
        ) }
    }

    fun onPhoneNumberChange(phoneNumber: String) {

    }

    fun onEmailChange(email: String) {

    }

    fun onFirstNameChange(firstName: String) {

    }

    fun onLastNameChange(lastName: String) {

    }
}