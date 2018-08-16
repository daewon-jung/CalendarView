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
import java.text.DateFormatSymbols
import java.util.*


@SuppressLint("ViewConstructor")
class MonthView(
    context: Context,
    private val viewAttrs: ViewAttrs
) : View(context) {

    interface OnDateClickListener {
        fun onDateClicked(calendarDate: CalendarDate)
        fun onInvalidDateClicked(calendarDate: CalendarDate)
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

    private var weekStart = 1
    private var numCells = 0
    private var dayOfWeekOffset = 0

    private var year: Int = 0
    private var month: Int = 0
    private var today: Int = 0

    private var startDate: CalendarDate? = null
    private var endDate: CalendarDate? = null
    private var selectLimitDay: Int = 0

    private val todayCalendar: Calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private var selectedDates: SelectedDates? = null
    private var numRows = 0
    private var onDateClickListener: OnDateClickListener? = null

    init {
        isClickable = true
        isSaveEnabled = false
    }

    private fun calculateNumRows(numCells: Int): Int {
        val offset = getDayOffset()

        return (offset + numCells) / DAYS_IN_WEEK +
                if ((offset + numCells) % DAYS_IN_WEEK > 0)
                    1
                else
                    0
    }

    private fun getDayOffset(): Int {
        val offset = if (dayOfWeekOffset < weekStart)
            dayOfWeekOffset + DAYS_IN_WEEK
        else
            dayOfWeekOffset

        return offset - weekStart
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

//        가운데 정렬
//        val x = (viewWidth - viewAttrs.padding * 2) / 2
        val dayOfWeekDivision = (viewWidth - viewAttrs.padding * 2) / (DAYS_IN_WEEK * 2)
        val x = (monthBounds.right - monthBounds.left) / 2 + viewAttrs.padding +
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

    private fun getMonthAndYearString(year: Int, month: Int): String {
        return year.toString() + context.getString(R.string.year) + " " + (month + 1) + context.getString(
            R.string.month
        )
    }

    private fun drawDayOfWeek(canvas: Canvas) {
        val dayOfWeekDivision = (viewWidth - viewAttrs.padding * 2) / (DAYS_IN_WEEK * 2)

        val dateFormatSymbols = DateFormatSymbols()
        val dayOfWeekCalendar = Calendar.getInstance()

        for (i in 0 until DAYS_IN_WEEK) {
            val dayOfWeekIndex = (i + weekStart) % DAYS_IN_WEEK

            dayOfWeekCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeekIndex)

            val dayOfWeekText = dateFormatSymbols
                .shortWeekdays[dayOfWeekCalendar.get(Calendar.DAY_OF_WEEK)]
                .toUpperCase(Locale.getDefault())
            val bounds = getTextBounds(dayOfWeekText, dayOfWeekPaint)

            val x = (2 * i + 1) * dayOfWeekDivision + viewAttrs.padding
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

    private fun getCalendarWithoutTime(year: Int, month: Int, day: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar
    }

    private fun isSelectedStartDay(year: Int, month: Int, day: Int): Boolean {
        return getCalendarWithoutTime(
            year,
            month,
            day
        ).timeInMillis == selectedDates?.start?.createCalendar()?.timeInMillis ?: -1
    }

    private fun isSelectedEndDay(year: Int, month: Int, day: Int): Boolean {
        return getCalendarWithoutTime(
            year,
            month,
            day
        ).timeInMillis == selectedDates?.end?.createCalendar()?.timeInMillis ?: -1
    }

    private fun isIncludeSelectedDay(year: Int, month: Int, day: Int): Boolean {
        if (selectedDates?.start == null || selectedDates?.end == null) {
            return false
        }

        if (isSelectedStartDay(year, month, day) || isSelectedEndDay(year, month, day)) {
            return false
        }

        if ((getCalendarWithoutTime(
                year,
                month,
                day
            ).timeInMillis > selectedDates?.start?.createCalendar()?.timeInMillis ?: 0)
            && (getCalendarWithoutTime(
                year,
                month,
                day
            ).timeInMillis < selectedDates?.end?.createCalendar()?.timeInMillis ?: Long.MAX_VALUE)
        ) {
            return true
        }

        return false
    }

    private fun getDayState(year: Int, month: Int, day: Int, dayOffset: Int): DayState {
        if (selectedDates?.start == null && selectedDates?.end == null) {
            return if (!isEnableDay(year, month, day)) {
                DayState.DISABLE
            } else {
                getSubDayState(year, month, day, dayOffset)
            }
        } else if (selectedDates?.end == null) {
            return if (isSelectedStartDay(year, month, day)) {
                DayState.FIRST_TOUCH
            } else if (!isEnableDay(year, month, day)) {
                DayState.DISABLE
            } else {
                getSubDayState(year, month, day, dayOffset)
            }
        } else {
            return if (isSelectedStartDay(year, month, day) && isSelectedEndDay(year, month, day)) {
                DayState.ONE_DAY
            } else if (isSelectedStartDay(year, month, day)) {
                DayState.START
            } else if (isSelectedEndDay(year, month, day)) {
                DayState.END
            } else if (isIncludeSelectedDay(year, month, day)) {
                DayState.INCLUDE
            } else if (!isEnableDay(year, month, day)) {
                DayState.DISABLE
            } else {
                getSubDayState(year, month, day, dayOffset)
            }
        }
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
        }
    }

    private fun getDayTextPaint(dayState: DayState): Paint {
        return when (dayState) {
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
    }

    private fun drawDay(canvas: Canvas) {
        var y = viewAttrs.monthSpacing +
                viewAttrs.monthHeight +
                viewAttrs.dayOfWeekHeight +
                (viewAttrs.dayHeight / 2)
        val dayDivision = (viewWidth - viewAttrs.padding * 2) / (DAYS_IN_WEEK * 2)
        var dayOffset = getDayOffset()
        var day = 1

        val dayOfWeekText = DateFormatSymbols()
            .shortWeekdays[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)]
            .toUpperCase(Locale.getDefault())
        val dayOfWeekBounds = Rect()
        dayOfWeekPaint.getTextBounds(dayOfWeekText, 0, dayOfWeekText.length, dayOfWeekBounds)

        while (day <= numCells) {
            val x = dayDivision * (1 + dayOffset * 2) + viewAttrs.padding

            val textPaint: Paint
            val dayState = getDayState(year, month, day, dayOffset)

            drawRectBackground(canvas, dayState, x, y, dayDivision, dayOffset, dayOfWeekBounds)
            drawCircleBackground(canvas, dayState, x, y)
            textPaint = getDayTextPaint(dayState)

            val datText = day.toString()
            val bounds = getTextBounds(datText, textPaint)
            canvas.drawText(
                datText,
                x.toFloat(),
                (y + (bounds.bottom - bounds.top) / 2).toFloat(),
                textPaint
            )

            dayOffset++

            if (dayOffset == DAYS_IN_WEEK) {
                dayOffset = 0
                y += viewAttrs.dayHeight
            }

            day++
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

    private fun getSubDayState(year: Int, month: Int, day: Int, dayOffset: Int): DayState {
        return if (today == day
            && todayCalendar.get(Calendar.YEAR) == year
            && todayCalendar.get(Calendar.MONTH) == month
        ) {
            DayState.TODAY
        } else {
            getWeekendDayState(dayOffset)
        }
    }

    private fun getWeekendDayState(dayOffset: Int): DayState {
        return when (dayOffset) {
            0 -> DayState.SUNDAY
            DAYS_IN_WEEK - 1 -> DayState.SATURDAY
            else -> DayState.NORMAL
        }
    }

    private fun isEnableDay(year: Int, month: Int, day: Int): Boolean {
        if (startDate?.year == year &&
            startDate?.month == month &&
            (startDate?.day) ?: Int.MAX_VALUE > day
        ) {
            return false
        }

        if (endDate?.year == year &&
            endDate?.month == month &&
            (endDate?.day) ?: Int.MAX_VALUE < day
        ) {
            return false
        }

        if (selectLimitDay != Int.MAX_VALUE &&
            selectedDates?.start != null &&
            selectedDates?.end == null
        ) {
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val diffDay =
                Math.abs(
                    selectedDates?.start!!.createCalendar().timeInMillis -
                            calendar.timeInMillis
                ) / 1000 / 60 / 60 / 24
            return diffDay < selectLimitDay
        }

        return true
    }

    private fun isToday(year: Int, month: Int, day: Int): Boolean {
        return getCalendarWithoutTime(year, month, day).timeInMillis == todayCalendar.timeInMillis
    }

    private fun isPrevDay(year: Int, month: Int, day: Int): Boolean {
        return getCalendarWithoutTime(year, month, day).timeInMillis < todayCalendar.timeInMillis
    }

    private fun isNextDay(year: Int, month: Int, day: Int): Boolean {
        return getCalendarWithoutTime(year, month, day).timeInMillis > todayCalendar.timeInMillis
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
                    viewAttrs.dayHeight * numRows
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        viewWidth = w
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            getDayFromPosition(event.x, event.y)?.let {
                onDayClick(it)
            }
        }
        return true
    }

    private fun getDayFromPosition(x: Float, y: Float): Int? {
        if (x < viewAttrs.padding || x > viewWidth - viewAttrs.padding) {
            return null
        }

        val yDay =
            (y - viewAttrs.monthSpacing - viewAttrs.monthHeight - viewAttrs.dayOfWeekHeight)
                .toInt() / viewAttrs.dayHeight
        val day =
            1 + (((x - viewAttrs.padding) * DAYS_IN_WEEK /
                    (viewWidth - viewAttrs.padding - viewAttrs.padding))
                .toInt() - getDayOffset()) + yDay * DAYS_IN_WEEK

        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)

        return if (month > 11
            || month < 0
            || calendar.getActualMaximum(Calendar.DAY_OF_MONTH) < day
            || day < 1
        )
            null
        else
            day
    }

    private fun onDayClick(day: Int) {
        if (isEnableDay(year, month, day)) {
            onDateClickListener?.onDateClicked(CalendarDate(year, month, day))
        } else if (selectLimitDay != Int.MAX_VALUE &&
            selectedDates?.start != null &&
            selectedDates?.end == null
        ) {
            onDateClickListener?.onInvalidDateClicked(CalendarDate(year, month, day))
        }
    }

    fun setMonthParams(
        year: Int, month: Int, selectedDates: SelectedDates,
        startDate: CalendarDate?, endDate: CalendarDate?, selectLimitDay: Int
    ) {
        this@MonthView.year = year
        this@MonthView.month = month
        this@MonthView.selectedDates = selectedDates
        this@MonthView.startDate = startDate
        this@MonthView.endDate = endDate
        this@MonthView.selectLimitDay = selectLimitDay

        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        dayOfWeekOffset = calendar.get(Calendar.DAY_OF_WEEK)

//        weekStart = if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
//            params[VIEW_PARAMS_WEEK_START]!!
//        } else {
//            dayOfWeekOffset
//        }

        numCells = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        numRows = calculateNumRows(numCells)

        for (i in 0 until numCells) {
            val day = i + 1
            if (isToday(year, month, day)) {
                this@MonthView.today = day
            }
        }

        invalidate()
    }

    fun setOnDateClickListener(onDateClickListener: OnDateClickListener) {
        this.onDateClickListener = onDateClickListener
    }

    companion object {
        const val DAYS_IN_WEEK = 7
    }
}