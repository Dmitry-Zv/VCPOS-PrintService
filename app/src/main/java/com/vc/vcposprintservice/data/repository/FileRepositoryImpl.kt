package com.vc.vcposprintservice.data.repository

import com.vc.vcposprintservice.data.network.FileApi
import com.vc.vcposprintservice.domain.model.FileResponse
import com.vc.vcposprintservice.domain.model.PostAuth
import com.vc.vcposprintservice.domain.repository.FileRepository
import com.vc.vcposprintservice.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(private val fileApi: FileApi) : FileRepository {

    override suspend fun getFiles(postAuth: PostAuth): Result<List<FileResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val response = fileApi.getFiles(postAuth = postAuth)
                if (response.isSuccessful) {
                    val files = checkNotNull(response.body())
                    Result.Success(data = files)
                } else {
                    val errorBody = checkNotNull(response.errorBody())
                    Result.Error(exception = Exception(errorBody.string()))
                }
            } catch (e: Exception) {
                Result.Error(exception = e)
            }
        }
}