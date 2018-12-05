package net.meshkorea.android.component.calendarsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import net.meshkorea.android.component.calendar.CalendarDate
import net.meshkorea.android.component.calendar.DateSelectListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity :
    AppCompatActivity(),
    DateSelectListener {

    override fun onSelectedDatesChanged(start: CalendarDate?, end: CalendarDate?) {
        Log.d(TAG, "Day Range Selected : $start --> $end")
    }

    override fun onSelectLimitDayExceed(
        start: CalendarDate,
        end: CalendarDate,
        selectLimitDay: Int
    ) {
        Toast.makeText(
            applicationContext,
            "최대 " + selectLimitDay + "일까지 조회 가능합니다.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarView.dateSelectListener = this
    }

    companion object {
        const val TAG = "MainActivity"
    }
}