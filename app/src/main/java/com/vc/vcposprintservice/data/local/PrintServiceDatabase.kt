package com.vc.vcposprintservice.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vc.vcposprintservice.domain.model.Auth
import com.vc.vcposprintservice.domain.model.PrinterDevice

@Database(entities = [Auth::class, PrinterDevice::class], version = 1)
abstract class PrintServiceDatabase : RoomDatabase() {
    abstract fun getAuthDao(): AuthDao
    abstract fun getPrinterDao(): PrinterDao
}