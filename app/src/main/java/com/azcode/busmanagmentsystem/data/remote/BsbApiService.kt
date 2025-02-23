package com.azcode.busmanagmentsystem.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BsbApiService {

    @POST("/api/user/create")
    suspend fun registerUser(@Body userRegistrationRequest : UserRegistrationRequest) : Response<UserRegistrationResponse>

    @POST("api/auth/login")
    suspend fun loginUser(@Body userAuthRequest: UserAuthRequest): Response<UserAuthResponse>
}