package com.vc.vcposprintservice.presentation.screen.loggerscreen

import android.widget.DatePicker
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vc.vcposprintservice.presentation.common.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@HiltViewModel
class LoggerViewModel : ViewModel(), Event<LoggerEvent> {

    private val _state = MutableStateFlow<LoggerState>(LoggerState())
    val state = _state.asStateFlow()

    override fun onEvent(event: LoggerEvent) {
        when (event) {
            is LoggerEvent.OnDatePicked -> onDatePicked(datePicker = event.datePicker)
        }
    }

    private fun onDatePicked(datePicker: DatePicker) {
        val selectedYear = datePicker.year
        val selectedMonth = datePicker.month
        val selectedDay = datePicker.dayOfMonth
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, selectedDay)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var formattedDate: String? = sdf.format(calendar.time)
        val currentDay: String = sdf.format(Date())
        if (formattedDate == currentDay) {
            formattedDate = null
        }
        _state.value = _state.value.copy(date = formattedDate)
    }
}