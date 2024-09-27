package com.vc.vcposprintservice.domain.usecases.servicestate

import com.vc.vcposprintservice.domain.repository.ServiceStateRepository
import javax.inject.Inject

class GetState @Inject constructor(
    private val repository: ServiceStateRepository
) {
    operator fun invoke(): Boolean =
        repository.getState()
}