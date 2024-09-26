package com.vc.vcposprintservice.domain.model

data class FileResponse(
    val id: Int,
    val name: String,
    val data: ByteArray
)