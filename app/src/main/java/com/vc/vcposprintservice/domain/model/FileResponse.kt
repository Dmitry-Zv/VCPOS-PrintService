package com.vc.vcposprintservice.domain.model

data class FileResponse(
    val id: Int,
    val fileName: String,
    val data: ByteArray
)