package com.vc.vcposprintservice.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vc.vcposprintservice.domain.model.Auth
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAuth(auth: Auth)

    @Query("SELECT * FROM auth WHERE id=:id")
    suspend fun getAuth(id: Int): Auth

}