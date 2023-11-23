package com.cyberflow.base.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View

object ViewExt {
    fun convertViewToBitmap(view: View): Bitmap {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
//        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888);
        val canvas = Canvas(bitmap)
//    canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }
}


fun <T : View> T.click(
    block: View.OnClickListener,
) {
    setOnClickListener {
        block.onClick(this)
    }
}

fun View.safeClick(debounceInterval: Long = 1000L, onClickListener: (view: View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceInterval) {
            lastClickTime = currentTime
            onClickListener(it)
        }
    }
}

private var lastY = 0
private var offsetY = 0
fun View.dialogSlipDismiss(onDismiss: () -> Unit) {
    lastY = 0
    offsetY = 0
    setOnTouchListener { view, event ->
        var y = event.rawY.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastY = event.rawY.toInt()
            }

            MotionEvent.ACTION_MOVE -> {
                offsetY = y - lastY
                if (offsetY > 0) {
                    view.translationY = offsetY.toFloat()
                }
            }

            MotionEvent.ACTION_UP -> { // 手指离开
                if (offsetY > 0) {
                    if (offsetY < view.height / 3) {
                        view.translationY = 0f
                    } else {
                        onDismiss.invoke()
                    }
                }
            }
        }
        true
    }

}
