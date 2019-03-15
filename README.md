# CalendarView

[![](https://jitpack.io/v/daewon-jung/CalendarView.svg)](https://jitpack.io/#daewon-jung/CalendarView)


Download
--------

```gradle
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation "com.github.daewon-jung:CalendarView:1.1.0"
}
```

Declaration
--------
```
    <com.daewonjung.calendarview.CalendarView
        android:id="@+id/calendarView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:scrollbarAlwaysDrawVerticalTrack="true"
        android:scrollbars="vertical"
        app:selectType="weekRange"
        app:startDate="2018-03-05"
        app:endDate="2028-05-28"
        app:selectLimitWeek="13"
        app:dotRadius="1.5dp"
        app:sidePadding="10dp"
        app:todaySelected="true" />
```

Attribute
--------
```
    <declare-styleable name="CalendarView">
    
        <attr name="colorMonthText" format="color" />
        <attr name="colorDayOfWeekText" format="color" />
        <attr name="colorTodayText" format="color"/>
        <attr name="colorDayText" format="color" />
        <attr name="colorSaturdayText" format="color" />
        <attr name="colorSundayText" format="color" />

        <attr name="colorSelectedDayBackground" format="color"/>
        <attr name="colorSelectedDayText" format="color"/>
        <attr name="colorDisabledDayText" format="color"/>

        <attr name="monthHeight" format="dimension" />
        <attr name="dayOfWeekHeight" format="dimension" />
        <attr name="dayHeight" format="dimension" />

        <attr name="textSizeMonth" format="dimension" />
        <attr name="textSizeDayOfWeek" format="dimension" />
        <attr name="textSizeDay" format="dimension"/>

        <attr name="sidePadding" format="dimension"/>
        <attr name="monthSpacing" format="dimension"/>

        <attr name="selectedDayRadius" format="dimension" />
        <attr name="todaySelected" format="boolean" />

        <attr name="startDate" format="string"/>
        <attr name="endDate" format="string"/>

        <attr name="dotRadius" format="dimension" />

        <attr name="selectType" format="enum">
            <enum name="oneDay" value="0" />
            <enum name="dayRange" value="1" />
            <enum name="weekRange" value="2" />
            <enum name="monthRange" value="3" />
        </attr>
        <attr name="selectLimitDay" format="integer" />
        <attr name="selectLimitWeek" format="integer" />
        <attr name="selectLimitMonth" format="integer" />
        
        <attr name="titleDateFormat" format="string" />
        
    </declare-styleable>
```
