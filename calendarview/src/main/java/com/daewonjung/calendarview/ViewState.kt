package com.daewonjung.calendarview

data class ViewState(
    val startDate: CalendarDate? = null,
    val endDate: CalendarDate? = null,
    val todaySelected: Boolean = false,
    val selectType: SelectType = SelectType.OneDay,
    val selectedDates: SelectedDates = SelectedDates(null, null)
) {

    sealed class SelectType {

        object OneDay : SelectType()

        data class DayRange(
            val selectLimitDay: Int?
        ) : SelectType()

        data class WeekRange(
            val selectLimitWeek: Int?
        ) : SelectType()

        data class MonthRange(
            val selectLimitMonth: Int?
        ) : SelectType()
    }
}