package com.vc.vcposprintservice.domain.repository

import com.vc.vcposprintservice.domain.model.Auth
import com.vc.vcposprintservice.utils.Result

interface AuthRepository {

    suspend fun insertOrReplaceAuth(auth: Auth)

    suspend fun getAuth(id: Int): Result<Auth>
}