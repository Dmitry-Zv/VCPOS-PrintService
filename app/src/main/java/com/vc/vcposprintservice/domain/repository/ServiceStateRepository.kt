package com.vc.vcposprintservice.domain.repository

import kotlinx.coroutines.flow.Flow

interface ServiceStateRepository {

    fun saveState(isActive: Boolean)

    fun getState(): Flow<Boolean>
}