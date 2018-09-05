package com.daewonjung.calendarview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Paint.Style
import android.graphics.Rect
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import com.daewonjung.calendarview.Utils.checkSelectLimitDayExceed
import java.text.DateFormatSymbols
import java.util.*


@SuppressLint("ViewConstructor")
class MonthView(
    context: Context,
    private val viewAttrs: ViewAttrs
) : View(context) {

    interface OnDateClickListener {
        fun onDateClicked(calendarDate: CalendarDate)
        fun onSelectLimitDayExceed(startDate: CalendarDate, endDate: CalendarDate, limit: Int)
    }

    private var viewWidth: Int = 0

    private val monthTitlePaint by lazy {
        Paint().apply {
            isFakeBoldText = false
            isAntiAlias = true
            textSize = viewAttrs.monthTextSize.toFloat()
            color = viewAttrs.monthTextColor
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private val dayOfWeekPaint by lazy {
        Paint().apply {
            isFakeBoldText = false
            isAntiAlias = true
            textSize = viewAttrs.dayOfWeekTextSize.toFloat()
            color = viewAttrs.dayOfWeekTextColor
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private val normalDayTextPaint by lazy {
        Paint().apply {
            isFakeBoldText = false
            isAntiAlias = true
            textSize = viewAttrs.dayTextSize.toFloat()
            color = viewAttrs.dayTextColor
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private val todayTextPaint by lazy {
        Paint().apply {
            isFakeBoldText = false
            isAntiAlias = true
            textSize = viewAttrs.dayTextSize.toFloat()
            color = viewAttrs.todayTextColor
            style = Style.FILL
            textAlign = Align.CENTER
            typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        }
    }

    private val saturdayTextPaint by lazy {
        Paint().apply {
            isFakeBoldText = false
            isAntiAlias = true
            textSize = viewAttrs.dayTextSize.toFloat()
            color = viewAttrs.saturdayTextColor
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private val sundayTextPaint by lazy {
        Paint().apply {
            isFakeBoldText = false
            isAntiAlias = true
            textSize = viewAttrs.dayTextSize.toFloat()
            color = viewAttrs.sundayTextColor
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private val disabledDayTextPaint by lazy {
        Paint().apply {
            isFakeBoldText = false
            isAntiAlias = true
            textSize = viewAttrs.dayTextSize.toFloat()
            color = viewAttrs.disabledDayColor
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private val selectedDayTextPaint by lazy {
        Paint().apply {
            isFakeBoldText = false
            isAntiAlias = true
            textSize = viewAttrs.dayTextSize.toFloat()
            color = viewAttrs.selectedDayTextColor
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private val selectedCirclePaint by lazy {
        Paint().apply {
            isFakeBoldText = false
            isAntiAlias = true
            color = viewAttrs.selectedDayBgColor
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private var days: List<DayInfo> = emptyList()

    private var year: Int = 0
    private var month: Int = 0

    private var startDate: CalendarDate? = null
    private var endDate: CalendarDate? = null
    private var selectLimitDay: Int? = null

    private var selectedDates: SelectedDates? = null
    private var weekCount = 0
    private var onDateClickListener: OnDateClickListener? = null

    init {
        isClickable = true
        isSaveEnabled = false
    }

    private fun getTextBounds(text: String, paint: Paint): Rect {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        return bounds
    }

    private fun drawMonthTitle(canvas: Canvas) {
        val monthText = getMonthAndYearString(year, month)
        val monthBounds = getTextBounds(monthText, monthTitlePaint)

        val dayOfWeekText =
            DateFormatSymbols().shortWeekdays[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)]
                .toUpperCase(Locale.getDefault())
        val dayOfWeekBounds = getTextBounds(dayOfWeekText, dayOfWeekPaint)

        val dayOfWeekDivision = (viewWidth - viewAttrs.sidePadding * 2) / (DAYS_IN_WEEK * 2)
        val x = (monthBounds.right - monthBounds.left) / 2 + viewAttrs.sidePadding +
                dayOfWeekDivision - (dayOfWeekBounds.right - dayOfWeekBounds.left) / 2
        val y = viewAttrs.monthSpacing +
                (viewAttrs.monthHeight / 2) +
                (monthBounds.bottom - monthBounds.top) / 2

        canvas.drawText(
            monthText,
            x.toFloat(), y.toFloat(),
            monthTitlePaint
        )
    }

    private fun getMonthAndYearString(year: Int, month: Int): String =
        "$year${context.getString(R.string.year)} " +
                "$month${context.getString(R.string.month)}"

    private fun drawDayOfWeek(canvas: Canvas) {
        val dayOfWeekDivision = (viewWidth - viewAttrs.sidePadding * 2) / (DAYS_IN_WEEK * 2)

        val dateFormatSymbols = DateFormatSymbols()
        val dayOfWeekCalendar = Calendar.getInstance()

        for (i in 0 until DAYS_IN_WEEK) {
            val dayOfWeekIndex = (i + WEEK_START) % DAYS_IN_WEEK

            dayOfWeekCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeekIndex)

            val dayOfWeekText = dateFormatSymbols
                .shortWeekdays[dayOfWeekCalendar.get(Calendar.DAY_OF_WEEK)]
                .toUpperCase(Locale.getDefault())
            val bounds = getTextBounds(dayOfWeekText, dayOfWeekPaint)

            val x = (2 * i + 1) * dayOfWeekDivision + viewAttrs.sidePadding
            val y = viewAttrs.monthSpacing +
                    viewAttrs.monthHeight +
                    (viewAttrs.dayOfWeekHeight / 2) +
                    (bounds.bottom - bounds.top) / 2

            val textPaint: Paint = when (getWeekendDayState(i)) {
                DayState.NORMAL -> normalDayTextPaint
                DayState.SATURDAY -> saturdayTextPaint
                DayState.SUNDAY -> sundayTextPaint
                else -> normalDayTextPaint
            }

            canvas.drawText(
                dayOfWeekText,
                x.toFloat(), y.toFloat(),
                textPaint
            )
        }
    }

    private fun isSelectedStartDay(date: CalendarDate): Boolean {
        val start = selectedDates?.start ?: return false
        return date.date == start.date
    }

    private fun isSelectedEndDay(date: CalendarDate): Boolean {
        val end = selectedDates?.end ?: return false
        return date.date == end.date
    }

    private fun isIncludeSelectedDay(date: CalendarDate): Boolean {
        if (selectedDates?.start == null || selectedDates?.end == null) {
            return false
        }

        if (isSelectedStartDay(date) || isSelectedEndDay(date)) {
            return false
        }

        val startMilliSec = selectedDates?.start?.date?.time ?: 0
        val endMilliSec = selectedDates?.end?.date?.time ?: Long.MAX_VALUE
        val time = date.date.time
        if (time in (startMilliSec + 1)..(endMilliSec - 1)) {
            return true
        }

        return false
    }

    private fun getDayState(
        selectedDates: SelectedDates?,
        today: CalendarDate?,
        date: CalendarDate,
        dayOffset: Int
    ): DayState =
        when {
            selectedDates?.start == null && selectedDates?.end == null ->
                if (!isDateSelectable(date)) {
                    DayState.DISABLE
                } else {
                    getSubDayState(today, date, dayOffset)
                }
            selectedDates.end == null ->
                if (isSelectedStartDay(date)) {
                    DayState.FIRST_TOUCH
                } else if (!isDateSelectable(date)) {
                    DayState.DISABLE
                } else {
                    getSubDayState(today, date, dayOffset)
                }
            isSelectedStartDay(date) && isSelectedEndDay(date) -> DayState.ONE_DAY
            isSelectedStartDay(date) -> DayState.START
            isSelectedEndDay(date) -> DayState.END
            isIncludeSelectedDay(date) -> DayState.INCLUDE
            !isDateSelectable(date) -> DayState.DISABLE
            else -> getSubDayState(today, date, dayOffset)
        }

    private fun drawRectBackground(
        canvas: Canvas,
        dayState: DayState,
        x: Int,
        y: Int,
        dayDivision: Int,
        dayOffset: Int,
        dayOfWeekBounds: Rect
    ) {
        when (dayState) {
            DayState.START -> {
                drawDayStateRect(
                    left = x,
                    top = y - viewAttrs.selectedCircleSize,
                    right = x + dayDivision,
                    bottom = y + viewAttrs.selectedCircleSize,
                    paint = selectedCirclePaint,
                    canvas = canvas
                )
            }
            DayState.END -> {
                drawDayStateRect(
                    left = x - dayDivision,
                    top = y - viewAttrs.selectedCircleSize,
                    right = x,
                    bottom = y + viewAttrs.selectedCircleSize,
                    paint = selectedCirclePaint,
                    canvas = canvas
                )
            }
            DayState.INCLUDE -> {
                val left = when (dayOffset) {
                    0 -> x - dayDivision + (dayOfWeekBounds.right - dayOfWeekBounds.left) / 2
                    DAYS_IN_WEEK - 1 -> x - dayDivision
                    else -> x - dayDivision
                }
                val right = when (dayOffset) {
                    0 -> x + dayDivision
                    DAYS_IN_WEEK - 1 ->
                        x + dayDivision - (dayOfWeekBounds.right - dayOfWeekBounds.left) / 2
                    else -> x + dayDivision
                }
                drawDayStateRect(
                    left = left,
                    top = y - viewAttrs.selectedCircleSize,
                    right = right,
                    bottom = y + viewAttrs.selectedCircleSize,
                    paint = selectedCirclePaint,
                    canvas = canvas
                )
            }
            else -> Unit
        }
    }

    private fun drawCircleBackground(
        canvas: Canvas,
        dayState: DayState,
        x: Int,
        y: Int
    ) {
        when (dayState) {
            DayState.FIRST_TOUCH,
            DayState.START,
            DayState.END,
            DayState.ONE_DAY ->
                canvas.drawCircle(
                    x.toFloat(),
                    y.toFloat(),
                    viewAttrs.selectedCircleSize.toFloat(),
                    selectedCirclePaint
                )
            else -> Unit
        }
    }

    private fun getDayTextPaint(dayState: DayState): Paint =
        when (dayState) {
            DayState.FIRST_TOUCH,
            DayState.START,
            DayState.END,
            DayState.INCLUDE,
            DayState.ONE_DAY ->
                selectedDayTextPaint
            DayState.NORMAL ->
                normalDayTextPaint
            DayState.TODAY ->
                todayTextPaint
            DayState.SATURDAY ->
                saturdayTextPaint
            DayState.SUNDAY ->
                sundayTextPaint
            DayState.DISABLE ->
                disabledDayTextPaint
        }

    private fun drawDay(canvas: Canvas) {
        val yOffset = viewAttrs.monthSpacing +
                viewAttrs.monthHeight +
                viewAttrs.dayOfWeekHeight +
                (viewAttrs.dayHeight / 2)

        val dayDivision = (viewWidth - viewAttrs.sidePadding * 2) / (DAYS_IN_WEEK * 2)

        val dayOfWeekText = DateFormatSymbols()
            .shortWeekdays[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)]
            .toUpperCase(Locale.getDefault())
        val dayOfWeekBounds = Rect()
        dayOfWeekPaint.getTextBounds(dayOfWeekText, 0, dayOfWeekText.length, dayOfWeekBounds)

        days.forEach { dayInfo ->
            val x = dayDivision * (1 + dayInfo.dayOffset * 2) + viewAttrs.sidePadding
            val y = yOffset + viewAttrs.dayHeight * dayInfo.weekOffset

            drawRectBackground(
                canvas,
                dayInfo.state,
                x,
                y,
                dayDivision,
                dayInfo.dayOffset,
                dayOfWeekBounds
            )
            drawCircleBackground(canvas, dayInfo.state, x, y)

            val datText = dayInfo.date.day.toString()
            val textPaint = getDayTextPaint(dayInfo.state)
            val bounds = getTextBounds(datText, textPaint)
            canvas.drawText(
                datText,
                x.toFloat(),
                (y + (bounds.bottom - bounds.top) / 2).toFloat(),
                textPaint
            )
        }
    }

    private fun drawDayStateRect(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        paint: Paint,
        canvas: Canvas
    ) {
        canvas.drawRect(
            left.toFloat(),
            top.toFloat(),
            right.toFloat(),
            bottom.toFloat(),
            paint
        )
    }

    private fun getSubDayState(today: CalendarDate?, date: CalendarDate, dayOffset: Int): DayState =
        if (today == date) {
            DayState.TODAY
        } else {
            getWeekendDayState(dayOffset)
        }

    private fun getWeekendDayState(dayOffset: Int): DayState {
        return when (dayOffset) {
            0 -> DayState.SUNDAY
            DAYS_IN_WEEK - 1 -> DayState.SATURDAY
            else -> DayState.NORMAL
        }
    }

    private fun isDateSelectable(date: CalendarDate): Boolean {
        val startDate = this.startDate
        if (startDate != null &&
            startDate.year == date.year &&
            startDate.month == date.month &&
            startDate.day > date.day
        ) {
            return false
        }

        val endDate = this.endDate
        if (endDate != null &&
            endDate.year == date.year &&
            endDate.month == date.month &&
            endDate.day < date.day
        ) {
            return false
        }

        val selectedDates = this.selectedDates
        val selectLimitDay = this.selectLimitDay
        if (selectLimitDay != null &&
            selectedDates != null &&
            selectedDates.start != null &&
            selectedDates.end == null &&
            Utils.checkSelectLimitDayExceed(selectLimitDay, selectedDates.start, date)
        ) {
            return false
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        drawMonthTitle(canvas)
        drawDayOfWeek(canvas)
        drawDay(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            View.MeasureSpec.getSize(widthMeasureSpec),
            viewAttrs.monthSpacing +
                    viewAttrs.monthHeight +
                    viewAttrs.dayOfWeekHeight +
                    viewAttrs.dayHeight * weekCount
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        viewWidth = w
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            getDateFromPosition(event.x, event.y)?.let {
                onDateClick(it)
            }
        }
        return true
    }

    private fun getDateFromPosition(x: Float, y: Float): CalendarDate? {
        if (x < viewAttrs.sidePadding || x > viewWidth - viewAttrs.sidePadding) {
            return null
        }

        val yDay = (y - viewAttrs.monthSpacing - viewAttrs.monthHeight - viewAttrs.dayOfWeekHeight)
            .toInt() / viewAttrs.dayHeight
        val initialDayOffset = days.firstOrNull()?.dayOffset ?: return null
        val dayIndex = (((x - viewAttrs.sidePadding) * DAYS_IN_WEEK /
                (viewWidth - viewAttrs.sidePadding - viewAttrs.sidePadding)).toInt() - initialDayOffset) +
                yDay * DAYS_IN_WEEK

        return days.getOrNull(dayIndex)?.date
    }

    private fun onDateClick(date: CalendarDate) {
        val selectLimitDay = this.selectLimitDay
        val selectedDates = this.selectedDates
        if (isDateSelectable(date)) {
            onDateClickListener?.onDateClicked(date)
        } else if (selectLimitDay != null &&
            selectedDates != null &&
            selectedDates.start != null &&
            selectedDates.end == null &&
            checkSelectLimitDayExceed(selectLimitDay, selectedDates.start, date)
        ) {
            onDateClickListener?.onSelectLimitDayExceed(selectedDates.start, date, selectLimitDay)
        }
    }

    private fun getDayOffset(dayOfWeekOffset: Int): Int {
        val offset = if (dayOfWeekOffset < WEEK_START) {
            dayOfWeekOffset + DAYS_IN_WEEK
        } else {
            dayOfWeekOffset
        }

        return offset - WEEK_START
    }

    fun setMonthParams(
        year: Int,
        month: Int /* 1 - 12 */,
        selectedDates: SelectedDates,
        startDate: CalendarDate?,
        endDate: CalendarDate?,
        selectLimitDay: Int?,
        todaySelected: Boolean
    ) {
        if (selectLimitDay != null &&
            selectedDates.start != null &&
            selectedDates.end != null &&
            checkSelectLimitDayExceed(selectLimitDay, selectedDates.start, selectedDates.end)
        ) {
            throw IllegalArgumentException("selected dates exceed select limit day")
        }

        this@MonthView.year = year
        this@MonthView.month = month
        this@MonthView.selectedDates = selectedDates
        this@MonthView.startDate = startDate
        this@MonthView.endDate = endDate
        this@MonthView.selectLimitDay = selectLimitDay

        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        val dayOfWeekOffset = calendar.get(Calendar.DAY_OF_WEEK)

        val dayCount = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        var dayOffset = getDayOffset(dayOfWeekOffset)
        var weekOffset = 0
        val today = if (todaySelected) {
            Calendar.getInstance().let {
                CalendarDate(
                    it.get(Calendar.YEAR),
                    it.get(Calendar.MONTH) + 1,
                    it.get(Calendar.DAY_OF_MONTH)
                )
            }
        } else {
            null
        }

        days = (1..dayCount).map { day ->
            val date = CalendarDate(year, month, day)
            val dayData = DayInfo(
                date,
                getDayState(selectedDates, today, date, dayOffset),
                dayOffset,
                weekOffset
            )

            ++dayOffset
            if (dayOffset == DAYS_IN_WEEK) {
                dayOffset = 0
                ++weekOffset
            }
            return@map dayData
        }
        weekCount = if (days.isNotEmpty()) weekOffset + 1 else 0

        invalidate()
    }

    fun setOnDateClickListener(onDateClickListener: MonthView.OnDateClickListener) {
        this.onDateClickListener = onDateClickListener
    }

    private data class DayInfo(
        val date: CalendarDate,
        val state: DayState,
        val dayOffset: Int,
        val weekOffset: Int
    )

    companion object {
        private const val DAYS_IN_WEEK = 7
        private const val WEEK_START = 1
    }
}