package com.daewonjung.calendarview

interface OnDateSelectedListener {

    fun onSelectedDatesChanged(
        start: CalendarDate?,
        end: CalendarDate?,
        selectType: SelectType
    )

    fun onSelectLimitExceed(
        start: CalendarDate,
        end: CalendarDate,
        selectType: SelectType,
        limit: Int
    )
}