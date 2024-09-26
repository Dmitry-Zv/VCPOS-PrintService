package com.vc.vcposprintservice.domain.usecases.printer

import com.vc.vcposprintservice.domain.model.PrinterDevice
import com.vc.vcposprintservice.domain.repository.PrinterRepository
import javax.inject.Inject

class SavePrinter @Inject constructor(
    private val repository: PrinterRepository
) {
    suspend operator fun invoke(printerDevice: PrinterDevice) =
        repository.savePrinter(printerDevice = printerDevice)
}