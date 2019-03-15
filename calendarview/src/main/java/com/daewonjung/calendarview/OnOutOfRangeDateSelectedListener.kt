package com.daewonjung.calendarview

interface OnOutOfRangeDateSelectedListener {

    fun onSelectedBeforeStartDate(startDate: CalendarDate, selectedDate: CalendarDate)

    fun onSelectedAfterEndDate(endDate: CalendarDate, selectedDate: CalendarDate)
}