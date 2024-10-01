package com.vc.vcposprintservice.domain.repository

import com.vc.vcposprintservice.domain.model.FileResponse
import com.vc.vcposprintservice.domain.model.PostAuth
import com.vc.vcposprintservice.utils.Result

interface FileRepository {

    suspend fun getFiles(postAuth: PostAuth, counterOfFiles: Int): Result<List<FileResponse>>

    suspend fun putStatus(fileId: Int, statusId: Int): Result<Unit>
}