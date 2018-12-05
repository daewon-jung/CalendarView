package net.meshkorea.android.component.calendar

interface DateSelectListener {

    fun onSelectedDatesChanged(start: CalendarDate?, end: CalendarDate?)

    fun onSelectLimitDayExceed(start: CalendarDate, end: CalendarDate, selectLimitDay: Int)
}