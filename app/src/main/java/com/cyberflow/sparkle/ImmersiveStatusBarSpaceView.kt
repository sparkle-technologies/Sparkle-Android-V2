package com.cyberflow.sparkle

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.cyberflow.base.util.DisplayUtil

class ImmersiveStatusBarSpaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val viewHeight: Int
        @SuppressLint("ObsoleteSdkInt")
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) DisplayUtil.STATUS_BAR_HEIGHT_PIXELS else 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, viewHeight)
    }
}