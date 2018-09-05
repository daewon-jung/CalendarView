package com.daewonjung.calendarview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

open class CalendarViewAdapter(
    private val context: Context,
    private val dateSelectListener: DateSelectListener?,
    startDate: CalendarDate?,
    endDate: CalendarDate?,
    selectLimitDay: Int?,
    todaySelected: Boolean,
    private val viewAttrs: ViewAttrs
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), MonthView.OnDateClickListener {

    private var _startDate: CalendarDate? = startDate
    private var _endDate: CalendarDate? = endDate
    private var _selectLimitDay: Int? = selectLimitDay
    private var _todaySelected: Boolean = todaySelected
    private var _selectedDates = SelectedDates(null, null)

    var startDate: CalendarDate?
        get() = _startDate
        set(value) {
            _startDate = value
            _selectedDates = _selectedDates.copy(start = null, end = null)
            notifyDataSetChanged()
        }

    var endDate: CalendarDate?
        get() = _endDate
        set(value) {
            _endDate = value
            _selectedDates = _selectedDates.copy(start = null, end = null)
            notifyDataSetChanged()
        }

    var selectLimitDay: Int?
        get() = _selectLimitDay
        set(value) {
            _selectLimitDay = value
            _selectedDates = _selectedDates.copy(start = null, end = null)
            notifyDataSetChanged()
        }

    var todaySelected: Boolean
        get() = _todaySelected
        set(value) {
            _todaySelected = value
            notifyDataSetChanged()
        }

    var selectedDates: SelectedDates
        get() = _selectedDates
        set(value) {
            val start = value.start
            val end = value.end
            if (start != null) {
                if (!Utils.isDateInRange(startDate, endDate, start)) {
                    throw IllegalArgumentException("invalid start date")
                }
            }
            if (end != null) {
                if (!Utils.isDateInRange(startDate, endDate, end)) {
                    throw IllegalArgumentException("invalid end date")
                }
            }
            val limit = selectLimitDay
            if (start != null && end != null && limit != null) {
                if (Utils.checkSelectLimitDayExceed(limit, start, end)) {
                    throw IllegalArgumentException("exceed select limit day count")
                }
            }
            _selectedDates = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder.create(context, viewAttrs, this)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val startYear: Int = _startDate?.year ?: 0
        val startMonth: Int = _startDate?.month?.minus(1) ?: 0

        val year = (position + startMonth) / CalendarView.MONTHS_IN_YEAR + startYear
        val month = ((startMonth + position) % CalendarView.MONTHS_IN_YEAR) + 1
        onBindViewHolder(
            viewHolder,
            MonthData(
                year,
                month,
                _selectedDates,
                _startDate,
                _endDate,
                _selectLimitDay,
                _todaySelected
            )
        )
    }

    protected open fun onBindViewHolder(holder: RecyclerView.ViewHolder, monthData: MonthData) {
        (holder as? ViewHolder)?.bind(monthData)
    }

    override fun getItemCount(): Int {
        val startDate = this._startDate
        val endDate = this._endDate
        return when {
            endDate == null -> Int.MAX_VALUE
            startDate == null -> {
                val endYear = endDate.year
                val endMonth = endDate.month

                endYear * CalendarView.MONTHS_IN_YEAR + endMonth - 1
            }
            else -> {
                val diffYear = endDate.year - startDate.year
                val diffMonth = endDate.month - startDate.month

                diffYear * CalendarView.MONTHS_IN_YEAR + diffMonth + 1
            }
        }
    }

    override fun onDateClicked(calendarDate: CalendarDate) {
        dateSelectListener?.onDateSelected(calendarDate)

        val prevSelectedDays = this._selectedDates
        val changedSelectedDays = createSelectedDay(prevSelectedDays, calendarDate)

        if (prevSelectedDays != changedSelectedDays) {
            this._selectedDates = changedSelectedDays

            if (changedSelectedDays.start != null && changedSelectedDays.end != null) {
                dateSelectListener?.onDateRangeSelected(
                    changedSelectedDays.start,
                    changedSelectedDays.end
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

    private fun createSelectedDay(
        prevSelectedDate: SelectedDates,
        calendarDate: CalendarDate
    ): SelectedDates =
        when {
            prevSelectedDate.start == null && prevSelectedDate.end == null ->
                prevSelectedDate.copy(start = calendarDate)
            prevSelectedDate.start != null && prevSelectedDate.end == null ->
                if (prevSelectedDate.start.date.time > calendarDate.date.time) {
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

