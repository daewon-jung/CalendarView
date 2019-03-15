package com.daewonjung.calendarview

import java.util.*

object Utils {

    fun getFirstDateInWeek(date: Date): Date =
        Calendar.getInstance().apply {
            time = date
            add(
                Calendar.DAY_OF_MONTH,
                if (get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    -6
                } else {
                    Calendar.MONDAY - get(Calendar.DAY_OF_WEEK)
                }
            )
        }.time

    fun getLastDateInWeek(date: Date): Date =
        Calendar.getInstance().apply {
            time = date
            add(
                Calendar.DAY_OF_MONTH,
                if (get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    0
                } else {
                    Calendar.SATURDAY - get(Calendar.DAY_OF_WEEK) + 1
                }
            )
        }.time
}