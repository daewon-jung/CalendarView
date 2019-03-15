package com.daewonjung.calendarview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Paint.Style
import android.graphics.Rect
import android.graphics.RectF
import android.support.v4.content.ContextCompat
import android.view.MotionEvent
import android.view.View
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("ViewConstructor")
internal class MonthView(
    context: Context,
    private val viewAttrs: ViewAttrs
) : View(context) {

    private val monthTitlePaint by lazy {
        Paint().apply {
            isAntiAlias = true
            textSize = viewAttrs.monthTextSize.toFloat()
            color = viewAttrs.monthTextColor
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private val dayOfWeekPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            textSize = viewAttrs.dayOfWeekTextSize.toFloat()
            color = viewAttrs.dayOfWeekTextColor
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private val normalDayTextPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            textSize = viewAttrs.dayTextSize.toFloat()
            color = viewAttrs.dayTextColor
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private val saturdayTextPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            textSize = viewAttrs.dayTextSize.toFloat()
            color = viewAttrs.saturdayTextColor
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private val sundayTextPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            textSize = viewAttrs.dayTextSize.toFloat()
            color = viewAttrs.sundayTextColor
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private val disabledDayTextPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            textSize = viewAttrs.dayTextSize.toFloat()
            color = viewAttrs.disabledDayColor
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private val selectedDayTextPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            textSize = viewAttrs.dayTextSize.toFloat()
            color = viewAttrs.selectedDayTextColor
            style = Style.FILL
            textAlign = Align.CENTER
        }
    }

    private val selectedBackgroundPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = viewAttrs.selectedDayBgColor
            style = Style.FILL
        }
    }

    private val todaySelectedCirclePaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = ContextCompat.getColor(context, R.color.white)
            style = Style.STROKE
            strokeWidth = 2f
        }
    }

    private val todayNotSelectedCirclePaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = viewAttrs.selectedDayBgColor
            style = Style.STROKE
            strokeWidth = 2f
        }
    }

    private val dotSelectedPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = ContextCompat.getColor(context, R.color.white)
            style = Style.FILL
        }
    }

    private val dotNotSelectedPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = viewAttrs.selectedDayBgColor
            style = Style.FILL
        }
    }

    private val dateFormatSymbols: DateFormatSymbols

    private var days: List<DayInfo> = emptyList()

    private var year: Int = 0
    private var month: Int = 0

    private var startDate: CalendarDate? = null
    private var endDate: CalendarDate? = null
    private var selectType: SelectType = SelectType.OneDay
    private var selectedDates: SelectedDates? = null
    private var dotDayList: MutableList<Int>? = null

    private var weekCount = 0
    internal var onDateClickListener: OnInternalDateSelectedListener? = null

    init {
        isClickable = true
        isSaveEnabled = false
        dateFormatSymbols = DateFormatSymbols()
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

        val dayOfWeekText = dateFormatSymbols
            .shortWeekdays[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)]
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

    private fun getMonthAndYearString(year: Int, month: Int): String {
        val date = Calendar.getInstance().apply {
            set(year, month - 1, 1)
        }.time

        val dateFormat = SimpleDateFormat(viewAttrs.titleDateFormat, Locale.getDefault())
        return dateFormat.format(date)
    }


    /*****
     *
     * In relation to Week
     *
     *****/

    private fun drawDayOfWeek(canvas: Canvas) {
        val dayOfWeekDivision = (width - viewAttrs.sidePadding * 2) / (DAYS_IN_WEEK * 2)

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

            val dayType = getDayType(i)
            val textPaint: Paint = when (dayType) {
                DayState.DayType.SUNDAY -> sundayTextPaint
                DayState.DayType.SATURDAY -> saturdayTextPaint
                else -> normalDayTextPaint
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

        val dayOfWeekText = dateFormatSymbols
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
                is DayState.Normal -> {
                    textPaint = when (dayInfo.state.dayType) {
                        DayState.DayType.SUNDAY -> sundayTextPaint
                        DayState.DayType.WEEKDAY -> normalDayTextPaint
                        DayState.DayType.SATURDAY -> saturdayTextPaint
                    }
                }
            }

            val dayText = dayInfo.date.day.toString()
            val bounds = getTextBounds(dayText, textPaint)
            drawDayText(canvas, centerX, centerY, bounds, dayText, textPaint)
            drawDayDot(canvas, centerX, centerY, bounds, dayInfo.state)
            drawToday(canvas, centerX, centerY, dayInfo.state)
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
            is DayState.Normal -> dayState.dot
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

    private fun drawToday(
        canvas: Canvas,
        centerX: Int,
        centerY: Int,
        dayState: DayState
    ) {
        if (dayState.today) {
            val paint = if (dayState is DayState.Selected) {
                todaySelectedCirclePaint
            } else {
                todayNotSelectedCirclePaint
            }

            canvas.drawCircle(
                centerX.toFloat(),
                centerY.toFloat(),
                viewAttrs.selectedCircleSize.toFloat() - 3,
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
            DayState.Range.START ->
                canvas.drawArc(
                    RectF(
                        (x - viewAttrs.selectedCircleSize).toFloat(),
                        (y - viewAttrs.selectedCircleSize).toFloat(),
                        (x + viewAttrs.selectedCircleSize).toFloat(),
                        (y + viewAttrs.selectedCircleSize).toFloat()
                    ),
                    90f,
                    180f,
                    true,
                    selectedBackgroundPaint
                )
            DayState.Range.END ->
                canvas.drawArc(
                    RectF(
                        (x - viewAttrs.selectedCircleSize).toFloat(),
                        (y - viewAttrs.selectedCircleSize).toFloat(),
                        (x + viewAttrs.selectedCircleSize).toFloat(),
                        (y + viewAttrs.selectedCircleSize).toFloat()
                    ),
                    270f,
                    180f,
                    true,
                    selectedBackgroundPaint
                )
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
        selectType: SelectType
    ): DayState {
        val dayType = getDayType(dayOffset)
        val isToday = today == date
        val hasDot = hasDot(date)

        if (!isSelectableDate(date)) {
            return DayState.Disabled(dayType, isToday, hasDot)
        }

        val rangeStartDate = selectedDates?.start?.date
        val rangeEndDate = selectedDates?.end?.date ?: when (selectType) {
            is SelectType.OneDay, is SelectType.DayRange -> selectedDates?.start?.date
            is SelectType.WeekRange -> {
                val selectedStartDate = selectedDates?.start?.date
                selectedStartDate?.let {
                    val lastDateInWeek = Utils.getLastDateInWeek(selectedStartDate)

                    val endDate = this.endDate
                    if (endDate?.date != null && lastDateInWeek.after(endDate.date)) {
                        endDate.date
                    } else {
                        lastDateInWeek
                    }
                }
            }
            is SelectType.MonthRange -> {
                val selectedStartDate = selectedDates?.start?.date
                selectedStartDate?.let {
                    val lastDateInMonth = Calendar.getInstance().apply {
                        time = selectedStartDate
                        set(
                            Calendar.DAY_OF_MONTH,
                            getActualMaximum(Calendar.DAY_OF_MONTH)
                        )
                    }

                    val endDate = this.endDate
                    if (endDate?.date != null && lastDateInMonth.time.after(endDate.date)) {
                        endDate.date
                    } else {
                        lastDateInMonth.time
                    }
                }
            }
        }

        return when {
            date.date == rangeStartDate && date.date == rangeEndDate ->
                DayState.Selected(DayState.Range.ONE_DAY, dayType, isToday, hasDot)
            date.date == rangeStartDate ->
                DayState.Selected(DayState.Range.START, dayType, isToday, hasDot)
            date.date == rangeEndDate ->
                DayState.Selected(DayState.Range.END, dayType, isToday, hasDot)
            rangeStartDate == null || rangeEndDate == null ->
                DayState.Normal(dayType, isToday, hasDot)
            date.date.after(rangeStartDate) && date.date.before(rangeEndDate) ->
                DayState.Selected(DayState.Range.MIDDLE, dayType, isToday, hasDot)
            else -> DayState.Normal(dayType, isToday, hasDot)
        }
    }

    private fun getDayType(dayOffset: Int): DayState.DayType =
        when (dayOffset) {
            0 -> DayState.DayType.SUNDAY
            DAYS_IN_WEEK - 1 -> DayState.DayType.SATURDAY
            else -> DayState.DayType.WEEKDAY
        }

    private fun hasDot(date: CalendarDate): Boolean {
        val dotDayList = this.dotDayList
        dotDayList?.let {
            it.forEach { dotDay ->
                if (date.day == dotDay) {
                    this.dotDayList?.remove(date.day)
                    return true
                }
            }
        }

        return false
    }


    /*****
     *
     * Measure & Draw view
     *
     *****/

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
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
     * is Selectable
     *
     *****/

    private fun isSelectableDate(date: CalendarDate): Boolean {
        val startDate = this.startDate
        if (startDate?.date?.after(date.date) == true) {
            return false
        }

        val endDate = this.endDate
        if (endDate?.date?.before(date.date) == true) {
            return false
        }

        val selectType = this.selectType
        val selectedDates = this.selectedDates
        if (selectedDates?.start != null && selectedDates.end == null) {
            when (selectType) {
                is SelectType.OneDay ->
                    throw IllegalStateException(
                        "Not allowed selectedDates state in SelectType OneDay"
                    )
                is SelectType.DayRange ->
                    if (selectType.selectLimitDay != null) {
                        return isSelectableDayRange(
                            selectedDates.start,
                            date,
                            selectType.selectLimitDay
                        )
                    }
                is SelectType.WeekRange ->
                    if (selectType.selectLimitWeek != null) {
                        return isSelectableWeekRange(
                            selectedDates.start,
                            date,
                            selectType.selectLimitWeek
                        )
                    }
                is SelectType.MonthRange ->
                    if (selectType.selectLimitMonth != null) {
                        return isSelectableMonthRange(
                            selectedDates.start,
                            date,
                            selectType.selectLimitMonth
                        )
                    }
            }
        }

        return true
    }

    private fun isSelectableDayRange(
        startDate: CalendarDate,
        date: CalendarDate,
        selectLimitDay: Int
    ): Boolean {
        val diffMilliSec = Math.abs(startDate.date.time - date.date.time)
        val diffDay = diffMilliSec / 1000 / 60 / 60 / 24 + 1
        return diffDay <= selectLimitDay
    }

    private fun isSelectableWeekRange(
        startDate: CalendarDate,
        date: CalendarDate,
        selectLimitWeek: Int
    ): Boolean {
        val selectableFirstWeek = Calendar.getInstance().apply {
            time = startDate.date
            add(Calendar.WEEK_OF_YEAR, (selectLimitWeek - 1) * -1)
        }

        if (date.date.before(Utils.getFirstDateInWeek(selectableFirstWeek.time))) {
            return false
        }

        val selectableLastWeek = Calendar.getInstance().apply {
            time = startDate.date
            add(Calendar.WEEK_OF_YEAR, (selectLimitWeek - 1))
        }

        if (date.date.after(Utils.getLastDateInWeek(selectableLastWeek.time))) {
            return false
        }

        return true
    }

    private fun isSelectableMonthRange(
        startDate: CalendarDate,
        date: CalendarDate,
        selectLimitMonth: Int
    ): Boolean {
        val selectableFirstDate = Calendar.getInstance().apply {
            time = startDate.date
            add(Calendar.MONTH, (selectLimitMonth - 1) * -1)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        if (date.date.before(selectableFirstDate.time)) {
            return false
        }

        val selectableLastDate = Calendar.getInstance().apply {
            time = startDate.date
            add(Calendar.MONTH, (selectLimitMonth - 1))
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }

        if (date.date.after(selectableLastDate.time)) {
            return false
        }

        return true
    }

    private fun validationSelectedDates(
        selectedStartDate: CalendarDate,
        selectedEndDate: CalendarDate,
        selectType: SelectType
    ): Boolean {
        when (selectType) {
            is SelectType.OneDay -> Unit
            is SelectType.DayRange ->
                if (selectType.selectLimitDay != null &&
                    !isSelectableDayRange(
                        selectedStartDate,
                        selectedEndDate,
                        selectType.selectLimitDay
                    )
                ) {
                    return false
                }
            is SelectType.WeekRange ->
                if (selectType.selectLimitWeek != null &&
                    !isSelectableWeekRange(
                        selectedStartDate,
                        selectedEndDate,
                        selectType.selectLimitWeek
                    )
                ) {
                    return false
                }
            is SelectType.MonthRange ->
                if (selectType.selectLimitMonth != null &&
                    !isSelectableMonthRange(
                        selectedStartDate,
                        selectedEndDate,
                        selectType.selectLimitMonth
                    )
                ) {
                    return false
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
                (width - viewAttrs.sidePadding * 2)).toInt() -
                initialDayOffset) + yDay * DAYS_IN_WEEK

        return days.getOrNull(dayIndex)?.date
    }

    private fun onDateClick(clickedDate: CalendarDate) {
        val startDate = this.startDate
        val endDate = this.endDate
        val selectedDates = this.selectedDates
        val selectType = this.selectType

        if (isSelectableDate(clickedDate)) {
            onDateClickListener?.onDateClicked(clickedDate, selectType)

        } else if (startDate?.date?.after(clickedDate.date) == true) {
            onDateClickListener?.onSelectedBeforeStartDate(startDate, clickedDate)

        } else if (endDate?.date?.before(clickedDate.date) == true) {
            onDateClickListener?.onSelectedAfterEndDate(endDate, clickedDate)

        } else if (selectedDates?.start != null && selectedDates.end == null) {
            val clickedLastDate = when (selectType) {
                is SelectType.OneDay -> clickedDate
                is SelectType.DayRange -> clickedDate
                is SelectType.WeekRange ->
                    CalendarDate.create(Utils.getLastDateInWeek(clickedDate.date))
                is SelectType.MonthRange ->
                    CalendarDate.create(Calendar.getInstance().apply {
                        time = clickedDate.date
                        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    }.time)
            }

            if (!validationSelectedDates(selectedDates.start, clickedLastDate, selectType)) {
                val limit = when (selectType) {
                    is SelectType.OneDay ->
                        throw IllegalStateException(
                            "Not allowed selectedDates state in SelectType OneDay"
                        )
                    is SelectType.DayRange -> selectType.selectLimitDay
                    is SelectType.WeekRange -> selectType.selectLimitWeek
                    is SelectType.MonthRange -> selectType.selectLimitMonth
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
        selectType: SelectType = SelectType.OneDay,
        selectedDates: SelectedDates = SelectedDates(null, null),
        dotDayList: List<Int>?
    ) {
        if (selectedDates.start != null && selectedDates.end != null &&
            !validationSelectedDates(selectedDates.start, selectedDates.end, selectType)
        ) {
            throw IllegalArgumentException("exceed select limit count")
        }

        this@MonthView.year = year
        this@MonthView.month = month
        this@MonthView.startDate = startDate
        this@MonthView.endDate = endDate
        this@MonthView.selectType = selectType
        this@MonthView.selectedDates = selectedDates
        this@MonthView.dotDayList = dotDayList?.toMutableList()

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
                    selectType
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