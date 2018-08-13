package com.daewonjung.calendarview

import android.content.Context
import android.support.v7.widget.RecyclerView

class ViewHolder(
    private val monthView: MonthView,
    onDateClickListener: MonthView.OnDateClickListener
) : RecyclerView.ViewHolder(monthView) {

    init {
        monthView.setOnDateClickListener(onDateClickListener)
    }

    fun bind(
        year: Int,
        month: Int,
        selectedDays: SelectedDates,
        startDate: CalendarDate?,
        endDate: CalendarDate?,
        selectLimitDay: Int
    ) {
        monthView.setMonthParams(year, month, selectedDays, startDate, endDate, selectLimitDay)
    }


    companion object {

        fun create(
            context: Context,
            viewAttrs: ViewAttrs,
            onDateClickListener: MonthView.OnDateClickListener
        ): ViewHolder = ViewHolder(MonthView(context, viewAttrs), onDateClickListener)
    }
}