package com.azcode.busmanagmentsystem.data.remote.interceptor

import com.azcode.busmanagmentsystem.data.local.SecuredPreferencesManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val securedPreferencesManager: SecuredPreferencesManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = securedPreferencesManager.getAccessToken()

        val request = chain.request().newBuilder()
            .apply {
                if (!accessToken.isNullOrBlank()) {
                    header("Authorization", "Bearer $accessToken")
                }
            }
            .build()

        return chain.proceed(request)
    }
}