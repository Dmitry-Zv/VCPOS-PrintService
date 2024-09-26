package com.vc.vcposprintservice.domain.usecases.printer

import com.vc.vcposprintservice.domain.model.PrinterDevice
import com.vc.vcposprintservice.domain.repository.PrinterRepository
import com.vc.vcposprintservice.utils.Result
import javax.inject.Inject

class GetPrinter @Inject constructor(
    private val repository: PrinterRepository
) {

    suspend operator fun invoke(): Result<PrinterDevice> =
        repository.getPrinter(id = 1)
}