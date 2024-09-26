package com.vc.vcposprintservice.presentation.main

import androidx.lifecycle.ViewModel
import com.vc.vcposprintservice.presentation.common.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ShareViewModel : ViewModel(), Event<Navigation> {

    private val _state = MutableStateFlow<Navigation>(Navigation.PrintScreen)
    val state = _state.asStateFlow()

    override fun onEvent(event: Navigation) {
        when (event) {
            Navigation.PrintScreen -> performPrintScreen()
            Navigation.StartPrintService -> startService()
            Navigation.LoggerScreen -> performLoggerScreen()
        }
    }

    private fun performPrintScreen() {
        _state.value = Navigation.PrintScreen
    }

    private fun startService() {
        _state.value = Navigation.StartPrintService
    }

    private fun performLoggerScreen() {
        _state.value = Navigation.LoggerScreen
    }


}