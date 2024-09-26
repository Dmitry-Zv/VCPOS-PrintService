package com.vc.vcposprintservice.domain.model

import android.print.PrinterId
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "printer_device")
data class PrinterDevice(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val deviceId: Int,
    val vendorId: Int,
    val productName: String?,
    val manufactureName: String?
)