package com.vc.vcposprintservice.presentation.screen.printscreen

import com.vc.vcposprintservice.domain.model.Auth

data class AuthForm(
    val login: String = "",
    val password: String = "",
    val loginError: String? = null,
    val passwordError: String? = null,
    val auth: Auth? = null,
    val isPrinterDeviceIsSave: Boolean = false,
    val isPrinterServiceActive: Boolean? = null
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
