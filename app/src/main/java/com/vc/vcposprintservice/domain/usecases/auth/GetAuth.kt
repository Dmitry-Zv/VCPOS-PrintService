package com.vc.vcposprintservice.domain.usecases.auth

import com.vc.vcposprintservice.domain.model.Auth
import com.vc.vcposprintservice.domain.repository.AuthRepository
import com.vc.vcposprintservice.utils.Result
import javax.inject.Inject

class GetAuth @Inject constructor(private val repository: AuthRepository) {

    suspend operator fun invoke(): Result<Auth> =
        repository.getAuth(id = 1)
}