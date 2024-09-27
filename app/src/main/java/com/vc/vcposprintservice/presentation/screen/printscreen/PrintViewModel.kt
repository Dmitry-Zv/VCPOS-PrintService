package com.vc.vcposprintservice.presentation.screen.printscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vc.vcposprintservice.domain.model.Auth
import com.vc.vcposprintservice.domain.model.PrinterDevice
import com.vc.vcposprintservice.domain.usecases.auth.AuthUseCases
import com.vc.vcposprintservice.domain.usecases.printer.PrinterUseCases
import com.vc.vcposprintservice.domain.usecases.servicestate.GetState
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
    private val printerUseCases: PrinterUseCases,
    private val getState: GetState
) : ViewModel(), Event<PrintScreenEvent> {


    private val _state = MutableStateFlow(AuthForm.default)
    val state = _state.asStateFlow()

    override fun onEvent(event: PrintScreenEvent) {
        when (event) {
            is PrintScreenEvent.CheckAuthenticationForm -> checkAuthenticationForm(
                login = event.login,
                password = event.password,
                counterOfFiles = event.counterOfFiles
            )

            PrintScreenEvent.CheckAuthWasSave -> checkAuthWasSave()
            is PrintScreenEvent.SavePrinter -> savePrinter(printerDevice = event.printerDevice)
            PrintScreenEvent.CheckIfPrintServiceIsActive -> checkIfPrintServiceIsActive()
        }
    }

    private fun performDefault() {
        _state.value = AuthForm.default
    }

    private fun checkAuthWasSave() {
        viewModelScope.launch {
            when (val result = authUseCases.getAuth()) {
                is Result.Error -> _state.value = AuthForm(
                    auth = null
                )

                is Result.Success -> _state.value = AuthForm(auth = result.data)
            }
//            performDefault()
        }
    }

    private fun checkAuthenticationForm(login: String, password: String, counterOfFiles: Int) {
        viewModelScope.launch {
            if (login.isBlank()) {
                _state.value = AuthForm(loginError = "Поле логина пустое")
                return@launch
            }
            if (password.isBlank()) {
                _state.value = AuthForm(passwordError = "Поле пароля пустое")
                return@launch
            }
            authUseCases.saveAuth(
                auth = Auth(
                    id = 1,
                    login = login,
                    password = password,
                    counterOfFiles = counterOfFiles
                )
            )
            _state.value = AuthForm(login = login, password = password)
//            performDefault()
        }

    }

    private fun savePrinter(printerDevice: PrinterDevice) {
        viewModelScope.launch {
            printerUseCases.savePrinter(printerDevice = printerDevice)
            _state.value = AuthForm(isPrinterDeviceIsSave = true)
//            performDefault()
        }
    }

    private fun checkIfPrintServiceIsActive() {
        _state.value = AuthForm(isPrinterServiceActive = getState())
    }


}