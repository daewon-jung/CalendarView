package com.daewonjung.calendarview

import java.util.*

class Utils {

    companion object {

        fun getDaysInMonth(year: Int, month: Int): Int {
            return when (month) {
                Calendar.JANUARY, Calendar.MARCH, Calendar.MAY, Calendar.JULY, Calendar.AUGUST, Calendar.OCTOBER, Calendar.DECEMBER -> 31
                Calendar.APRIL, Calendar.JUNE, Calendar.SEPTEMBER, Calendar.NOVEMBER -> 30
                Calendar.FEBRUARY ->
                    if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0)
                        29
                    else
                        28
                else -> throw IllegalArgumentException("Invalid Month - $month")
            }
        }
    }
}