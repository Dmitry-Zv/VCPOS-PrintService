package com.vc.vcposprintservice.presentation.screen.printscreen

import com.vc.vcposprintservice.domain.model.PrinterDevice

sealed class PrintScreenEvent {
    data class CheckAuthenticationForm(
        val login: String,
        val password: String,
        val counterOfFiles: String
    ) : PrintScreenEvent()

    data class SavePrinter(val printerDevice: PrinterDevice) : PrintScreenEvent()

    data object PerformDefault:PrintScreenEvent()
}