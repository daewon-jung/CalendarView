package com.daewonjung.calendarview

import java.util.*

data class CalendarDate(
    val year: Int,
    val month: Int,
    val day: Int
) {

    fun createCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar
    }
}