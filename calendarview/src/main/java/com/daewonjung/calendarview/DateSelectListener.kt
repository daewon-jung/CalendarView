package com.daewonjung.calendarview

interface DateSelectListener {

    fun onDateSelected(date: CalendarDate)

    fun onDateRangeSelected(start: CalendarDate, end: CalendarDate)

    fun onSelectLimitDayExceed(start: CalendarDate, end: CalendarDate, selectLimitDay: Int)
}