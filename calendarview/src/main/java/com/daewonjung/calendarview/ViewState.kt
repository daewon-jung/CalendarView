package com.daewonjung.calendarview

data class ViewState(
    val startDate: CalendarDate? = null,
    val endDate: CalendarDate? = null,
    val todaySelected: Boolean = false,
    val selectType: SelectType = SelectType.OneDay,
    val selectedDates: SelectedDates = SelectedDates(null, null)
)