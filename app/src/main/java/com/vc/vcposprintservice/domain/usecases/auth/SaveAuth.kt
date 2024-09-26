package com.vc.vcposprintservice.domain.usecases.auth

import com.vc.vcposprintservice.domain.model.Auth
import com.vc.vcposprintservice.domain.repository.AuthRepository
import javax.inject.Inject

class SaveAuth @Inject constructor(private val repository: AuthRepository) {

    suspend operator fun invoke(auth: Auth) {
        repository.insertOrReplaceAuth(auth = auth)
    }
}