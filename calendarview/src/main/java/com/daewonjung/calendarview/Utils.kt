package com.daewonjung.calendarview

object Utils {

    fun isDateInRange(start: CalendarDate?, end: CalendarDate?, date: CalendarDate): Boolean {
        if (start != null && start.date.after(date.date)) {
            return false
        }
        if (end != null && end.date.before(date.date)) {
            return false
        }
        return true
    }

    fun checkSelectLimitDayExceed(
        limit: Int,
        start: CalendarDate,
        end: CalendarDate
    ): Boolean {
        val diffMilliSec = Math.abs(start.date.time - end.date.time)
        val diffDay = diffMilliSec / 1000 / 60 / 60 / 24
        return diffDay > limit
    }
}