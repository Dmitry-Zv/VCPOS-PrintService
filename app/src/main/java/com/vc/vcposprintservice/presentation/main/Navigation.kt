package com.vc.vcposprintservice.presentation.main

sealed class Navigation {
    data object PrintScreen : Navigation()
    data object StartPrintService : Navigation()
    data object StopPrintService : Navigation()
    data object LoggerScreen : Navigation()
    data object PopBackStack : Navigation()
    data object Default : Navigation()
}