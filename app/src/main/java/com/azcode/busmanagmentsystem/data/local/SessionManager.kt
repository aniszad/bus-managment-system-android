package com.azcode.busmanagmentsystem.data.local

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class SessionManager @Inject constructor(
    private val securedPreferencesManager: SecuredPreferencesManager
) {
    private val _signOutFlow = MutableSharedFlow<Unit>(replay = 1)
    val signOutFlow = _signOutFlow.asSharedFlow()

    fun signOutUser() {
        securedPreferencesManager.clearTokens()
        _signOutFlow.tryEmit(Unit)
    }
}
