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

    private var scrollToInitialPosition = true

    private val internalDateSelectListener = object : DateSelectListener {

        override fun onSelectedDatesChanged(start: CalendarDate?, end: CalendarDate?) {
            dateSelectListener?.onSelectedDatesChanged(start, end)
        }

        override fun onSelectLimitExceed(
            start: CalendarDate,
            end: CalendarDate,
            selectType: ViewState.SelectType,
            limit: Int
        ) {
            dateSelectListener?.onSelectLimitExceed(start, end, selectType, limit)
        }
    }

    var dateSelectListener: DateSelectListener? = null

    var viewState: ViewState
        get() = calendarAdapter.viewState
        set(value) {
            calendarAdapter.viewState = value
        }

    var startDate: CalendarDate?
        get() = viewState.startDate
        set(value) {
            viewState = viewState.copy(
                startDate = value,
                selectedDates = SelectedDates(null, null)
            )
        }

    var endDate: CalendarDate?
        get() = viewState.endDate
        set(value) {
            viewState = viewState.copy(
                endDate = value,
                selectedDates = SelectedDates(null, null)
            )
        }

    var todaySelected: Boolean
        get() = viewState.todaySelected
        set(value) {
            viewState = viewState.copy(todaySelected = value)
        }

    var selectType: ViewState.SelectType
        get() = viewState.selectType
        set(value) {
            viewState = viewState.copy(
                selectType = value,
                selectedDates = SelectedDates(null, null)
            )
        }

    var selectedDates: SelectedDates
        get() = viewState.selectedDates
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
            viewState = viewState.copy(selectedDates = value)
        }

    var dotList: List<Date>? = null
        set(value) {
            calendarAdapter.dotList = value
        }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CalendarView)
        val startDate = parseStringDate(typedArray.getString(R.styleable.CalendarView_startDate))
        val endDate = parseStringDate(typedArray.getString(R.styleable.CalendarView_endDate))
        val todaySelected = typedArray.getBoolean(R.styleable.CalendarView_todaySelected, true)
        val selectTypeAttr = typedArray.getInteger(R.styleable.CalendarView_selectType, 0)
        val selectLimitDay =
            typedArray.getInt(R.styleable.CalendarView_selectLimitDay, 0).takeIf { it > 0 }
        val selectLimitWeek =
            typedArray.getInt(R.styleable.CalendarView_selectLimitWeek, 0).takeIf { it > 0 }
        val selectLimitMonth =
            typedArray.getInt(R.styleable.CalendarView_selectLimitMonth, 0).takeIf { it > 0 }

        val viewAttrs = initViewAttrs(typedArray)
        typedArray.recycle()

        val selectType = when (selectTypeAttr) {
            0 -> ViewState.SelectType.OneDay
            1 -> ViewState.SelectType.DayRange(selectLimitDay)
            2 -> ViewState.SelectType.WeekRange(selectLimitWeek)
            3 -> ViewState.SelectType.MonthRange(selectLimitMonth)
            else -> throw IllegalArgumentException("unknown selectType attribute : $selectTypeAttr")
        }

        calendarAdapter = CalendarViewAdapter(
            context,
            internalDateSelectListener,
            ViewState(startDate, endDate, todaySelected, selectType),
            dotList,
            viewAttrs
        )

        layoutManager = LinearLayoutManager(context)
        adapter = calendarAdapter
    }

    // TODO
//    fun setDotListDelegate(delegate: (Month, (List<Date>) -> Unit) ->  Unit) {
//
//    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (scrollToInitialPosition && scrollToInitPosition()) {
            scrollToInitialPosition = false
        }
    }

    private fun scrollToInitPosition(): Boolean {
        val calendar = Calendar.getInstance()

        val curr = CalendarDate.create(calendar.time)
        val start = this.startDate
        val end = this.endDate

        val date = selectedDates.start
            ?: selectedDates.end
            ?: when {
                start != null && curr.date.before(start.date) -> start
                end != null && curr.date.after(end.date) -> end
                else -> curr
            }
        return scrollToDate(date)
    }

    private fun scrollToDate(date: CalendarDate): Boolean {
        val position = calendarAdapter.getPositionOfDate(date)
        if (position != null) {
            scrollToInitialPosition = false
            (layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
            return true
        }
        return false
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
            resources.getDimensionPixelSize(R.dimen.monthSpacing)
        )
        val padding = typedArray.getDimensionPixelSize(
            R.styleable.CalendarView_sidePadding,
            resources.getDimensionPixelSize(R.dimen.sidePadding)
        )

        val selectedCircleSize = typedArray.getDimensionPixelSize(
            R.styleable.CalendarView_selectedDayRadius,
            resources.getDimensionPixelSize(R.dimen.selected_circle_size)
        )

        val dotRadius = typedArray.getDimensionPixelSize(
            R.styleable.CalendarView_dotRadius,
            resources.getDimensionPixelSize(R.dimen.dot_radius)
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
            selectedCircleSize = selectedCircleSize,
            dotRadius = dotRadius
        )
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        if (adapter != null && adapter !is CalendarViewAdapter) {
            throw UnsupportedOperationException("adapter should be CalendarViewAdapter")
        }
        super.setAdapter(adapter)
    }

    override fun setLayoutManager(layout: RecyclerView.LayoutManager?) {
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