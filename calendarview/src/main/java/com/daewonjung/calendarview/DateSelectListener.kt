package com.daewonjung.calendarview

interface DateSelectListener {

    fun onSelectedDatesChanged(start: CalendarDate?, end: CalendarDate?)

    fun onSelectLimitDayExceed(start: CalendarDate, end: CalendarDate, selectLimitDay: Int)
}