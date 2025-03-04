package com.azcode.busmanagmentsystem.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BsbApiService {

    @POST("/api/user/create")
    suspend fun signUp(@Body userRegistrationRequest : UserRegistrationRequest) : Response<UserRegistrationResponse>

    @POST("api/auth/login")
    suspend fun signIn(@Body userAuthRequest: UserAuthRequest): Response<UserAuthResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body refreshTokenRequest: RefreshTokenRequest): Response<RefreshTokenResponse>
}