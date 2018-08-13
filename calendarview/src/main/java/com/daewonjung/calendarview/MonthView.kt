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

    private val monthTitlePaint: Paint
    private lateinit var dayOfWeekPaint: Paint
    private lateinit var dayPaint: Paint
    private lateinit var selectedCirclePaint: Paint

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

    private lateinit var selectedDates: SelectedDates

    private var numRows = 0

    private var onDateClickListener: OnDateClickListener? = null


    init {


        isClickable = true

        monthTitlePaint = Paint()
        monthTitlePaint.isFakeBoldText = false
        monthTitlePaint.isAntiAlias = true
        monthTitlePaint.textSize = viewAttrs.monthTextSize.toFloat()
        monthTitlePaint.color = viewAttrs.monthTextColor
        monthTitlePaint.style = Style.FILL
        monthTitlePaint.textAlign = Align.CENTER

        dayOfWeekPaint = Paint()
        dayOfWeekPaint.isFakeBoldText = false
        dayOfWeekPaint.isAntiAlias = true
        dayOfWeekPaint.textSize = viewAttrs.dayOfWeekTextSize.toFloat()
        dayOfWeekPaint.color = viewAttrs.dayOfWeekTextColor
        dayOfWeekPaint.style = Style.FILL
        dayOfWeekPaint.textAlign = Align.CENTER

        dayPaint = Paint()
        dayPaint.isFakeBoldText = false
        dayPaint.isAntiAlias = true
        dayPaint.textSize = viewAttrs.dayTextSize.toFloat()
        dayPaint.color = viewAttrs.dayTextColor
        dayPaint.style = Style.FILL
        dayPaint.textAlign = Align.CENTER

        selectedCirclePaint = Paint()
        selectedCirclePaint.isFakeBoldText = false
        selectedCirclePaint.isAntiAlias = true
        selectedCirclePaint.color = viewAttrs.selectedDayBgColor
        selectedCirclePaint.style = Style.FILL
        selectedCirclePaint.textAlign = Align.CENTER
    }

    private fun calculateNumRows(numCells: Int): Int {
        val offset = getDayOffset()
        return (offset + numCells) / Constants.DAYS_IN_WEEK + if ((offset + numCells) % Constants.DAYS_IN_WEEK > 0) 1 else 0
    }

    private fun getDayOffset(): Int {
        return (if (dayOfWeekOffset < weekStart) dayOfWeekOffset + Constants.DAYS_IN_WEEK else dayOfWeekOffset) - weekStart
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
            DateFormatSymbols().shortWeekdays[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)].toUpperCase(
                Locale.getDefault()
            )
        val dayOfWeekBounds = getTextBounds(dayOfWeekText, dayOfWeekPaint)

