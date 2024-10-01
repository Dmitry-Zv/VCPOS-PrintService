package com.vc.vcposprintservice.data.network

import com.vc.vcposprintservice.domain.model.FileResponse
import com.vc.vcposprintservice.domain.model.PostAuth
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FileApi {

    @POST("file/list")
    suspend fun getFiles(
        @Body postAuth: PostAuth,
        @Query("count") count: Int
    ): Response<List<FileResponse>>

    @PUT("file/{fileId}/{statusId}")
    suspend fun putStatus(
        @Path("fileId") fileId: Int,
        @Path("statusId") statusId: Int
    ):Response<Unit>
}