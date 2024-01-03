package com.cyberflow.sparkle.main.widget.calendar

import androidx.databinding.BaseObservable
import com.cyberflow.sparkle.MyApp
import com.cyberflow.sparkle.R

data class DateBean(
    var year: Int,
    var month: Int = 1,
    var day: Int = 1,
    var isThisMonth: Boolean = true,
): BaseObservable(){
    var checked = false
}

fun getMonthEngStr(month: Int): String? {
    return when (month) {
        1 -> MyApp.instance.getString(R.string.january)
        2 -> MyApp.instance.getString(R.string.february)
        3 -> MyApp.instance.getString(R.string.march)
        4 -> MyApp.instance.getString(R.string.april)
        5 -> MyApp.instance.getString(R.string.may)
        6 -> MyApp.instance.getString(R.string.june)
        7 -> MyApp.instance.getString(R.string.july)
        8 -> MyApp.instance.getString(R.string.august)
        9 -> MyApp.instance.getString(R.string.september)
        10 -> MyApp.instance.getString(R.string.october)
        11 -> MyApp.instance.getString(R.string.november)
        12 -> MyApp.instance.getString(R.string.december)
        else -> ""
    }
}