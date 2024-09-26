package com.vc.vcposprintservice.presentation.screen.printscreen

import com.vc.vcposprintservice.domain.model.PrinterDevice

sealed class PrintScreenEvent {
    data class CheckAuthenticationForm(
        val login: String,
        val password: String
    ) : PrintScreenEvent()

    data object CheckAuthWasSave : PrintScreenEvent()

    data class SavePrinter(val printerDevice: PrinterDevice) : PrintScreenEvent()

}