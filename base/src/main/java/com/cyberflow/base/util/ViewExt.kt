package com.cyberflow.base.util

import android.graphics.Bitmap
import android.graphics.Canvas
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
