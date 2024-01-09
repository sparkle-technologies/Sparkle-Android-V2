package com.cyberflow.sparkle.widget

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.Window
import android.view.inputmethod.InputMethodManager

object ImmerUtils {
    open fun hideSoftKeyBoard(activity: Activity) {
        val localView = activity.currentFocus
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (localView != null && imm != null) {
            imm.hideSoftInputFromWindow(localView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    open fun getWidthAndHeight(window: Window?): Array<Int>? {
        if (window == null) {
            return null
        }
        val integer = arrayOf(0, 0)
        val dm = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.windowManager.defaultDisplay.getRealMetrics(dm)
        } else {
            window.windowManager.defaultDisplay.getMetrics(dm)
        }
        integer[0] = dm.widthPixels
        integer[1] = dm.heightPixels
        return integer
    }
}