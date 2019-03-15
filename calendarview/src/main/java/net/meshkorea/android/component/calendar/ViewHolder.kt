package com.daewonjung.calendarview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup


internal class ViewHolder(
    private val monthView: MonthView
) : RecyclerView.ViewHolder(monthView) {

    fun bind(monthData: CalendarViewAdapter.MonthData) {
        with(monthData) {
            monthView.setMonthParams(
                year,
                month,
                startDate,
                endDate,
                todaySelected,
                selectType,
                selectedDates,
                dotDayList
            )
        }
    }

    companion object {

        fun create(
            context: Context,
            viewAttrs: ViewAttrs,
            onDateClickListener: OnInternalDateSelectedListener
        ): ViewHolder =
            ViewHolder(MonthView(context, viewAttrs).apply {
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                this.onDateClickListener = onDateClickListener
            })
    }
}