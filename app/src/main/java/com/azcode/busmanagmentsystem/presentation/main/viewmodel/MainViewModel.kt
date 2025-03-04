package com.azcode.busmanagmentsystem.presentation.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azcode.busmanagmentsystem.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    sessionManager: SessionManager
) : ViewModel() {

    private val _signOutState = MutableStateFlow(false)
    val signOutState = _signOutState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.signOutFlow.collect {
                _signOutState.value = true // Notify UI to navigate to login
            }
        }
    }
}
