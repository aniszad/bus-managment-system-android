package com.azcode.busmanagmentsystem.di

import android.app.Application
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.azcode.busmanagmentsystem.data.local.SecuredPreferencesManager
import com.azcode.busmanagmentsystem.data.local.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun provideSecuredPreferencesManager(context : Context) : SecuredPreferencesManager{
        return SecuredPreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideSessionManager(securedPreferencesManager: SecuredPreferencesManager): SessionManager{
        return SessionManager(securedPreferencesManager)
    }

}