package com.vc.vcposprintservice.domain.model

import com.google.gson.annotations.SerializedName

data class PostAuth(
    @SerializedName("username")
    val userName: String,
    val password: String
) {
    companion object {
        fun mapAuthToPostAuth(auth: Auth): PostAuth =
            PostAuth(
                userName = auth.login,
                password = auth.password
            )
    }
}
