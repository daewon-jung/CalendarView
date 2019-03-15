package com.daewonjung.calendarviewsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.daewonjung.calendarview.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity :
    AppCompatActivity(),
    OnDateSelectedListener,
    OnOutOfRangeDateSelectedListener {

    override fun onSelectedDatesChanged(
        start: CalendarDate?,
        end: CalendarDate?,
        selectType: SelectType
    ) {
        Log.d(TAG, "Day Range Selected : $start --> $end")
    }

    override fun onSelectLimitExceed(
        start: CalendarDate,
        end: CalendarDate,
        selectType: SelectType,
        limit: Int
    ) {
        val limitUnit = when (selectType) {
            is SelectType.OneDay -> return
            is SelectType.DayRange -> "일"
            is SelectType.WeekRange -> "주"
            is SelectType.MonthRange -> "개월"
        }

        Toast.makeText(
            applicationContext,
            "최대 $limit$limitUnit" + "까지 조회 가능합니다.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onSelectedBeforeStartDate(startDate: CalendarDate, selectedDate: CalendarDate) {
        Toast.makeText(
            applicationContext,
            "${startDate.year}. ${startDate.month}. ${startDate.day} " +
                    "이전은 선택할 수 없습니다.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onSelectedAfterEndDate(endDate: CalendarDate, selectedDate: CalendarDate) {
        Toast.makeText(
            applicationContext,
            "${endDate.year}. ${endDate.month}. ${endDate.day} " +
                    "이후는 선택할 수 없습니다.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarView.endDate = CalendarDate.create(Date())
        calendarView.onDateSelectedListener = this
        calendarView.setDotSource { year, successCallback, failureCallback ->
            Log.d(TAG, "$year")
        }
        calendarView.onOutOfRangeDateSelectedListener = this
        calendarView.onTodayVisibleListener = object : OnTodayVisibleListener {
            override fun onVisible(visible: Boolean) {
                Log.d(TAG, "Today visibility : $visible")
            }
        }

        oneDayTypeButton.setOnClickListener {
            calendarView.selectType = SelectType.OneDay
        }

        dayRangeTypeButton.setOnClickListener {
            calendarView.selectType = SelectType.DayRange(10)
        }

        weekRangeTypeButton.setOnClickListener {
            calendarView.selectType = SelectType.WeekRange(3)
        }

        monthRangeTypeButton.setOnClickListener {
            calendarView.selectType = SelectType.MonthRange(2)
        }

        todayScrollButton.setOnClickListener {
            calendarView.scrollToToday()
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
