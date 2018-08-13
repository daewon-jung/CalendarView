package com.daewonjung.calendarview

interface DateSelectListener {

    fun onDateSelected(year: Int, month: Int, day: Int)

    fun onDateRangeSelected(start: CalendarDate, end: CalendarDate)

    fun onInvalidDateSelected(year: Int, month: Int, day: Int, selectLimitDay: Int)

}