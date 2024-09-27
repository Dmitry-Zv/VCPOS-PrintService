package com.vc.vcposprintservice.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth")
data class Auth(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val login: String,
    val password: String,
    val counterOfFiles:Int
)
