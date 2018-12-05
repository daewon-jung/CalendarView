package com.daewonjung.calendarviewsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.daewonjung.calendarview.CalendarDate
import com.daewonjung.calendarview.DateSelectListener
import com.daewonjung.calendarview.ViewState
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity :
    AppCompatActivity(),
    DateSelectListener {

    override fun onSelectedDatesChanged(start: CalendarDate?, end: CalendarDate?) {
        Log.d(TAG, "Day Range Selected : $start --> $end")
    }

    override fun onSelectLimitExceed(
        start: CalendarDate,
        end: CalendarDate,
        selectType: ViewState.SelectType,
        limit: Int
    ) {
        val limitUnit = when (selectType) {
            is ViewState.SelectType.OneDay -> return
            is ViewState.SelectType.DayRange -> "일"
            is ViewState.SelectType.WeekRange -> "주"
            is ViewState.SelectType.MonthRange -> "개월"
        }

        Toast.makeText(
            applicationContext,
            "최대 $limit$limitUnit" + "까지 조회 가능합니다.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarView.dateSelectListener = this

        val list = mutableListOf<Date>()
        for (i in 3..17) {
            val calendar = Calendar.getInstance()
            calendar.set(2018, 11, i)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            list.add(calendar.time)
        }
        calendarView.dotList = list


        oneDayTypeButton.setOnClickListener {
            calendarView.selectType = ViewState.SelectType.OneDay
        }

        dayRangeTypeButton.setOnClickListener {
            calendarView.selectType = ViewState.SelectType.DayRange(10)
        }

        weekRangeTypeButton.setOnClickListener {
            calendarView.selectType = ViewState.SelectType.WeekRange(3)
        }

        monthRangeTypeButton.setOnClickListener {
            calendarView.selectType = ViewState.SelectType.MonthRange(2)
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
