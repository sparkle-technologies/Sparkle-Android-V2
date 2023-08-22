package com.cyberflow.base.util

import com.cyberflow.base.BaseApp

private const val TAG = "DisplayUtil"
object DisplayUtil {

}

fun dp2px(dpValue: Float): Int {
    val scale = BaseApp.instance!!.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}