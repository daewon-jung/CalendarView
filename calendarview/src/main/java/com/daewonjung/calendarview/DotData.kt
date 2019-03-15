package com.daewonjung.calendarview

sealed class DotData {

    object Loading : DotData()
    data class Valid(val dayListMap: Map<Int, List<Int>>) : DotData()
    object Invalid : DotData()
}