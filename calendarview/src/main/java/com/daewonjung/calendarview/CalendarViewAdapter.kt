package com.daewonjung.calendarview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

open class CalendarViewAdapter(
    private val context: Context,
    private val dateSelectListener: DateSelectListener?,
    private var startDate: CalendarDate?,
    private var endDate: CalendarDate?,
    private var selectLimitDay: Int?,
    private var todaySelected: Boolean,
    private val viewAttrs: ViewAttrs
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), MonthView.OnDateClickListener {

    var selectedDates = SelectedDates(null, null)


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder.create(context, viewAttrs, this)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val startYear: Int = startDate?.year ?: 0
        val startMonth: Int = startDate?.month ?: 0

        val year: Int =
            (position / CalendarView.MONTHS_IN_YEAR) + startYear +
                    ((startMonth + position % CalendarView.MONTHS_IN_YEAR) /
                            CalendarView.MONTHS_IN_YEAR)
        val month: Int = (startMonth + (position % CalendarView.MONTHS_IN_YEAR)) %
                CalendarView.MONTHS_IN_YEAR
        onBindViewHolder(
            viewHolder,
            MonthData(year, month, selectedDates, startDate, endDate, selectLimitDay, todaySelected)
        )
    }

    protected open fun onBindViewHolder(holder: RecyclerView.ViewHolder, monthData: MonthData) {
        (holder as? ViewHolder)?.bind(monthData)
    }

    override fun getItemCount(): Int {
        return when {
            endDate == null -> Int.MAX_VALUE
            startDate == null -> {
                val endYear = endDate?.year
                val endMonth = endDate?.month

                (endYear?.times(CalendarView.MONTHS_IN_YEAR) ?: Int.MAX_VALUE) +
                        (endMonth?.plus(1) ?: 0)
            }
            else -> {
                val diffYear = endDate?.year?.minus(startDate?.year ?: 0)
                val diffMonth = endDate?.month?.minus(startDate?.month ?: 0)

                (diffYear?.times(CalendarView.MONTHS_IN_YEAR) ?: Int.MAX_VALUE) +
                        (diffMonth?.plus(1) ?: 0)
            }
        }
    }

    override fun onDateClicked(calendarDate: CalendarDate) {
        dateSelectListener?.onDateSelected(
            calendarDate.year,
            calendarDate.month + 1,
            calendarDate.day
        )

        val prevSelectedDays = this.selectedDates
        val changedSelectedDays = createSelectedDay(prevSelectedDays, calendarDate)

        if (prevSelectedDays != changedSelectedDays) {
            this.selectedDates = changedSelectedDays

            if (changedSelectedDays.start != null && changedSelectedDays.end != null) {
                dateSelectListener?.onDateRangeSelected(
                    changedSelectedDays.start.copy(month = changedSelectedDays.start.month + 1),
                    changedSelectedDays.end.copy(month = changedSelectedDays.end.month + 1)
                )
            }
            notifyDataSetChanged()
        }
    }

    override fun onSelectLimitDayExceed(
        startDate: CalendarDate,
        endDate: CalendarDate,
        limit: Int
    ) {
        dateSelectListener?.onSelectLimitDayExceed(startDate, endDate, limit)
    }

    fun setStartDate(date: CalendarDate?) {
        this.startDate = date
        selectedDates = selectedDates.copy(start = null, end = null)
        notifyDataSetChanged()
    }

    fun setEndDate(date: CalendarDate?) {
        this.endDate = date
        selectedDates = selectedDates.copy(start = null, end = null)
        notifyDataSetChanged()
    }

    fun setSelectLimitDay(days: Int?) {
        this.selectLimitDay = days
        selectedDates = selectedDates.copy(start = null, end = null)
        notifyDataSetChanged()
    }

    fun setTodaySelected(selected: Boolean) {
        this.todaySelected = selected
        notifyDataSetChanged()
    }

    private fun createSelectedDay(
        prevSelectedDate: SelectedDates,
        calendarDate: CalendarDate
    ): SelectedDates =
        when {
            prevSelectedDate.start == null && prevSelectedDate.end == null ->
                prevSelectedDate.copy(start = calendarDate)
            prevSelectedDate.start != null && prevSelectedDate.end == null ->
                if (prevSelectedDate.start.createCalendar().timeInMillis >
                    calendarDate.createCalendar().timeInMillis
                ) {
                    prevSelectedDate.copy(start = calendarDate, end = prevSelectedDate.start)
                } else {
                    prevSelectedDate.copy(end = calendarDate)
                }
            prevSelectedDate.start != null && prevSelectedDate.end != null ->
                prevSelectedDate.copy(start = calendarDate, end = null)
            else -> prevSelectedDate
        }

    data class MonthData(
        val year: Int,
        val month: Int,
        val selectedDays: SelectedDates,
        val startDate: CalendarDate?,
        val endDate: CalendarDate?,
        val selectLimitDay: Int?,
        val todaySelected: Boolean
    )
}

