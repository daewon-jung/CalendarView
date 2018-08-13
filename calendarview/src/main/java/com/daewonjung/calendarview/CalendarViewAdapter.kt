package com.daewonjung.calendarview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup


class CalendarViewAdapter(
    private val context: Context,
    private val dateSelectListener: DateSelectListener?,
    private var startDate: CalendarDate?,
    private var endDate: CalendarDate?,
    private val selectLimitDay: Int,
    private val viewAttrs: ViewAttrs
) : RecyclerView.Adapter<ViewHolder>(), MonthView.OnDateClickListener {

    var selectedDates = SelectedDates(null, null)


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder =
        ViewHolder.create(context, viewAttrs, this)

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val startYear: Int = startDate?.year ?: 0
        val startMonth: Int = startDate?.month ?: 0

        val year: Int =
            position / Constants.MONTHS_IN_YEAR + startYear + (startMonth + position % Constants.MONTHS_IN_YEAR) / Constants.MONTHS_IN_YEAR
        val month: Int =
            (startMonth + position % Constants.MONTHS_IN_YEAR) % Constants.MONTHS_IN_YEAR

        viewHolder.bind(year, month, selectedDates, startDate, endDate, selectLimitDay)
    }

    override fun getItemCount(): Int {
        return when {
            endDate == null -> Int.MAX_VALUE
            startDate == null -> (endDate?.year?.times(Constants.MONTHS_IN_YEAR)
                    ?: Int.MAX_VALUE) + (endDate?.month?.plus(1) ?: 0)
            else -> ((endDate?.year?.minus(startDate?.year ?: 0))?.times(Constants.MONTHS_IN_YEAR)
                    ?: Int.MAX_VALUE) + (endDate?.month?.minus(startDate?.month ?: 0)?.plus(1) ?: 0)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
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

    override fun onInvalidDateClicked(calendarDate: CalendarDate) {
        dateSelectListener?.onInvalidDateSelected(
            calendarDate.year,
            calendarDate.month,
            calendarDate.day,
            selectLimitDay
        )
    }

    fun setStartDate(date: CalendarDate) {
        this.startDate = date
        selectedDates = selectedDates.copy(start = null, end = null)
        notifyDataSetChanged()
    }

    fun setEndDate(date: CalendarDate) {
        this.endDate = date
        selectedDates = selectedDates.copy(start = null, end = null)
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
                if (prevSelectedDate.start.createCalendar().timeInMillis > calendarDate.createCalendar().timeInMillis) {
                    prevSelectedDate.copy(start = calendarDate, end = prevSelectedDate.start)
                } else {
                    prevSelectedDate.copy(end = calendarDate)
                }
            prevSelectedDate.start != null && prevSelectedDate.end != null ->
                prevSelectedDate.copy(start = calendarDate, end = null)
            else -> prevSelectedDate
        }
}