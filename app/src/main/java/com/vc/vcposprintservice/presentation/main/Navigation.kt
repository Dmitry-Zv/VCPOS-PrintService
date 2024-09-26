package com.vc.vcposprintservice.presentation.main

sealed class Navigation {
    data object PrintScreen : Navigation()
    data object StartPrintService : Navigation()
    data object LoggerScreen:Navigation()
}