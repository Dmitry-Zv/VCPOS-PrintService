package com.vc.vcposprintservice.presentation.screen.loggerscreen

import android.widget.DatePicker

sealed class LoggerEvent {
    data class OnDatePicked(val datePicker: DatePicker) : LoggerEvent()
}