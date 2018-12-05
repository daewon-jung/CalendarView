package com.daewonjung.calendarview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Paint.Style
import android.graphics.Rect
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.view.MotionEvent
import android.view.View
import java.text.DateFormatSymbols
import java.util.*
import java.util.concurrent.TimeUnit


@SuppressLint("ViewConstructor")
class MonthView(
    context: Context,
    private val viewAttrs: ViewAttrs
) : View(context) {

    interface OnDateClickListener {
        fun onDateClicked(calendarDate: CalendarDate)
        fun onSelectLimitExceed(
            startDate: CalendarDate,
            endDate: CalendarDate,
            selectType: ViewState.SelectType,
            limit: Int
        )
    }

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

    private val selectedBackgroundPaint by lazy {
        Paint().apply {
            isFakeBoldText = false
            isAntiAlias = true
            color = viewAttrs.selectedDayBgColor
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private val dotSelectedPaint by lazy {
        Paint().apply {
            isFakeBoldText = false
            isAntiAlias = true
            color = ContextCompat.getColor(context, R.color.white)
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private val dotNotSelectedPaint by lazy {
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
    private var selectType: ViewState.SelectType = ViewState.SelectType.OneDay
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


    /*****
     *
     * In relation to Month
     *
     *****/

    private fun drawMonth(canvas: Canvas) {
        val monthText = getMonthAndYearString(year, month)
        val monthBounds = getTextBounds(monthText, monthTitlePaint)

        val dayOfWeekText =
            DateFormatSymbols().shortWeekdays[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)]
                .toUpperCase(Locale.getDefault())
        val dayOfWeekBounds = getTextBounds(dayOfWeekText, dayOfWeekPaint)

        val dayOfWeekDivision = (width - viewAttrs.sidePadding * 2) / (DAYS_IN_WEEK * 2)
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
        "$year${context.getString(R.string.year)} " + "$month${context.getString(R.string.month)}"


    /*****
     *
     * In relation to Week
     *
     *****/

    private fun drawDayOfWeek(canvas: Canvas) {
        val dayOfWeekDivision = (width - viewAttrs.sidePadding * 2) / (DAYS_IN_WEEK * 2)

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

            val dayState = getWeekendDayState(i)
            val textPaint: Paint = if (dayState is DayState.NotSelected) {
                when (dayState.dayType) {
                    DayState.DayType.SUNDAY -> sundayTextPaint
                    DayState.DayType.SATURDAY -> saturdayTextPaint
                    else -> normalDayTextPaint
                }
            } else {
                throw IllegalStateException("only possible NotSelected DayState in day of week ")
            }

            canvas.drawText(
                dayOfWeekText,
                x.toFloat(), y.toFloat(),
                textPaint
            )
        }
    }


    /*****
     *
     * In relation to Day
     *
     *****/

    private fun drawDay(canvas: Canvas) {
        val yOffset = viewAttrs.monthSpacing +
                viewAttrs.monthHeight +
                viewAttrs.dayOfWeekHeight +
                (viewAttrs.dayHeight / 2)

        val dayDivision = (width - viewAttrs.sidePadding * 2) / (DAYS_IN_WEEK * 2)

        val dayOfWeekText = DateFormatSymbols()
            .shortWeekdays[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)]
            .toUpperCase(Locale.getDefault())
        val dayOfWeekBounds = Rect()
        dayOfWeekPaint.getTextBounds(dayOfWeekText, 0, dayOfWeekText.length, dayOfWeekBounds)

        days.forEach { dayInfo ->
            val centerX = dayDivision * (1 + dayInfo.dayIndexInWeek * 2) + viewAttrs.sidePadding
            val centerY = yOffset + viewAttrs.dayHeight * dayInfo.weekIndex

            val textPaint: Paint
            when (dayInfo.state) {
                is DayState.Selected -> {
                    drawBackground(
                        canvas,
                        dayInfo.state,
                        centerX,
                        centerY,
                        dayDivision,
                        dayInfo.dayIndexInWeek,
                        dayOfWeekBounds
                    )
                    textPaint = selectedDayTextPaint
                }
                is DayState.Disabled -> textPaint = disabledDayTextPaint
                is DayState.NotSelected -> {
                    textPaint = when (dayInfo.state.dayType) {
                        DayState.DayType.SUNDAY -> sundayTextPaint
                        DayState.DayType.WEEKDAY -> normalDayTextPaint
                        DayState.DayType.SATURDAY -> saturdayTextPaint
                        DayState.DayType.TODAY -> todayTextPaint
                    }
                }
            }

            val dayText = dayInfo.date.day.toString()
            val bounds = getTextBounds(dayText, textPaint)
            drawDayText(canvas, centerX, centerY, bounds, dayText, textPaint)
            drawDayDot(canvas, centerX, centerY, bounds, dayInfo.state)
        }
    }

    private fun drawDayText(
        canvas: Canvas,
        centerX: Int,
        centerY: Int,
        bounds: Rect,
        dayText: String,
        textPaint: Paint
    ) {
        canvas.drawText(
            dayText,
            centerX.toFloat(),
            (centerY + (bounds.bottom - bounds.top) / 2).toFloat(),
            textPaint
        )
    }

    private fun drawDayDot(
        canvas: Canvas,
        centerX: Int,
        centerY: Int,
        bounds: Rect,
        dayState: DayState
    ) {
        val dot = when (dayState) {
            is DayState.Selected -> dayState.dot
            is DayState.NotSelected -> dayState.dot
            else -> false
        }

        if (dot && viewAttrs.dotRadius != 0) {
            val paint = if (dayState is DayState.Selected) {
                dotSelectedPaint
            } else {
                dotNotSelectedPaint
            }

            canvas.drawCircle(
                centerX.toFloat(),
                ((centerY + (bounds.bottom - bounds.top) / 2) +
                        (viewAttrs.dotRadius * 3)).toFloat(),
                viewAttrs.dotRadius.toFloat(),
                paint
            )
        }
    }


    /*****
     *
     * Draw day background
     *
     *****/

    private fun drawBackground(
        canvas: Canvas,
        dayState: DayState,
        x: Int,
        y: Int,
        dayDivision: Int,
        dayOffset: Int,
        dayOfWeekBounds: Rect
    ) {
        if (dayState is DayState.Selected) {
            drawRectBackground(
                canvas,
                dayState,
                x,
                y,
                dayDivision,
                dayOffset,
                dayOfWeekBounds
            )
            drawCircleBackground(
                canvas,
                dayState,
                x,
                y
            )
        }
    }

    private fun drawRectBackground(
        canvas: Canvas,
        dayState: DayState.Selected,
        x: Int,
        y: Int,
        dayDivision: Int,
        dayOffset: Int,
        dayOfWeekBounds: Rect
    ) {
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

        when (dayState.range) {
            DayState.Range.START -> {
                drawRectBackgroundCanvas(
                    left = x,
                    top = y - viewAttrs.selectedCircleSize,
                    right = right,
                    bottom = y + viewAttrs.selectedCircleSize,
                    paint = selectedBackgroundPaint,
                    canvas = canvas
                )
            }
            DayState.Range.END -> {
                drawRectBackgroundCanvas(
                    left = left,
                    top = y - viewAttrs.selectedCircleSize,
                    right = x,
                    bottom = y + viewAttrs.selectedCircleSize,
                    paint = selectedBackgroundPaint,
                    canvas = canvas
                )
            }
            DayState.Range.MIDDLE -> {
                drawRectBackgroundCanvas(
                    left = left,
                    top = y - viewAttrs.selectedCircleSize,
                    right = right,
                    bottom = y + viewAttrs.selectedCircleSize,
                    paint = selectedBackgroundPaint,
                    canvas = canvas
                )
            }
            else -> Unit
        }
    }

    private fun drawCircleBackground(
        canvas: Canvas,
        dayState: DayState.Selected,
        x: Int,
        y: Int
    ) {
        when (dayState.range) {
            DayState.Range.START,
            DayState.Range.END,
            DayState.Range.ONE_DAY ->
                canvas.drawCircle(
                    x.toFloat(),
                    y.toFloat(),
                    viewAttrs.selectedCircleSize.toFloat(),
                    selectedBackgroundPaint
                )
            else -> Unit
        }
    }

    private fun drawRectBackgroundCanvas(
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


    /*****
     *
     * Day State
     *
     *****/

    private fun getDayState(
        date: CalendarDate,
        selectedDates: SelectedDates?,
        today: CalendarDate?,
        dayOffset: Int,
        selectType: ViewState.SelectType,
        dotList: List<Date>?
    ): DayState {
        val dot = dotList?.contains(date.date) ?: false
        return when {
            !isSelectableDate(date) -> DayState.Disabled
            selectedDates?.start == null && selectedDates?.end == null ->
                getDayStateNotSelected(today, date, dayOffset, dot)
            selectedDates.start != null && selectedDates.end == null ->
                when (selectType) {
                    is ViewState.SelectType.OneDay ->
                        throw IllegalStateException(
                            "Not allowed selectedDates state in SelectType OneDay"
                        )
                    is ViewState.SelectType.DayRange -> {
                        if (isSelectedStartDay(date)) {
                            DayState.Selected(DayState.Range.ONE_DAY, dot)
                        } else {
                            getDayStateNotSelected(today, date, dayOffset, dot)
                        }
                    }
                    is ViewState.SelectType.WeekRange -> {
                        val min = selectedDates.start.date
                        val calendar = Calendar.getInstance()
                        calendar.time = min
                        calendar.add(Calendar.DAY_OF_MONTH, 6)
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        val max = calendar.time
                        when {
                            date.date == min -> DayState.Selected(DayState.Range.START, dot)
                            date.date == max -> DayState.Selected(DayState.Range.END, dot)
                            date.date.after(min) && date.date.before(max) -> DayState.Selected(
                                DayState.Range.MIDDLE,
                                dot
                            )
                            else -> getDayStateNotSelected(today, date, dayOffset, dot)
                        }
                    }
                    is ViewState.SelectType.MonthRange -> {
                        val min = selectedDates.start.date
                        val calendar = Calendar.getInstance()
                        calendar.time = min
                        val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                        calendar.set(Calendar.DAY_OF_MONTH, lastDay)
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        val max = calendar.time
                        when {
                            date.date == min -> DayState.Selected(DayState.Range.START, dot)
                            date.date == max -> DayState.Selected(DayState.Range.END, dot)
                            date.date.after(min) && date.date.before(max) -> DayState.Selected(
                                DayState.Range.MIDDLE,
                                dot
                            )
                            else -> getDayStateNotSelected(today, date, dayOffset, dot)
                        }
                    }
                }
            isSelectedStartDay(date) && isSelectedEndDay(date) ->
                DayState.Selected(DayState.Range.ONE_DAY, dot)
            isSelectedStartDay(date) -> DayState.Selected(DayState.Range.START, dot)
            isSelectedEndDay(date) -> DayState.Selected(DayState.Range.END, dot)
            isIncludeSelectedDay(date) -> DayState.Selected(DayState.Range.MIDDLE, dot)
            else -> getDayStateNotSelected(today, date, dayOffset, dot)
        }
    }

    private fun getDayStateNotSelected(
        today: CalendarDate?,
        date: CalendarDate,
        dayOffset: Int,
        dot: Boolean
    ): DayState =
        if (today == date) {
            DayState.NotSelected(DayState.DayType.TODAY, dot)
        } else {
            getWeekendDayState(dayOffset, dot)
        }

    private fun getWeekendDayState(dayOffset: Int, dot: Boolean = false): DayState {
        return when (dayOffset) {
            0 -> DayState.NotSelected(DayState.DayType.SUNDAY, dot)
            DAYS_IN_WEEK - 1 -> DayState.NotSelected(DayState.DayType.SATURDAY, dot)
            else -> DayState.NotSelected(DayState.DayType.WEEKDAY, dot)
        }
    }


    /*****
     *
     * Measure & Draw view
     *
     *****/

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            View.MeasureSpec.getSize(widthMeasureSpec),
            viewAttrs.monthSpacing +
                    viewAttrs.monthHeight +
                    viewAttrs.dayOfWeekHeight +
                    viewAttrs.dayHeight * weekCount
        )
    }

    override fun onDraw(canvas: Canvas) {
        drawMonth(canvas)
        drawDayOfWeek(canvas)
        drawDay(canvas)
    }


    /*****
     *
     * is Select (able)
     *
     *****/

    private fun isSelectedStartDay(date: CalendarDate): Boolean {
        val start = selectedDates?.start ?: return false
        return date.date == start.date
    }

    private fun isSelectedEndDay(date: CalendarDate): Boolean {
        val end = selectedDates?.end ?: return false
        return date.date == end.date
    }

    private fun isIncludeSelectedDay(date: CalendarDate): Boolean {
        val selectedDates = this.selectedDates

        if (selectedDates?.start == null || selectedDates.end == null) {
            return false
        }

        if (isSelectedStartDay(date) || isSelectedEndDay(date)) {
            return false
        }

        val startMilliSec = selectedDates.start.date.time
        val endMilliSec = selectedDates.end.date.time
        val time = date.date.time
        if (time in (startMilliSec + 1)..(endMilliSec - 1)) {
            return true
        }

        return false
    }

    private fun isSelectableDate(date: CalendarDate): Boolean {
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

        val selectType = this.selectType
        val selectedDates = this.selectedDates
        if (selectedDates?.start != null &&
            selectedDates.end == null
        ) {
            when (selectType) {
                is ViewState.SelectType.OneDay ->
                    throw IllegalStateException(
                        "Not allowed selectedDates state in SelectType OneDay"
                    )
                is ViewState.SelectType.DayRange ->
                    if (selectType.selectLimitDay != null) {
                        return !Utils.checkSelectLimitDayExceed(
                            selectType.selectLimitDay,
                            selectedDates.start,
                            date
                        )
                    }
                is ViewState.SelectType.WeekRange ->
                    if (selectType.selectLimitWeek != null) {
                        return isSelectableWeekRange(date, selectType.selectLimitWeek)
                    }
                is ViewState.SelectType.MonthRange ->
                    if (selectType.selectLimitMonth != null) {
                        return isSelectableMonthRange(date, selectType.selectLimitMonth)
                    }
            }
        }
        return true
    }

    private fun isSelectableWeekRange(date: CalendarDate, selectLimitWeek: Int): Boolean {
        val selectedDates = this.selectedDates

        if (selectedDates?.start == null) {
            return true
        }

        val calendar = Calendar.getInstance()
        calendar.time = selectedDates.start.date
        calendar.add(Calendar.WEEK_OF_YEAR, (selectLimitWeek - 1) * -1)
        var difference = if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            -6
        } else {
            Calendar.MONDAY - calendar.get(Calendar.DAY_OF_WEEK)
        }
        calendar.add(Calendar.DAY_OF_MONTH, difference)

        if (date.date.before(calendar.time)) {
            return false
        }

        calendar.time = selectedDates.start.date
        calendar.add(Calendar.WEEK_OF_YEAR, (selectLimitWeek - 1))
        difference = if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            0
        } else {
            Calendar.SATURDAY - calendar.get(Calendar.DAY_OF_WEEK) + 1
        }
        calendar.add(Calendar.DAY_OF_MONTH, difference)

        if (date.date.after(calendar.time)) {
            return false
        }

        return true
    }

    private fun isSelectableMonthRange(date: CalendarDate, selectLimitMonth: Int): Boolean {
        val selectedDates = this.selectedDates

        if (selectedDates?.start == null) {
            return true
        }

        val calendar = Calendar.getInstance()
        calendar.time = selectedDates.start.date
        calendar.add(Calendar.MONTH, (selectLimitMonth - 1) * -1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        if (date.date.before(calendar.time)) {
            return false
        }

        calendar.time = selectedDates.start.date
        calendar.add(Calendar.MONTH, (selectLimitMonth - 1))
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))

        if (date.date.after(calendar.time)) {
            return false
        }

        return true
    }

    private fun isAvailableSelectedDates(
        selectType: ViewState.SelectType,
        selectedStartDate: CalendarDate,
        selectedEndDate: CalendarDate
    ): Boolean {
        when (selectType) {
            is ViewState.SelectType.OneDay -> Unit
            is ViewState.SelectType.DayRange ->
                if (selectType.selectLimitDay != null &&
                    Utils.checkSelectLimitDayExceed(
                        selectType.selectLimitDay,
                        selectedStartDate,
                        selectedEndDate
                    )
                ) {
                    return false
                }
            is ViewState.SelectType.WeekRange ->
                if (selectType.selectLimitWeek != null) {
                    val diffDay = if (selectedStartDate.date.before(selectedEndDate.date)) {
                        TimeUnit.MILLISECONDS.toDays(
                            Math.abs(selectedEndDate.date.time - selectedStartDate.date.time)
                        )
                    } else {
                        val calendar = Calendar.getInstance()
                        calendar.time = selectedStartDate.date
                        calendar.add(Calendar.DAY_OF_MONTH, 6)
                        TimeUnit.MILLISECONDS.toDays(
                            Math.abs(selectedEndDate.date.time - calendar.time.time)
                        )
                    }

                    if (diffDay >= selectType.selectLimitWeek * 7) {
                        return false
                    }
                }
            is ViewState.SelectType.MonthRange ->
                if (selectType.selectLimitMonth != null) {
                    val startCalendar = Calendar.getInstance()
                    val endCalendar = Calendar.getInstance()

                    if (selectedStartDate.date.before(selectedEndDate.date)) {
                        startCalendar.time = selectedStartDate.date
                        endCalendar.time = selectedEndDate.date
                    } else {
                        startCalendar.time = selectedEndDate.date
                        endCalendar.time = selectedStartDate.date
                    }

                    val diffYear = endCalendar.get(Calendar.YEAR) -
                            startCalendar.get(Calendar.YEAR)
                    val diffMonth = diffYear * 12 +
                            endCalendar.get(Calendar.MONTH) -
                            startCalendar.get(Calendar.MONTH)

                    if (diffMonth >= selectType.selectLimitMonth) {
                        return false
                    }
                }
        }

        return true
    }


    /*****
     *
     * Click Event
     *
     *****/

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
        if (x < viewAttrs.sidePadding || x > width - viewAttrs.sidePadding) {
            return null
        }

        val yDay = (y - viewAttrs.monthSpacing - viewAttrs.monthHeight - viewAttrs.dayOfWeekHeight)
            .toInt() / viewAttrs.dayHeight
        val initialDayOffset = days.firstOrNull()?.dayIndexInWeek ?: return null
        val dayIndex = (((x - viewAttrs.sidePadding) * DAYS_IN_WEEK /
                (width - viewAttrs.sidePadding - viewAttrs.sidePadding)).toInt() -
                initialDayOffset) + yDay * DAYS_IN_WEEK

        return days.getOrNull(dayIndex)?.date
    }

    private fun onDateClick(clickedDate: CalendarDate) {
        val selectedDates = this.selectedDates

        if (isSelectableDate(clickedDate)) {
            onDateClickListener?.onDateClicked(clickedDate)
        } else if (selectedDates?.start != null && selectedDates.end == null) {
            val selectType = this.selectType

            if (!isAvailableSelectedDates(selectType, selectedDates.start, clickedDate)) {
                val limit = when (selectType) {
                    is ViewState.SelectType.OneDay ->
                        throw IllegalStateException(
                            "Not allowed selectedDates state in SelectType OneDay"
                        )
                    is ViewState.SelectType.DayRange -> selectType.selectLimitDay
                    is ViewState.SelectType.WeekRange -> selectType.selectLimitWeek
                    is ViewState.SelectType.MonthRange -> selectType.selectLimitMonth
                }

                limit?.let {
                    onDateClickListener?.onSelectLimitExceed(
                        selectedDates.start,
                        clickedDate,
                        selectType,
                        it
                    )
                }
            }
        }
    }


    /*****
     *
     * Set Params and requestLayout
     *
     *****/

    fun setMonthParams(
        year: Int,
        month: Int /* 1 - 12 */,
        startDate: CalendarDate? = null,
        endDate: CalendarDate? = null,
        todaySelected: Boolean = true,
        selectType: ViewState.SelectType = ViewState.SelectType.OneDay,
        selectedDates: SelectedDates = SelectedDates(null, null),
        dotList: List<Date>?
    ) {
        if (selectedDates.start != null && selectedDates.end != null &&
            !isAvailableSelectedDates(selectType, selectedDates.start, selectedDates.end)
        ) {
            throw IllegalArgumentException("exceed select limit day count")
        }

        this@MonthView.year = year
        this@MonthView.month = month
        this@MonthView.startDate = startDate
        this@MonthView.endDate = endDate
        this@MonthView.selectType = selectType
        this@MonthView.selectedDates = selectedDates

        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        val dayOfWeekOffset = calendar.get(Calendar.DAY_OF_WEEK)

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

        val dayCount = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayOffset = getDayOffset(dayOfWeekOffset)

        days = (1..dayCount).map { day ->
            val date = CalendarDate(year, month, day)
            val dayIndex = dayOffset + day - 1
            val dayIndexInWeek = dayIndex % DAYS_IN_WEEK

            DayInfo(
                date,
                getDayState(
                    date,
                    selectedDates,
                    today,
                    dayIndexInWeek,
                    selectType,
                    dotList
                ),
                dayIndexInWeek,
                dayIndex / DAYS_IN_WEEK
            )
        }

        weekCount = Math.ceil((dayCount + dayOffset).toDouble() / DAYS_IN_WEEK).toInt()
        requestLayout()
    }

    private fun getDayOffset(dayOfWeekOffset: Int): Int {
        val offset = if (dayOfWeekOffset < WEEK_START) {
            dayOfWeekOffset + DAYS_IN_WEEK
        } else {
            dayOfWeekOffset
        }
        return offset - WEEK_START
    }


    fun setOnDateClickListener(onDateClickListener: MonthView.OnDateClickListener) {
        this.onDateClickListener = onDateClickListener
    }

    private data class DayInfo(
        val date: CalendarDate,
        val state: DayState,
        val dayIndexInWeek: Int,
        val weekIndex: Int
    )

    companion object {
        private const val DAYS_IN_WEEK = 7
        private const val WEEK_START = Calendar.SUNDAY
    }
}