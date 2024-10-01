package com.vc.vcposprintservice.domain.usecases.fileusecases

import com.vc.vcposprintservice.domain.repository.FileRepository
import com.vc.vcposprintservice.utils.Result
import javax.inject.Inject

class PutStatus @Inject constructor(
    private val repository: FileRepository
) {

    suspend operator fun invoke(fileId: Int, statusId: Int): Result<Unit> =
        repository.putStatus(fileId = fileId, statusId = statusId)
}