package com.daewonjung.calendarview

sealed class SelectType {

    object OneDay : SelectType()

    data class DayRange(
        val selectLimitDay: Int? = null
    ) : SelectType()

    data class WeekRange(
        val selectLimitWeek: Int? = null
    ) : SelectType()

    data class MonthRange(
        val selectLimitMonth: Int? = null
    ) : SelectType()
}