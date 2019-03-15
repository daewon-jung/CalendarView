package com.daewonjung.calendarview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import java.util.*

internal open class CalendarViewAdapter(
    private val context: Context,
    private val onInternalDateSelectedListener: OnInternalDateSelectedListener?,
    viewState: ViewState,
    private val viewAttrs: ViewAttrs
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), OnInternalDateSelectedListener {


    private var _viewState: ViewState = viewState

    var viewState: ViewState
        get() = _viewState
        set(value) {
            _viewState = value
            notifyDataSetChanged()
        }

    var dotSource: ((Int, (Map<Int, List<Int>>) -> Unit, (Throwable) -> Unit) -> Unit)? = null
    private val dotDataMap = mutableMapOf<Int, DotData>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder =
        ViewHolder.create(context, viewAttrs, this)

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val startYear: Int = _viewState.startDate?.year ?: 0
        val startMonth: Int = _viewState.startDate?.month?.minus(1) ?: 0

        val year = (position + startMonth) / CalendarView.MONTHS_IN_YEAR + startYear
        val month = ((startMonth + position) % CalendarView.MONTHS_IN_YEAR) + 1

        val dotDayList = getDotDayList(year, month)

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
                dotDayList
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

                var count = endYear * CalendarView.MONTHS_IN_YEAR + endMonth

                with(Calendar.getInstance()) {
                    if (endYear == get(Calendar.YEAR) && endMonth == get(Calendar.MONTH) + 1) {
                        val middleDay = (1 + getActualMaximum(Calendar.DAY_OF_MONTH)) / 2
                        if (get(Calendar.DAY_OF_MONTH) >= middleDay) {
                            count++
                        }
                    }
                }

                count
            }
            else -> {
                val diffYear = endDate.year - startDate.year
                val diffMonth = endDate.month - startDate.month

                diffYear * CalendarView.MONTHS_IN_YEAR + diffMonth + 1
            }
        }
    }

    private fun getDotDayList(year: Int, month: Int): List<Int>? {
        val dotSource = dotSource
        dotSource?.let {
            val dotData = dotDataMap[year]
            if (dotData == null || dotData is DotData.Invalid) {
                dotDataMap[year] = DotData.Loading
                dotSource.invoke(
                    year,
                    { dateListMap ->
                        dotDataMap[year] = DotData.Valid(dateListMap)
                        notifyDataSetChanged()
                    }, { throwable ->
                        dotDataMap[year] = DotData.Invalid
                        throwable.printStackTrace()
                    }
                )
            }

            return if (dotData is DotData.Valid) {
                dotData.dayListMap[month]
            } else {
                null
            }
        }

        return null
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

    override fun onDateClicked(calendarDate: CalendarDate, selectType: SelectType) {
        val prevSelectedDates = this._viewState.selectedDates

        val changedSelectedDates = when (selectType) {
            is SelectType.OneDay -> createSelectedDatesForOneDayType(
                prevSelectedDates,
                calendarDate
            )
            is SelectType.DayRange -> createSelectedDatesForDayRangeType(
                prevSelectedDates,
                calendarDate
            )
            is SelectType.WeekRange -> createSelectedDatesForWeekRangeType(
                prevSelectedDates,
                calendarDate
            )
            is SelectType.MonthRange -> createSelectedDatesForMonthRangeType(
                prevSelectedDates,
                calendarDate
            )
        }

        if (prevSelectedDates != changedSelectedDates) {
            this._viewState = this._viewState.copy(selectedDates = changedSelectedDates)
            onInternalDateSelectedListener?.onSelectedDatesChanged(
                changedSelectedDates.start,
                changedSelectedDates.end,
                selectType
            )
            notifyDataSetChanged()
        }
    }

    override fun onSelectLimitExceed(
        startDate: CalendarDate,
        endDate: CalendarDate,
        selectType: SelectType,
        limit: Int
    ) {
        onInternalDateSelectedListener?.onSelectLimitExceed(startDate, endDate, selectType, limit)
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
        val firstDayInWeek = Utils.getFirstDateInWeek(calendarDate.date)

        val date = if (startDate?.date != null && firstDayInWeek.before(startDate.date)) {
            startDate.date
        } else {
            firstDayInWeek
        }

        return CalendarDate.create(date)
    }

    private fun getWeekEndDate(calendarDate: CalendarDate): CalendarDate {
        val endDate = this._viewState.endDate
        val lastDayInWeek = Utils.getLastDateInWeek(calendarDate.date)

        val date = if (endDate?.date != null && lastDayInWeek.after(endDate.date)) {
            endDate.date
        } else {
            lastDayInWeek
        }

        return CalendarDate.create(date)
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
        val firstDayInMonth = Calendar.getInstance().apply {
            time = calendarDate.date
            set(Calendar.DAY_OF_MONTH, 1)
        }.time

        val date = if (startDate?.date != null && firstDayInMonth.before(startDate.date)) {
            startDate.date
        } else {
            firstDayInMonth
        }

        return CalendarDate.create(date)
    }

    private fun getMonthEndDate(calendarDate: CalendarDate): CalendarDate {
        val endDate = this._viewState.endDate
        val lastDayInMonth = Calendar.getInstance().apply {
            time = calendarDate.date
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }.time

        val date = if (endDate?.date != null && lastDayInMonth.after(endDate.date)) {
            endDate.date
        } else {
            lastDayInMonth
        }

        return CalendarDate.create(date)
    }

    override fun onSelectedDatesChanged(
        start: CalendarDate?,
        end: CalendarDate?,
        selectType: SelectType
    ) {
        throw UnsupportedOperationException("Not use onSelectedDatesChanged in CalendarViewAdapter")
    }

    override fun onSelectedBeforeStartDate(startDate: CalendarDate, selectedDate: CalendarDate) {
        onInternalDateSelectedListener?.onSelectedBeforeStartDate(startDate, selectedDate)
    }

    override fun onSelectedAfterEndDate(endDate: CalendarDate, selectedDate: CalendarDate) {
        onInternalDateSelectedListener?.onSelectedAfterEndDate(endDate, selectedDate)
    }

    data class MonthData(
        val year: Int,
        val month: Int,
        val startDate: CalendarDate?,
        val endDate: CalendarDate?,
        val todaySelected: Boolean,
        val selectType: SelectType,
        val selectedDates: SelectedDates,
        val dotDayList: List<Int>?
    )
}

