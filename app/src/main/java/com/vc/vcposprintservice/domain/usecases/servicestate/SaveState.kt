package com.vc.vcposprintservice.domain.usecases.servicestate

import com.vc.vcposprintservice.domain.repository.ServiceStateRepository
import javax.inject.Inject

class SaveState @Inject constructor(
    private val repository: ServiceStateRepository
) {
    operator fun invoke(isActive: Boolean) =
        repository.saveState(isActive = isActive)
}