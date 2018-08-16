# CalendarView

[![](https://jitpack.io/v/daewon-jung/CalendarView.svg)](https://jitpack.io/#daewon-jung/CalendarView)

Download
--------

```gradle
repositories {
  maven { url "https://jitpack.io" }
}

dependencies {
  implementation 'com.github.daewon-jung:CalendarView:1.0.0'
}
```

Usage
--------

```
<com.daewonjung.calendarview.CalendarView
    android:id="@+id/calendarView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:startDate="2017-3-10"/>


<declare-styleable name="CalendarView">
    <attr name="colorMonthText" format="color" />
    <attr name="colorDayOfWeekText" format="color" />
    <attr name="colorTodayText" format="color"/>
    <attr name="colorDayText" format="color" />
    <attr name="colorSaturdayText" format="color" />
    <attr name="colorSundayText" format="color" />

    <attr name="colorSelectedDayBackground" format="color"/>
    <attr name="colorSelectedDayText" format="color"/>
    <attr name="colorDisabledDay" format="color"/>

    <attr name="monthHeight" format="dimension" />
    <attr name="dayOfWeekHeight" format="dimension" />
    <attr name="dayHeight" format="dimension" />

    <attr name="textSizeMonth" format="dimension" />
    <attr name="textSizeDayOfWeek" format="dimension" />
    <attr name="textSizeDay" format="dimension"/>

    <attr name="padding" format="dimension"/>
    <attr name="monthSpacing" format="dimension"/>

    <attr name="selectLimitDay" format="integer" />
    <attr name="selectedDayRadius" format="dimension" />
    <attr name="currentDaySelected" format="boolean" />

    <attr name="startDate" format="string"/>
    <attr name="endDate" format="string"/>
</declare-styleable>
```
