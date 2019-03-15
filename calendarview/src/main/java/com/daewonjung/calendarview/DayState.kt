package com.daewonjung.calendarview

sealed class DayState {

    abstract val dayType: DayType
    abstract val today: Boolean
    abstract val dot: Boolean

    data class Disabled(
        override val dayType: DayType,
        override val today: Boolean,
        override val dot: Boolean
    ) : DayState()

    data class Normal(
        override val dayType: DayType,
        override val today: Boolean,
        override val dot: Boolean
    ) : DayState()

    data class Selected(
        val range: Range,
        override val dayType: DayType,
        override val today: Boolean,
        override val dot: Boolean
    ) : DayState()

    enum class DayType {
        SUNDAY,
        WEEKDAY,
        SATURDAY
    }

    enum class Range {
        ONE_DAY,
        START,
        MIDDLE,
        END
    }
}