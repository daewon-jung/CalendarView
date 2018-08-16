package com.daewonjung.calendarview

data class ViewAttrs(
    val monthTextColor: Int = 0,
    val dayOfWeekTextColor: Int = 0,
    val todayTextColor: Int = 0,
    val dayTextColor: Int = 0,
    val saturdayTextColor: Int = 0,
    val sundayTextColor: Int = 0,

    val selectedDayBgColor: Int = 0,
    val selectedDayTextColor: Int = 0,
    val disabledDayColor: Int = 0,

    val monthHeight: Int = 0,
    val dayOfWeekHeight: Int = 0,
    val dayHeight: Int = 0,

    val monthTextSize: Int = 0,
    val dayOfWeekTextSize: Int = 0,
    val dayTextSize: Int = 0,

    val padding: Int = 0,
    val monthSpacing: Int = 0,

    val selectedCircleSize: Int = 0,
    val isCurrentDaySelected: Boolean = true
)
