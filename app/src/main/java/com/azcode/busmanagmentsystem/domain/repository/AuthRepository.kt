package com.azcode.busmanagmentsystem.domain.repository

import android.util.Log
import com.azcode.busmanagmentsystem.data.remote.*
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

open class AuthRepository @Inject constructor(private var bsbApiService: BsbApiService) {

    suspend fun loginUser(userAuthRequest: UserAuthRequest): Result<UserAuthResponse> {
        return try {
            val response = bsbApiService.loginUser(userAuthRequest)

            if (response.isSuccessful) {
                Log.e("Registration-Response:","${response.body()}")
                response.body()?.let {
                    Result.Success(it)
                } ?: Result.Error("Couldn't log in: Empty response")
            } else {
                handleLoginError(response.code())
            }
        } catch (e: Exception) {
            Result.Error("${e.localizedMessage}")
        }
    }

    suspend fun registerUser(
        userRegistrationRequest: UserRegistrationRequest
    ): Result<UserRegistrationResponse> {
        return try {
            val response = bsbApiService.registerUser(
                userRegistrationRequest
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.Success(it)
                } ?: Result.Error("Couldn't register user: Empty response")
            } else {
                handleRegisterError(response.code())
            }
        } catch (e: HttpException) {
            Result.Error("Network error: ${e.message()}")
        } catch (e: IOException) {
            Result.Error("Check your internet connection")
        } catch (e: Exception) {
            Result.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

    // Handle login errors based on HTTP response codes
    private fun handleLoginError(code: Int): Result<UserAuthResponse> {
        return when (code) {
            400 -> Result.Error("Invalid credentials. Please check your email or password.")
            401 -> Result.Error("Unauthorized. Your session may have expired.")
            403 -> Result.Error("Access denied. Contact support.")
            500 -> Result.Error("Server error. Try again later.")
            else -> Result.Error("Login failed with error code: $code")
        }
    }

    // Handle registration errors based on HTTP response codes
    private fun handleRegisterError(code: Int): Result<UserRegistrationResponse> {
        return when (code) {
            400 -> Result.Error("Invalid registration data. Please check your inputs.")
            409 -> Result.Error("User already exists. Try logging in instead.")
            500 -> Result.Error("Server error. Try again later.")
            else -> Result.Error("Registration failed with error code: $code")
        }
    }
}
