package com.vc.vcposprintservice.presentation.screen.printscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vc.vcposprintservice.domain.model.Auth
import com.vc.vcposprintservice.domain.model.PrinterDevice
import com.vc.vcposprintservice.domain.usecases.auth.AuthUseCases
import com.vc.vcposprintservice.domain.usecases.printer.PrinterUseCases
import com.vc.vcposprintservice.presentation.common.Event
import com.vc.vcposprintservice.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrintViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val printerUseCases: PrinterUseCases
) : ViewModel(), Event<PrintScreenEvent> {


    private val _state = MutableStateFlow(AuthForm.default)
    val state = _state.asStateFlow()

    override fun onEvent(event: PrintScreenEvent) {
        when (event) {
            is PrintScreenEvent.CheckAuthenticationForm -> checkAuthenticationForm(
                login = event.login,
                password = event.password
            )

            PrintScreenEvent.CheckAuthWasSave -> checkAuthWasSave()
            is PrintScreenEvent.SavePrinter -> savePrinter(printerDevice = event.printerDevice)
        }
    }

    private fun checkAuthWasSave() {
        viewModelScope.launch {
            when (val result = authUseCases.getAuth()) {
                is Result.Error -> _state.value = _state.value.copy(
                    loginError = result.exception.message ?: "Unknown error",
                    passwordError = result.exception.message ?: "Unknown error"
                )

                is Result.Success -> _state.value = _state.value.copy(auth = result.data)
            }
        }
    }

    private fun checkAuthenticationForm(login: String, password: String) {
        viewModelScope.launch {
            if (login.isBlank()) {
                _state.value = _state.value.copy(loginError = "Поле логина пустое")
                return@launch
            }
            if (password.isBlank()) {
                _state.value = _state.value.copy(passwordError = "Поле пароля пустое")
                return@launch
            }
            authUseCases.saveAuth(auth = Auth(id = 1, login = login, password = password))
            _state.value = _state.value.copy(login = login, password = password)
        }

    }

    private fun savePrinter(printerDevice: PrinterDevice) {
        viewModelScope.launch {
            printerUseCases.savePrinter(printerDevice = printerDevice)
            _state.value = _state.value.copy(isPrinterDeviceIsSave = true)
        }
    }


}