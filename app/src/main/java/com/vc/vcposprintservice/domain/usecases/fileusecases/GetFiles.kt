package com.vc.vcposprintservice.domain.usecases.fileusecases

import android.content.Context
import android.util.Log
import com.vc.vcposprintservice.domain.model.Auth
import com.vc.vcposprintservice.domain.model.PostAuth
import com.vc.vcposprintservice.domain.repository.FileRepository
import com.vc.vcposprintservice.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class GetFiles @Inject constructor(private val repository: FileRepository) {

    suspend fun invoke2(
        context: Context,
        assetsFileNames: List<String>
    ): Result<Map<Int, String>> =
        withContext(Dispatchers.IO) {
            try {
                if (assetsFileNames.isNotEmpty()) {
                    assetsFileNames.forEach { fileName ->
                        context.assets.open(fileName).use { inputStream ->
                            val outPutFile = File(context.filesDir, fileName)
                            FileOutputStream(outPutFile).use { outputStream ->
                                val buffer = ByteArray(4096)
                                var bytesRead: Int
                                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                    outputStream.write(buffer, 0, bytesRead)
                                }
                                outputStream.flush()
                            }
                        }
                    }
                    var count = 1
                    val assetsWithId = assetsFileNames.associate { count++ to it }
                    Result.Success(data = assetsWithId)
                } else {
                    throw Exception("There aren't files in back")
                }
            } catch (e: Exception) {
                Result.Error(exception = e)
            }
        }

    suspend operator fun invoke(context: Context, auth: Auth): Result<Map<Int, String>> =
        withContext(Dispatchers.IO) {
            when (val result = repository.getFiles(
                postAuth = PostAuth.mapAuthToPostAuth(auth = auth),
                counterOfFiles = auth.counterOfFiles
            )) {
                is Result.Error -> Result.Error(exception = result.exception)
                is Result.Success -> {
                    try {
                        result.data.forEach { fileResponse ->
                            val fileData = fileResponse.data
                            val fileName = fileResponse.fileName

                            val file = File(context.filesDir, fileName)
                            FileOutputStream(file).use { outputStream ->
                                val buffer = ByteArray(4096)
                                var byteWritten = 0
                                var offset = 0
                                while (offset < fileData.size) {
                                    val length = minOf(buffer.size, fileData.size - offset)
                                    System.arraycopy(fileData, offset, buffer, 0, length)
                                    outputStream.write(buffer, 0, length)
                                    offset += length
                                    byteWritten += length
                                }
                                outputStream.flush()
                            }
                        }
                        val fileNames = result.data.associate { it.id to it.fileName }
                        Result.Success(data = fileNames)
                    } catch (e: Exception) {
                        Result.Error(exception = e)
                    }
                }
            }
        }
}

