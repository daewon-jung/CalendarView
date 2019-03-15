package com.daewonjung.calendarview

internal interface OnInternalDateSelectedListener {

    fun onSelectedDatesChanged(
        start: CalendarDate?,
        end: CalendarDate?,
        selectType: SelectType
    )

    fun onSelectLimitExceed(
        startDate: CalendarDate,
        endDate: CalendarDate,
        selectType: SelectType,
        limit: Int
    )

    fun onDateClicked(calendarDate: CalendarDate, selectType: SelectType)

    fun onSelectedBeforeStartDate(startDate: CalendarDate, selectedDate: CalendarDate)
    fun onSelectedAfterEndDate(endDate: CalendarDate, selectedDate: CalendarDate)

}