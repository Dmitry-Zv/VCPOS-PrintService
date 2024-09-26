package com.vc.vcposprintservice.presentation.common

interface ToolBarSettings {

    fun setUpToolBar(title: String, enum: ToolBarEnum)
}

enum class ToolBarEnum {
    PRINT_FRAGMENT, LOGGER_FRAGMENT
}