package com.azcode.busmanagmentsystem.di

import com.azcode.busmanagmentsystem.data.local.SecuredPreferencesManager
import com.azcode.busmanagmentsystem.data.remote.BsbApiService
import com.azcode.busmanagmentsystem.data.remote.interceptor.AuthInterceptor
import com.azcode.busmanagmentsystem.data.remote.interceptor.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.234.250:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenAuthenticator: TokenAuthenticator,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .authenticator(tokenAuthenticator) // intercepts 401 unauthorized requests and refreshes the access token
            .addInterceptor(authInterceptor) // intercepts every api call and attaches accessToekn to the requests
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(securedPreferencesManager: SecuredPreferencesManager): AuthInterceptor {
        return AuthInterceptor(securedPreferencesManager)
    }

    @Provides
    @Singleton
    fun providesBsbApiService(retrofit: Retrofit): BsbApiService {
        return retrofit.create(BsbApiService::class.java)
    }

//    @Provides
//    @Singleton
//    fun provideAuthRepository(bsbApiService: BsbApiService): AuthRepository{
//        return AuthRepository(bsbApiService)
//    }

}