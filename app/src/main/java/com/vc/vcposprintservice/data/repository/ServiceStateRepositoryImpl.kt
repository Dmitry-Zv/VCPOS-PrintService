package com.vc.vcposprintservice.data.repository

import android.content.SharedPreferences
import com.vc.vcposprintservice.domain.repository.ServiceStateRepository
import com.vc.vcposprintservice.utils.Constants.PRINT_SERVICE_ACTIVE_STATE
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ServiceStateRepositoryImpl @Inject constructor(
    private val preferences: SharedPreferences
) : ServiceStateRepository {

    override fun saveState(isActive: Boolean) {
        preferences.edit()
            .putBoolean(PRINT_SERVICE_ACTIVE_STATE, isActive)
            .apply()
    }

    override fun getState(): Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener{_,key->
            if (key == PRINT_SERVICE_ACTIVE_STATE){

                trySend(preferences.getBoolean(key, false))
            }
        }

        preferences.registerOnSharedPreferenceChangeListener(listener)

        trySend(preferences.getBoolean(PRINT_SERVICE_ACTIVE_STATE, false))

        awaitClose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

}