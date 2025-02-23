package com.azcode.busmanagmentsystem.di

import com.azcode.busmanagmentsystem.data.remote.BsbApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit() : Retrofit{
        return Retrofit.Builder()
            .baseUrl("http://192.168.188.222:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    @Provides
    @Singleton
    fun providesBsbApiService(retrofit: Retrofit) : BsbApiService {
        return retrofit.create(BsbApiService::class.java)
    }

//    @Provides
//    @Singleton
//    fun provideAuthRepository(bsbApiService: BsbApiService): AuthRepository{
//        return AuthRepository(bsbApiService)
//    }

}