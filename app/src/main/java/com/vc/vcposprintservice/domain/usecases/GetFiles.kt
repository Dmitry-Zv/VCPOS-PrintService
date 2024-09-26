package com.vc.vcposprintservice.domain.usecases

import android.content.Context
import com.vc.vcposprintservice.domain.model.PostAuth
import com.vc.vcposprintservice.domain.repository.FileRepository
import com.vc.vcposprintservice.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class GetFiles @Inject constructor(private val repository: FileRepository) {

    suspend operator fun invoke(
        context: Context,
        assetsFileNames: List<String>
    ): Result<List<String>> =
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
                    Result.Success(data = assetsFileNames)
                } else {
                    throw Exception("There aren't files in back")
                }
            } catch (e: Exception) {
                Result.Error(exception = e)
            }
        }

    suspend fun invoke2(context: Context, postAuth: PostAuth): Result<List<String>> =
        withContext(Dispatchers.IO) {
            when (val result = repository.getFiles(postAuth = postAuth)) {
                is Result.Error -> Result.Error(exception = result.exception)
                is Result.Success -> {
                    try {
                        result.data.forEach { fileResponse ->
                            val fileData = fileResponse.data
                            val fileName = fileResponse.name

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
                        val fileNames = result.data.map { it.name }
                        Result.Success(data = fileNames)
                    } catch (e: Exception) {
                        Result.Error(exception = e)
                    }
                }
            }
        }
}

