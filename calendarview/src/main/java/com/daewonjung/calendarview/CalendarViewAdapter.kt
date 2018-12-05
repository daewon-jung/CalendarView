package com.daewonjung.calendarview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import java.util.*

open class CalendarViewAdapter(
    private val context: Context,
    private val dateSelectListener: DateSelectListener?,
    viewState: ViewState,
    dotList: List<Date>?,
    private val viewAttrs: ViewAttrs
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), MonthView.OnDateClickListener {

    private var _viewState: ViewState = viewState
    private var _dotList: List<Date>? = dotList

    var viewState: ViewState
        get() = _viewState
        set(value) {
            _viewState = value
            notifyDataSetChanged()
        }

    var dotList: List<Date>?
        get() = _dotList
        set(value) {
            _dotList = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder.create(context, viewAttrs, this)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val startYear: Int = _viewState.startDate?.year ?: 0
        val startMonth: Int = _viewState.startDate?.month?.minus(1) ?: 0

        val year = (position + startMonth) / CalendarView.MONTHS_IN_YEAR + startYear
        val month = ((startMonth + position) % CalendarView.MONTHS_IN_YEAR) + 1

        onBindViewHolder(
            viewHolder,
            MonthData(
                year,
                month,
                _viewState.startDate,
                _viewState.endDate,
                _viewState.todaySelected,
                _viewState.selectType,
                _viewState.selectedDates,
                _dotList
            )
        )
    }

    protected open fun onBindViewHolder(holder: RecyclerView.ViewHolder, monthData: MonthData) {
        (holder as? ViewHolder)?.bind(monthData)
    }

    override fun getItemCount(): Int {
        val startDate = this._viewState.startDate
        val endDate = this._viewState.endDate

        return when {
            endDate == null -> Int.MAX_VALUE
            startDate == null -> {
                val endYear = endDate.year
                val endMonth = endDate.month

                endYear * CalendarView.MONTHS_IN_YEAR + endMonth
            }
            else -> {
                val diffYear = endDate.year - startDate.year
                val diffMonth = endDate.month - startDate.month

                diffYear * CalendarView.MONTHS_IN_YEAR + diffMonth + 1
            }
        }
    }

    fun getPositionOfDate(date: CalendarDate): Int? {
        val startYear: Int = _viewState.startDate?.year ?: 0
        val startMonth: Int = _viewState.startDate?.month?.minus(1) ?: 0

        val targetYear = date.year
        val targetMonth = date.month - 1
        val targetTotalMonths = targetYear * CalendarView.MONTHS_IN_YEAR + targetMonth
        val startTotalMonths = startYear * CalendarView.MONTHS_IN_YEAR + startMonth
        val position = targetTotalMonths - startTotalMonths
        return if (position in 0 until itemCount) position else null
    }

    override fun onDateClicked(calendarDate: CalendarDate) {
        val prevSelectedDates = this._viewState.selectedDates
        val selectType = this._viewState.selectType

        val changedSelectedDates = when (selectType) {
            is ViewState.SelectType.OneDay -> createSelectedDatesForOneDayType(
                prevSelectedDates,
                calendarDate
            )
            is ViewState.SelectType.DayRange -> createSelectedDatesForDayRangeType(
                prevSelectedDates,
                calendarDate
            )
            is ViewState.SelectType.WeekRange -> createSelectedDatesForWeekRangeType(
                prevSelectedDates,
                calendarDate
            )
            is ViewState.SelectType.MonthRange -> createSelectedDatesForMonthRangeType(
                prevSelectedDates,
                calendarDate
            )
        }

        if (prevSelectedDates != changedSelectedDates) {
            this._viewState = this._viewState.copy(selectedDates = changedSelectedDates)
            dateSelectListener?.onSelectedDatesChanged(
                changedSelectedDates.start,
                changedSelectedDates.end
            )
            notifyDataSetChanged()
        }
    }

    override fun onSelectLimitExceed(
        startDate: CalendarDate,
        endDate: CalendarDate,
        selectType: ViewState.SelectType,
        limit: Int
    ) {
        dateSelectListener?.onSelectLimitExceed(startDate, endDate, selectType, limit)
    }

    private fun createSelectedDatesForOneDayType(
        prevSelectedDate: SelectedDates,
        calendarDate: CalendarDate
    ): SelectedDates =
        if (prevSelectedDate.start == calendarDate && prevSelectedDate.end == calendarDate) {
            SelectedDates(null, null)
        } else {
            SelectedDates(calendarDate, calendarDate)
        }

    private fun createSelectedDatesForDayRangeType(
        prevSelectedDate: SelectedDates,
        calendarDate: CalendarDate
    ): SelectedDates = when {
        prevSelectedDate.start == null && prevSelectedDate.end == null ->
            prevSelectedDate.copy(start = calendarDate)
        prevSelectedDate.start != null && prevSelectedDate.end == null ->
            if (prevSelectedDate.start.date.time > calendarDate.date.time) {
                SelectedDates(calendarDate, prevSelectedDate.start)
            } else {
                prevSelectedDate.copy(end = calendarDate)
            }
        prevSelectedDate.start != null && prevSelectedDate.end != null ->
            SelectedDates(null, null)
        else -> prevSelectedDate
    }

    private fun createSelectedDatesForWeekRangeType(
        prevSelectedDate: SelectedDates,
        calendarDate: CalendarDate
    ): SelectedDates = when {
        prevSelectedDate.start == null && prevSelectedDate.end == null ->
            prevSelectedDate.copy(start = getWeekStartDate(calendarDate))
        prevSelectedDate.start != null && prevSelectedDate.end == null ->
            if (prevSelectedDate.start.date.time > calendarDate.date.time) {
                SelectedDates(
                    getWeekStartDate(calendarDate),
                    getWeekEndDate(prevSelectedDate.start)
                )
            } else {
                prevSelectedDate.copy(end = getWeekEndDate(calendarDate))
            }
        prevSelectedDate.start != null && prevSelectedDate.end != null ->
            SelectedDates(null, null)
        else -> prevSelectedDate
    }

    private fun getWeekStartDate(calendarDate: CalendarDate): CalendarDate {
        val startDate = this._viewState.startDate

        val calendar = Calendar.getInstance()
        calendar.time = calendarDate.date
        val difference = if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            -6
        } else {
            Calendar.MONDAY - calendar.get(Calendar.DAY_OF_WEEK)
        }

        if (startDate?.year == calendar.get(Calendar.YEAR) &&
            startDate.month == calendar.get(Calendar.MONTH) + 1 &&
            startDate.day >= calendar.get(Calendar.DAY_OF_MONTH) + difference
        ) {
            calendar.set(Calendar.DAY_OF_MONTH, startDate.day)
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, difference)
        }

        return CalendarDate.create(calendar.time)
    }

    private fun getWeekEndDate(calendarDate: CalendarDate): CalendarDate {
        val endDate = this._viewState.endDate

        val calendar = Calendar.getInstance()
        calendar.time = calendarDate.date
        val difference = if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            0
        } else {
            Calendar.SATURDAY - calendar.get(Calendar.DAY_OF_WEEK) + 1
        }

        if (endDate?.year == calendar.get(Calendar.YEAR) &&
            endDate.month == calendar.get(Calendar.MONTH) + 1 &&
            endDate.day <= calendar.get(Calendar.DAY_OF_MONTH) + difference
        ) {
            calendar.set(Calendar.DAY_OF_MONTH, endDate.day)
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, difference)
        }

        return CalendarDate.create(calendar.time)
    }

    private fun createSelectedDatesForMonthRangeType(
        prevSelectedDate: SelectedDates,
        calendarDate: CalendarDate
    ): SelectedDates = when {
        prevSelectedDate.start == null && prevSelectedDate.end == null ->
            prevSelectedDate.copy(start = getMonthStartDate(calendarDate))
        prevSelectedDate.start != null && prevSelectedDate.end == null ->
            if (prevSelectedDate.start.date.time > calendarDate.date.time) {
                SelectedDates(
                    getMonthStartDate(calendarDate),
                    getMonthEndDate(prevSelectedDate.start)
                )
            } else {
                prevSelectedDate.copy(end = getMonthEndDate(calendarDate))
            }
        prevSelectedDate.start != null && prevSelectedDate.end != null ->
            SelectedDates(null, null)
        else -> prevSelectedDate
    }

    private fun getMonthStartDate(calendarDate: CalendarDate): CalendarDate {
        val startDate = this._viewState.startDate

        val calendar = Calendar.getInstance()
        calendar.time = calendarDate.date
        val day = if (startDate?.year == calendar.get(Calendar.YEAR) &&
            startDate.month == calendar.get(Calendar.MONTH) + 1
        ) {
            startDate.day
        } else {
            1
        }
        calendar.set(Calendar.DAY_OF_MONTH, day)

        return CalendarDate.create(calendar.time)
    }

    private fun getMonthEndDate(calendarDate: CalendarDate): CalendarDate {
        val endDate = this._viewState.endDate

        val calendar = Calendar.getInstance()
        calendar.time = calendarDate.date
        val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val day = if (endDate?.year == calendar.get(Calendar.YEAR) &&
            endDate.month == calendar.get(Calendar.MONTH) + 1
        ) {
            endDate.day
        } else {
            lastDay
        }
        calendar.set(Calendar.DAY_OF_MONTH, day)

        return CalendarDate.create(calendar.time)
    }

    data class MonthData(
        val year: Int,
        val month: Int,
        val startDate: CalendarDate?,
        val endDate: CalendarDate?,
        val todaySelected: Boolean,
        val selectType: ViewState.SelectType,
        val selectedDates: SelectedDates,
        val dotList: List<Date>?
    )
}

