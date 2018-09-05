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

    private val internalDateSelectListener = object : DateSelectListener {

        override fun onDateSelected(date: CalendarDate) {
            dateSelectListener?.onDateSelected(date)
        }

        override fun onDateRangeSelected(start: CalendarDate, end: CalendarDate) {
            dateSelectListener?.onDateRangeSelected(start, end)
        }

        override fun onSelectLimitDayExceed(
            start: CalendarDate,
            end: CalendarDate,
            selectLimitDay: Int
        ) {
            dateSelectListener?.onSelectLimitDayExceed(start, end, selectLimitDay)
        }
    }

    var dateSelectListener: DateSelectListener? = null

    var startDate: CalendarDate?
        get() = calendarAdapter.startDate
        set(value) {
            calendarAdapter.startDate = value
        }

    var endDate: CalendarDate?
        get() = calendarAdapter.endDate
        set(value) {
            calendarAdapter.endDate = value
        }

    var selectLimitDay: Int?
        get() = calendarAdapter.selectLimitDay
        set(value) {
            calendarAdapter.selectLimitDay = value
        }

    var todaySelected: Boolean
        get() = calendarAdapter.todaySelected
        set(value) {
            calendarAdapter.todaySelected = value
        }

    var selectedDates: SelectedDates
        get() = calendarAdapter.selectedDates
        set(value) {
            calendarAdapter.selectedDates = value
        }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CalendarView)
        val startDate = parseStringDate(typedArray.getString(R.styleable.CalendarView_startDate))
        val endDate = parseStringDate(typedArray.getString(R.styleable.CalendarView_endDate))
        val selectLimitDay =
            typedArray.getInt(R.styleable.CalendarView_selectLimitDay, 0).takeIf { it > 0 }
        val todaySelected = typedArray.getBoolean(R.styleable.CalendarView_todaySelected, true)

        val viewAttrs = initViewAttrs(typedArray)
        typedArray.recycle()

        calendarAdapter = CalendarViewAdapter(
            context,
            internalDateSelectListener,
            startDate,
            endDate,
            selectLimitDay,
            todaySelected,
            viewAttrs
        )

        layoutManager = LinearLayoutManager(context)
        adapter = calendarAdapter
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        scrollInitPosition(startDate, endDate)
    }

    private fun scrollInitPosition(startDate: CalendarDate?, endDate: CalendarDate?) {
        val calendar = Calendar.getInstance()

        val curr = calendar.time
        val start = startDate?.date
        val end = endDate?.date

        val position = if ((start == null || curr.after(start)) &&
            (end == null || curr.before(end))
        ) {

            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)

            val absPosition = currentYear * MONTHS_IN_YEAR + currentMonth
            if (startDate == null) {
                absPosition
            } else {
                absPosition - (startDate.year * MONTHS_IN_YEAR + startDate.month - 1)
            }
        } else if (end != null && curr.after(end)) {
            endDate.year * MONTHS_IN_YEAR + endDate.month - 1
        } else {
            return
        }
        (layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
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
                    calendar.get(Calendar.MONTH) + 1,
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
            R.styleable.CalendarView_colorDisabledDayText,
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

        val monthHeight = typedArray.getDimensionPixelSize(
            R.styleable.CalendarView_monthHeight,
            resources.getDimensionPixelSize(R.dimen.month_height)
        )
        val dayOfWeekHeight = typedArray.getDimensionPixelSize(
            R.styleable.CalendarView_dayOfWeekHeight,
            resources.getDimensionPixelSize(R.dimen.day_of_week_height)
        )
        val dayHeight = typedArray.getDimensionPixelSize(
            R.styleable.CalendarView_dayHeight,
            resources.getDimensionPixelSize(R.dimen.day_height)
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
            R.styleable.CalendarView_sidePadding,
            resources.getDimensionPixelOffset(R.dimen.sidePadding)
        )

        val selectedCircleSize = typedArray.getDimensionPixelSize(
            R.styleable.CalendarView_selectedDayRadius,
            resources.getDimensionPixelOffset(R.dimen.selected_circle_size)
        )

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
            sidePadding = padding,
            selectedCircleSize = selectedCircleSize
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

    companion object {
        const val MONTHS_IN_YEAR = 12
    }
}