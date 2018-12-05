package com.daewonjung.calendarview

sealed class DayState {

    object Disabled : DayState()

    data class NotSelected(
        val dayType: DayType = DayType.WEEKDAY,
        val dot: Boolean = false
    ) : DayState()

    data class Selected(
        val range: Range = Range.ONE_DAY,
        val dot: Boolean = false
    ) : DayState()

    enum class DayType {
        SUNDAY,
        WEEKDAY,
        SATURDAY,
        TODAY
    }

    enum class Range {
        ONE_DAY,
        START,
        MIDDLE,
        END
    }
}