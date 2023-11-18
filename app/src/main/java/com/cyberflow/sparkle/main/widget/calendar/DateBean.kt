package com.cyberflow.sparkle.main.widget.calendar

import androidx.databinding.BaseObservable

data class DateBean(
    var year: Int,
    var month: Int,
    var day: Int = 0,
    var isThisMonth: Boolean = true,
): BaseObservable(){
    var checked = false
}

fun getMonthEngStr(month: Int): String? {
    return when (month) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> ""
    }
}