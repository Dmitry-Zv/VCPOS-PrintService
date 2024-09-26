package com.vc.vcposprintservice.domain.repository

import com.vc.vcposprintservice.domain.model.PrinterDevice
import com.vc.vcposprintservice.utils.Result

interface PrinterRepository {

    suspend fun savePrinter(printerDevice: PrinterDevice)

    suspend fun getPrinter(id: Int): Result<PrinterDevice>
}