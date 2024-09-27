package com.vc.vcposprintservice.domain.repository

interface ServiceStateRepository {

    fun saveState(isActive: Boolean)

    fun getState(): Boolean
}