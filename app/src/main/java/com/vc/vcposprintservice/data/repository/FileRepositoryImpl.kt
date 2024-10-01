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

    override suspend fun getFiles(
        postAuth: PostAuth,
        counterOfFiles: Int
    ): Result<List<FileResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val response =
                    fileApi.getFiles(postAuth = postAuth, count = counterOfFiles)
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

    override suspend fun putStatus(fileId: Int, statusId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = fileApi.putStatus(fileId = fileId, statusId = statusId)
                if (response.isSuccessful) {
                    Result.Success(data = Unit)
                } else {
                    val errorBody = checkNotNull(response.errorBody())
                    Result.Error(exception = Exception(errorBody.string()))
                }
            } catch (e: Exception) {
                Result.Error(exception = e)
            }
        }
}