package com.vc.vcposprintservice.presentation.screen.printscreen

import com.vc.vcposprintservice.domain.model.Auth

data class AuthForm(
    val login: String,
    val password: String,
    val loginError: String?,
    val passwordError: String?,
    val auth: Auth?,
    val isPrinterDeviceIsSave: Boolean
) {
    companion object {
        val default = AuthForm(
            "",
            "",
            null,
            null,
            null,
            false
        )
    }
}