//        가운데 정렬
//        val x = (viewWidth - viewAttrs.padding * 2) / 2
        val dayOfWeekDivision = (viewWidth - viewAttrs.padding * 2) / (Constants.DAYS_IN_WEEK * 2)
        val x = (monthBounds.right - monthBounds.left) / 2 + viewAttrs.padding +
                dayOfWeekDivision - (dayOfWeekBounds.right - dayOfWeekBounds.left) / 2
        val y =
            viewAttrs.monthSpacing + (viewAttrs.monthHeight / 2) + (monthBounds.bottom - monthBounds.top) / 2

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
        val dayOfWeekDivision = (viewWidth - viewAttrs.padding * 2) / (Constants.DAYS_IN_WEEK * 2)

        val dateFormatSymbols = DateFormatSymbols()
        val dayOfWeekCalendar = Calendar.getInstance()

        for (i in 0 until Constants.DAYS_IN_WEEK) {
            val dayOfWeekIndex = (i + weekStart) % Constants.DAYS_IN_WEEK

            dayOfWeekCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeekIndex)

            val dayOfWeekText = dateFormatSymbols
                .shortWeekdays[dayOfWeekCalendar.get(Calendar.DAY_OF_WEEK)].toUpperCase(Locale.getDefault())
            val bounds = getTextBounds(dayOfWeekText, dayOfWeekPaint)

            val x = (2 * i + 1) * dayOfWeekDivision + viewAttrs.padding
            val y =
                viewAttrs.monthSpacing + viewAttrs.monthHeight + (viewAttrs.dayOfWeekHeight / 2) + (bounds.bottom - bounds.top) / 2

            drawWeekendDayColor(dayOfWeekPaint, i)

            canvas.drawText(
                dayOfWeekText,
                x.toFloat(), y.toFloat(),
                dayOfWeekPaint
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
        ).timeInMillis == selectedDates.start?.createCalendar()?.timeInMillis ?: -1
    }

    private fun isSelectedEndDay(year: Int, month: Int, day: Int): Boolean {
        return getCalendarWithoutTime(
            year,
            month,
            day
        ).timeInMillis == selectedDates.end?.createCalendar()?.timeInMillis ?: -1
    }

    private fun isIncludeSelectedDay(year: Int, month: Int, day: Int): Boolean {
        if (selectedDates.start == null || selectedDates.end == null) {
            return false
        }

        if (isSelectedStartDay(year, month, day) || isSelectedEndDay(year, month, day)) {
            return false
        }

        if ((getCalendarWithoutTime(
                year,
                month,
                day
            ).timeInMillis > selectedDates.start?.createCalendar()?.timeInMillis ?: 0)
            && (getCalendarWithoutTime(
                year,
                month,
                day
            ).timeInMillis < selectedDates.end?.createCalendar()?.timeInMillis ?: Long.MAX_VALUE)
        ) {
            return true
        }

        return false
    }

    private fun getDayState(year: Int, month: Int, day: Int): DayState {
        if (selectedDates.start == null && selectedDates.end == null) {
            return if (!isEnableDay(year, month, day)) {
                DayState.DISABLE
            } else {
                DayState.NONE
            }
        } else if (selectedDates.end == null) {
            return if (isSelectedStartDay(year, month, day)) {
                DayState.FIRST_TOUCH
            } else if (!isEnableDay(year, month, day)) {
                DayState.DISABLE
            } else {
                DayState.NONE
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
                DayState.NONE
            }
        }
    }

    private fun drawDay(canvas: Canvas) {
        var y =
            viewAttrs.monthSpacing + viewAttrs.monthHeight + viewAttrs.dayOfWeekHeight + (viewAttrs.dayHeight / 2)
        val dayDivision = (viewWidth - viewAttrs.padding * 2) / (Constants.DAYS_IN_WEEK * 2)
        var dayOffset = getDayOffset()
        var day = 1

        val dayOfWeekText = DateFormatSymbols()
            .shortWeekdays[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)].toUpperCase(Locale.getDefault())
        val dayOfWeekBounds = Rect()
        dayOfWeekPaint.getTextBounds(dayOfWeekText, 0, dayOfWeekText.length, dayOfWeekBounds)

        while (day <= numCells) {
            val x = dayDivision * (1 + dayOffset * 2) + viewAttrs.padding

            val textPaint: Paint
            val dayState = getDayState(year, month, day)
//            drawBackground(canvas, dayState)
//            drawText()
            when (getDayState(year, month, day)) {
                DayState.FIRST_TOUCH -> {
                    dayPaint.color = viewAttrs.selectedDayTextColor
                    canvas.drawCircle(
                        x.toFloat(),
                        y.toFloat(),
                        viewAttrs.selectedCircleSize.toFloat(),
                        selectedCirclePaint
                    )
                }
                DayState.START -> {
                    dayPaint.color = viewAttrs.selectedDayTextColor
                    drawDayStateRect(
                        left = x,
                        top = y - viewAttrs.selectedCircleSize,
                        right = x + dayDivision,
                        bottom = y + viewAttrs.selectedCircleSize,
                        paint = selectedCirclePaint,
                        canvas = canvas
                    )
                    canvas.drawCircle(
                        x.toFloat(),
                        y.toFloat(),
                        viewAttrs.selectedCircleSize.toFloat(),
                        selectedCirclePaint
                    )
                }
                DayState.END -> {
                    dayPaint.color = viewAttrs.selectedDayTextColor
                    drawDayStateRect(
                        left = x - dayDivision,
                        top = y - viewAttrs.selectedCircleSize,
                        right = x,
                        bottom = y + viewAttrs.selectedCircleSize,
                        paint = selectedCirclePaint,
                        canvas = canvas
                    )
                    canvas.drawCircle(
                        x.toFloat(),
                        y.toFloat(),
                        viewAttrs.selectedCircleSize.toFloat(),
                        selectedCirclePaint
                    )
                }
                DayState.INCLUDE -> {
                    dayPaint.color = viewAttrs.selectedDayTextColor
                    val left = when (dayOffset) {
                        0 -> x - dayDivision + (dayOfWeekBounds.right - dayOfWeekBounds.left) / 2
                        Constants.DAYS_IN_WEEK - 1 -> x - dayDivision
                        else -> x - dayDivision
                    }
                    val right = when (dayOffset) {
                        0 -> x + dayDivision
                        Constants.DAYS_IN_WEEK - 1 -> x + dayDivision - (dayOfWeekBounds.right - dayOfWeekBounds.left) / 2
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
                DayState.ONE_DAY -> {
                    dayPaint.color = viewAttrs.selectedDayTextColor
                    canvas.drawCircle(
                        x.toFloat(),
                        y.toFloat(),
                        viewAttrs.selectedCircleSize.toFloat(),
                        selectedCirclePaint
                    )
                }
                DayState.DISABLE -> {
                    drawDisableDayColor(dayPaint, year, month, day)
                }
                DayState.NONE -> {
                    drawNormalDayColor(dayPaint, year, month, day, dayOffset)
                }
            }

            val datText = day.toString()
            val bounds = getTextBounds(datText, dayPaint)
            canvas.drawText(
                datText,
                x.toFloat(),
                (y + (bounds.bottom - bounds.top) / 2).toFloat(),
                dayPaint
            )

            dayOffset++

            if (dayOffset == Constants.DAYS_IN_WEEK) {
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

    private fun drawDisableDayColor(paint: Paint, year: Int, month: Int, day: Int) {
        if (!isEnableDay(year, month, day)) {
            paint.color = viewAttrs.disableDayColor
        }
    }

    private fun drawNormalDayColor(paint: Paint, year: Int, month: Int, day: Int, dayOffset: Int) {
        if (today == day
            && todayCalendar.get(Calendar.YEAR) == year
            && todayCalendar.get(Calendar.MONTH) == month
        ) {
            paint.color = viewAttrs.todayTextColor
            paint.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        } else {
            drawWeekendDayColor(paint, dayOffset)
            paint.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        }
    }

    private fun drawWeekendDayColor(paint: Paint, dayOffset: Int) {
        when (dayOffset) {
            0 -> paint.color = viewAttrs.sundayTextColor
            Constants.DAYS_IN_WEEK - 1 -> paint.color = viewAttrs.saturdayTextColor
            else -> paint.color = viewAttrs.dayTextColor
        }
    }

    private fun isEnableDay(year: Int, month: Int, day: Int): Boolean {
        if (startDate?.year == year && startDate?.month == month && (startDate?.day) ?: Int.MAX_VALUE > day) {
            return false
        }

        if (endDate?.year == year && endDate?.month == month && (endDate?.day) ?: Int.MAX_VALUE < day) {
            return false
        }

        if (selectLimitDay != Int.MAX_VALUE && selectedDates.start != null && selectedDates.end == null) {
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val diffDay =
                Math.abs(selectedDates.start!!.createCalendar().timeInMillis - calendar.timeInMillis) / 1000 / 60 / 60 / 24
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
            viewAttrs.monthSpacing + viewAttrs.monthHeight + viewAttrs.dayOfWeekHeight + viewAttrs.dayHeight * numRows
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
            (y - viewAttrs.monthSpacing - viewAttrs.monthHeight - viewAttrs.dayOfWeekHeight).toInt() / viewAttrs.dayHeight
        val day =
            1 + (((x - viewAttrs.padding) * Constants.DAYS_IN_WEEK / (viewWidth - viewAttrs.padding - viewAttrs.padding)).toInt() - getDayOffset()) + yDay * Constants.DAYS_IN_WEEK

        return if (month > 11 || month < 0 || Utils.getDaysInMonth(
                year,
                month
            ) < day || day < 1
        ) null else day
    }

    private fun onDayClick(day: Int) {
        if (isEnableDay(year, month, day)) {
            onDateClickListener?.onDateClicked(CalendarDate(year, month, day))
        } else if (selectLimitDay != Int.MAX_VALUE && selectedDates.start != null && selectedDates.end == null) {
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
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        dayOfWeekOffset = calendar.get(Calendar.DAY_OF_WEEK)

//        weekStart = if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
//            params[VIEW_PARAMS_WEEK_START]!!
//        } else {
//            dayOfWeekOffset
//        }

        numCells = Utils.getDaysInMonth(year, month)
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
}