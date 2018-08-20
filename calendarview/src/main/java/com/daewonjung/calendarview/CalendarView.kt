package com.daewonjung.calendarview

import android.content.Context
import android.content.res.TypedArray
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import java.text.SimpleDateFormat
import java.util.*


@Suppress("unused")
class CalendarView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val calendarAdapter: CalendarViewAdapter
    var dateSelectListener: DateSelectListener? = null

    private val internalDateSelectListener = object : DateSelectListener {

        override fun onDateSelected(year: Int, month: Int, day: Int) {
            dateSelectListener?.onDateSelected(year, month, day)
        }

        override fun onDateRangeSelected(start: CalendarDate, end: CalendarDate) {
            dateSelectListener?.onDateRangeSelected(start, end)
        }

        override fun onInvalidDateSelected(year: Int, month: Int, day: Int, selectLimitDay: Int) {
            dateSelectListener?.onInvalidDateSelected(year, month, day, selectLimitDay)
        }
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CalendarView)
        val startDate = parseStringDate(typedArray.getString(R.styleable.CalendarView_startDate))
        val endDate = parseStringDate(typedArray.getString(R.styleable.CalendarView_endDate))
        val selectLimitDay =
            typedArray.getInt(R.styleable.CalendarView_selectLimitDay, Int.MAX_VALUE)
        val viewAttrs = initViewAttrs(typedArray)
        typedArray.recycle()

        calendarAdapter = CalendarViewAdapter(
            context,
            internalDateSelectListener,
            startDate,
            endDate,
            selectLimitDay,
            viewAttrs
        )

        layoutManager = LinearLayoutManager(context)
        adapter = calendarAdapter

        scrollInitPosition(startDate, endDate)
    }

    private fun scrollInitPosition(startDate: CalendarDate?, endDate: CalendarDate?) {
        val calendar = Calendar.getInstance()

        val curr = calendar.time
        val start = startDate?.createCalendar()?.time
        val end = endDate?.createCalendar()?.time

        if ((start == null || curr.after(start)) && (end == null || curr.before(end))) {

            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)

            val initPosition = if (startDate == null) {
                currentYear * MONTHS_IN_YEAR + currentMonth
            } else {
                (currentYear - startDate.year) * MONTHS_IN_YEAR -
                        startDate.month + currentMonth
            }

            scrollToPosition(initPosition)
        } else if (end != null && curr.after(end)) {
            scrollToPosition(endDate.year * MONTHS_IN_YEAR + endDate.month)
        }
    }

    private fun parseStringDate(strDate: String?): CalendarDate? =
        if (strDate?.isNotEmpty() != true) {
            null
        } else {
            try {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(strDate)
                val calendar = Calendar.getInstance()
                calendar.time = date
                CalendarDate(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    private fun initViewAttrs(typedArray: TypedArray): ViewAttrs {
        val monthTextColor = typedArray.getColor(
            R.styleable.CalendarView_colorMonthText,
            ContextCompat.getColor(context, R.color.normal_day)
        )
        val dayOfWeekTextColor = typedArray.getColor(
            R.styleable.CalendarView_colorDayOfWeekText,
            ContextCompat.getColor(context, R.color.normal_day)
        )
        val todayTextColor = typedArray.getColor(
            R.styleable.CalendarView_colorTodayText,
            ContextCompat.getColor(context, R.color.today)
        )
        val dayTextColor = typedArray.getColor(
            R.styleable.CalendarView_colorDayText,
            ContextCompat.getColor(context, R.color.normal_day)
        )
        val saturdayTextColor = typedArray.getColor(
            R.styleable.CalendarView_colorSaturdayText,
            ContextCompat.getColor(context, R.color.saturday)
        )
        val sundayTextColor = typedArray.getColor(
            R.styleable.CalendarView_colorSundayText,
            ContextCompat.getColor(context, R.color.sunday)
        )

        val disableDayColor = typedArray.getColor(
            R.styleable.CalendarView_colorDisabledDay,
            ContextCompat.getColor(context, R.color.disable_day)
        )
        val selectedDayBgColor = typedArray.getColor(
            R.styleable.CalendarView_colorSelectedDayBackground,
            ContextCompat.getColor(context, R.color.selected_day_background)
        )
        val selectedDayTextColor = typedArray.getColor(
            R.styleable.CalendarView_colorSelectedDayText,
            ContextCompat.getColor(context, R.color.selected_day_text)
        )

        val monthHeight = typedArray.getDimensionPixelOffset(
            R.styleable.CalendarView_monthHeight,
            resources.getDimensionPixelOffset(R.dimen.month_height)
        )
        val dayOfWeekHeight = typedArray.getDimensionPixelOffset(
            R.styleable.CalendarView_dayOfWeekHeight,
            resources.getDimensionPixelOffset(R.dimen.day_of_week_height)
        )
        val dayHeight = typedArray.getDimensionPixelOffset(
            R.styleable.CalendarView_dayHeight,
            resources.getDimensionPixelOffset(R.dimen.day_height)
        )

        val monthTextSize = typedArray.getDimensionPixelSize(
            R.styleable.CalendarView_textSizeMonth,
            resources.getDimensionPixelSize(R.dimen.month_text_size)
        )
        val dayOfWeekTextSize = typedArray.getDimensionPixelSize(
            R.styleable.CalendarView_textSizeDayOfWeek,
            resources.getDimensionPixelSize(R.dimen.day_of_week_text_size)
        )
        val dayTextSize = typedArray.getDimensionPixelSize(
            R.styleable.CalendarView_textSizeDay,
            resources.getDimensionPixelSize(R.dimen.day_text_size)
        )

        val monthSpacing = typedArray.getDimensionPixelSize(
            R.styleable.CalendarView_monthSpacing,
            resources.getDimensionPixelOffset(R.dimen.monthSpacing)
        )
        val padding = typedArray.getDimensionPixelSize(
            R.styleable.CalendarView_padding,
            resources.getDimensionPixelOffset(R.dimen.padding)
        )

        val selectedCircleSize = typedArray.getDimensionPixelSize(
            R.styleable.CalendarView_selectedDayRadius,
            resources.getDimensionPixelOffset(R.dimen.selected_circle_size)
        )
        val isCurrentDaySelected =
            typedArray.getBoolean(R.styleable.CalendarView_currentDaySelected, true)

        return ViewAttrs(
            monthTextColor = monthTextColor,
            dayOfWeekTextColor = dayOfWeekTextColor,
            todayTextColor = todayTextColor,
            dayTextColor = dayTextColor,
            saturdayTextColor = saturdayTextColor,
            sundayTextColor = sundayTextColor,
            disabledDayColor = disableDayColor,
            selectedDayBgColor = selectedDayBgColor,
            selectedDayTextColor = selectedDayTextColor,
            monthHeight = monthHeight,
            dayOfWeekHeight = dayOfWeekHeight,
            dayHeight = dayHeight,
            monthTextSize = monthTextSize,
            dayOfWeekTextSize = dayOfWeekTextSize,
            dayTextSize = dayTextSize,
            monthSpacing = monthSpacing,
            padding = padding,
            selectedCircleSize = selectedCircleSize,
            isCurrentDaySelected = isCurrentDaySelected
        )
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        if (adapter != null && adapter !is CalendarViewAdapter) {
            throw UnsupportedOperationException("adapter should be CalendarViewAdapter")
        }
        super.setAdapter(adapter)
    }

    override fun setLayoutManager(layout: LayoutManager?) {
        if (layout != null &&
            (layout !is LinearLayoutManager ||
                    layout.orientation != LinearLayoutManager.VERTICAL)
        ) {
            throw UnsupportedOperationException(
                "layoutManager should be LinearLayoutManager with orientation vertical"
            )
        }
        super.setLayoutManager(layout)
    }

    fun setStartDate(startDate: CalendarDate) {
        calendarAdapter.setStartDate(startDate)
    }

    fun setStartDate(strDate: String) {
        parseStringDate(strDate)?.let { calendarAdapter.setStartDate(it) }
    }

    fun setEndDate(endDate: CalendarDate) {
        calendarAdapter.setEndDate(endDate)
    }

    fun setEndDate(strDate: String) {
        parseStringDate(strDate)?.let { calendarAdapter.setEndDate(it) }
    }

    fun getSelectedDays(): SelectedDates? {
        return calendarAdapter.selectedDates
    }

    companion object {
        const val MONTHS_IN_YEAR = 12
    }
}