package net.meshkorea.android.component.calendar

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup


class ViewHolder(
    private val monthView: MonthView
) : RecyclerView.ViewHolder(monthView) {

    fun bind(monthData: CalendarViewAdapter.MonthData) {
        with(monthData) {
            monthView.setMonthParams(
                year,
                month,
                selectedDays,
                startDate,
                endDate,
                selectLimitDay,
                todaySelected
            )
        }
    }

    companion object {

        fun create(
            context: Context,
            viewAttrs: ViewAttrs,
            onDateClickListener: MonthView.OnDateClickListener
        ): ViewHolder =
            ViewHolder(
                MonthView(
                    context,
                    viewAttrs
                ).apply {
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setOnDateClickListener(onDateClickListener)
                })
    }
}