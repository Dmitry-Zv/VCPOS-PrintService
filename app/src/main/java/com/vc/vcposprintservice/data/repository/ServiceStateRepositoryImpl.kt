package com.vc.vcposprintservice.data.repository

import android.content.SharedPreferences
import com.vc.vcposprintservice.domain.repository.ServiceStateRepository
import com.vc.vcposprintservice.utils.Constants.PRINT_SERVICE_ACTIVE_STATE
import javax.inject.Inject

class ServiceStateRepositoryImpl @Inject constructor(
    private val preferences: SharedPreferences
) : ServiceStateRepository {

    override fun saveState(isActive: Boolean) {
        preferences.edit()
            .putBoolean(PRINT_SERVICE_ACTIVE_STATE, isActive)
            .apply()
    }

    override fun getState(): Boolean =
        preferences.getBoolean(PRINT_SERVICE_ACTIVE_STATE, false)
}