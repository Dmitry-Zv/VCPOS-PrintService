package com.vc.vcposprintservice.presentation.screen.printscreen

import com.vc.vcposprintservice.domain.model.Auth

data class AuthForm(
    val login: String = "",
    val password: String = "",
    val loginError: String? = null,
    val passwordError: String? = null,
    val counterOfFilesError: String? = null,
    val auth: Auth? = null,
    val isPrinterDeviceIsSave: Boolean = false,
) {
    companion object {
        val default = AuthForm(
            "",
            "",
            null,
            null,
            null,
            null,
            false
        )
    }
}
