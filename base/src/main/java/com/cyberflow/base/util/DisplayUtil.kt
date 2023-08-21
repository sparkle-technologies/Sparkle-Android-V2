package com.cyberflow.base.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import kotlin.math.roundToInt

private const val TAG = "DisplayUtil"
object DisplayUtil {
    var SCREEN_WIDTH_PIXELS = 0
    var SCREEN_HEIGHT_PIXELS = 0
    var SCREEN_DENSITY = 0f
    var SCREEN_WIDTH_DP = 0
    var SCREEN_HEIGHT_DP = 0
    var DPI = 0
    var SCALED_DENSITY = 0F

    //may can't get this value
    var STATUS_BAR_HEIGHT_PIXELS = 0

    @SuppressLint("InternalInsetResource")
    fun init(context: Context) {
        val dm: DisplayMetrics = context.resources.displayMetrics
        val w: Int = dm.widthPixels
        val h: Int = dm.heightPixels
        if (w > h) {
            SCREEN_WIDTH_PIXELS = h
            SCREEN_HEIGHT_PIXELS = w
        } else {
            SCREEN_WIDTH_PIXELS = w
            SCREEN_HEIGHT_PIXELS = h
        }
        SCREEN_DENSITY = dm.density
        DPI = dm.densityDpi
        SCREEN_WIDTH_DP = (SCREEN_WIDTH_PIXELS / dm.density).roundToInt()
        SCREEN_HEIGHT_DP = (SCREEN_HEIGHT_PIXELS / dm.density).roundToInt()
        val resourceId: Int = context.resources.getIdentifier(
            "status_bar_height", "dimen",
            "android"
        )
        if (resourceId > 0) {
            STATUS_BAR_HEIGHT_PIXELS = context.resources.getDimensionPixelSize(resourceId)
        }
        dumpInfo()
    }

    fun dp2px(dp: Int): Int {
        return dp2px(dp.toFloat())
    }

    fun dp2px(dp: Float): Int {
        return (dp * SCREEN_DENSITY + 0.5f).roundToInt()
    }

    fun px2dp(view: View, px: Int): Int {
        return (px / view.resources.displayMetrics.density).roundToInt()
    }

    fun sp2px(sp: Float): Int {
        return (sp * SCALED_DENSITY + 0.5f).roundToInt()
    }

    private fun dumpInfo() {
        val result =
            java.lang.StringBuilder().appendLine("SCREEN_WIDTH_PIXELS = $SCREEN_WIDTH_PIXELS")
                .appendLine("SCREEN_HEIGHT_PIXELS = $SCREEN_HEIGHT_PIXELS")
                .appendLine("SCREEN_DENSITY = $SCREEN_DENSITY")
                .appendLine("SCREEN_WIDTH_DP = $SCREEN_WIDTH_DP")
                .appendLine("SCREEN_HEIGHT_DP = $SCREEN_HEIGHT_DP")
                .appendLine("DPI = $DPI")
                .appendLine("STATUS_BAR_HEIGHT_PIXELS = $STATUS_BAR_HEIGHT_PIXELS")
        Log.i(TAG, "***Display***:\n$result")
    }
}