package com.daewonjung.calendarview

import java.util.*

data class CalendarDate(
    val year: Int,
    val month: Int /* 1 - 12 */,
    val day: Int /* 1 - 31 */
) {

    val date: Date = {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.time
    }()

    companion object {

        fun create(date: Date): CalendarDate {
            val calendar = Calendar.getInstance()
            calendar.time = date
            return CalendarDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}