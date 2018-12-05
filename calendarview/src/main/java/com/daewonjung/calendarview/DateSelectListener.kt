package com.daewonjung.calendarview

interface DateSelectListener {

    fun onSelectedDatesChanged(start: CalendarDate?, end: CalendarDate?)

    fun onSelectLimitExceed(
        start: CalendarDate,
        end: CalendarDate,
        selectType: ViewState.SelectType,
        limit: Int
    )
}