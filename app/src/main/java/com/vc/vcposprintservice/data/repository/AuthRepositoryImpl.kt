package com.vc.vcposprintservice.data.repository

import com.vc.vcposprintservice.data.local.AuthDao
import com.vc.vcposprintservice.domain.model.Auth
import com.vc.vcposprintservice.domain.repository.AuthRepository
import com.vc.vcposprintservice.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val dao: AuthDao
) : AuthRepository {

    override suspend fun insertOrReplaceAuth(auth: Auth) {
        dao.insertOrReplaceAuth(auth = auth)
    }

    override suspend fun getAuth(id: Int): Result<Auth> =
        withContext(Dispatchers.IO) {
            try {
                val auth = dao.getAuth(id = id)
                Result.Success(data = auth)
            } catch (e: Exception) {
                Result.Error(exception = e)
            }
        }

}