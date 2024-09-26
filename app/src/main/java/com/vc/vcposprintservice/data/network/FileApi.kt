package com.vc.vcposprintservice.data.network

import com.vc.vcposprintservice.domain.model.FileResponse
import com.vc.vcposprintservice.domain.model.PostAuth
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FileApi {

    @POST("file/list")
    suspend fun getFiles(@Body postAuth: PostAuth): Response<List<FileResponse>>
}