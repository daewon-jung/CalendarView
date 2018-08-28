package com.daewonjung.calendarview

interface DateSelectListener {

    fun onDateSelected(year: Int, month: Int, day: Int)

    fun onDateRangeSelected(start: CalendarDate, end: CalendarDate)

    fun onSelectLimitDayExceed(start: CalendarDate, end: CalendarDate, selectLimitDay: Int)
}