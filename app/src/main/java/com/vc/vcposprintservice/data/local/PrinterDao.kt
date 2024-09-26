package com.vc.vcposprintservice.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vc.vcposprintservice.domain.model.PrinterDevice

@Dao
interface PrinterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePrinter(printerDevice: PrinterDevice)

    @Query("SELECT * FROM printer_device WHERE id=:id")
    suspend fun getPrinter(id: Int): PrinterDevice
}