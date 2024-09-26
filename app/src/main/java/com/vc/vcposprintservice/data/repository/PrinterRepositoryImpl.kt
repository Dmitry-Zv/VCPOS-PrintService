package com.vc.vcposprintservice.data.repository

import com.vc.vcposprintservice.data.local.PrinterDao
import com.vc.vcposprintservice.domain.model.PrinterDevice
import com.vc.vcposprintservice.domain.repository.PrinterRepository
import com.vc.vcposprintservice.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PrinterRepositoryImpl @Inject constructor(
    private val dao: PrinterDao
) : PrinterRepository {

    override suspend fun savePrinter(printerDevice: PrinterDevice) {
        dao.savePrinter(printerDevice = printerDevice)
    }

    override suspend fun getPrinter(id: Int): Result<PrinterDevice> =
        withContext(Dispatchers.IO) {
            try {
                val printerDevice = dao.getPrinter(id = id)
                Result.Success(data = printerDevice)
            } catch (e: Exception) {
                Result.Error(exception = e)
            }
        }
}