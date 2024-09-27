package com.vc.vcposprintservice.presentation.main

import androidx.lifecycle.ViewModel
import com.vc.vcposprintservice.presentation.common.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor() : ViewModel(), Event<Navigation> {

    private val _state = MutableStateFlow<Navigation>(Navigation.PrintScreen)
    val state = _state.asStateFlow()

    override fun onEvent(event: Navigation) {
        when (event) {
            Navigation.PrintScreen -> performPrintScreen()
            Navigation.StartPrintService -> startService()
            Navigation.LoggerScreen -> performLoggerScreen()
            Navigation.PopBackStack -> popBackStack()
            Navigation.Default -> {
                _state.value = Navigation.Default
            }

            Navigation.StopPrintService -> stopPrintService()
        }
    }

    private fun performPrintScreen() {
        _state.value = Navigation.PrintScreen
        _state.value = Navigation.Default
    }

    private fun startService() {
        _state.value = Navigation.StartPrintService
//        _state.value = Navigation.Default
    }

    private fun stopPrintService() {
        _state.value = Navigation.StopPrintService
    }

    private fun performLoggerScreen() {
        _state.value = Navigation.LoggerScreen
        _state.value = Navigation.Default
    }

    private fun popBackStack() {
        _state.value = Navigation.PopBackStack
        _state.value = Navigation.Default
    }


}