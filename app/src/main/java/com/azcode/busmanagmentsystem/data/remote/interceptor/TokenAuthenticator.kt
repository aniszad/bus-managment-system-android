package com.azcode.busmanagmentsystem.data.remote.interceptor

import android.util.Log
import com.azcode.busmanagmentsystem.data.local.SecuredPreferencesManager
import com.azcode.busmanagmentsystem.data.local.SessionManager
import com.azcode.busmanagmentsystem.data.remote.BsbApiService
import com.azcode.busmanagmentsystem.data.remote.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManager,
    private val securedPreferencesManager: SecuredPreferencesManager,
    private val apiService: BsbApiService
) : Authenticator {

    companion object {
        private const val MAX_RETRY_COUNT = 2
        private const val RETRY_HEADER = "X-Refresh-Retry"
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = securedPreferencesManager.getRefreshToken()

        if (refreshToken.isNullOrBlank()) {
            forceLogout()
            return null
        }

        // Extract retry count from the original request (default to 0)
        val retryCount = response.request.header(RETRY_HEADER)?.toIntOrNull() ?: 0

        // If max retry attempts have been reached, force logout
        if (retryCount >= MAX_RETRY_COUNT) {
            forceLogout()
            return null
        }

        return try {
            val refreshResponse = runBlocking { apiService.refreshToken(RefreshTokenRequest(refreshToken)) }

            if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                val newAccessToken = refreshResponse.body()!!.accessToken
                securedPreferencesManager.saveAccessToken(newAccessToken)

                // Retry the failed request with the new token and increment retry count
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .header(RETRY_HEADER, (retryCount + 1).toString()) // Increase retry count
                    .build()
            } else {
                forceLogout()
                null
            }
        } catch (e: Exception) {
            forceLogout()
            null
        }
    }

    private fun forceLogout() {
        sessionManager.signOutUser()
    }
}


