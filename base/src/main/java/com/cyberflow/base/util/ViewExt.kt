package com.cyberflow.base.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.PixelCopy
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.view.drawToBitmap


object ViewExt {


    fun convertViewToBitmap(view: View): Bitmap {
        return convertViewToBitmap1(view)
    }


    // 方法1  实操过
    private fun convertViewToBitmap1(view: View): Bitmap {
//        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
//        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.RGBA_F16)
        } else {
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(bitmap)
//        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
//    canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }

    //方法2  和上面没啥区别
    private  fun convertViewToBitmap2(view: View): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view.drawToBitmap(Bitmap.Config.RGBA_F16)
        } else {
            view.drawToBitmap(Bitmap.Config.ARGB_8888)
        }
    }

    // 方法3 实际也没啥区别  像素级别的复制 对背景色还有要求  UI线程
    // for api level 28
    private fun convertViewToBitmap3(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
        activity.window?.let { window ->
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewInWindow)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PixelCopy.request(
                        window, Rect(
                            locationOfViewInWindow[0],
                            locationOfViewInWindow[1],
                            locationOfViewInWindow[0] + view.width,
                            locationOfViewInWindow[1] + view.height
                        ), bitmap, { copyResult ->
                            if (copyResult == PixelCopy.SUCCESS) {
                                callback(bitmap)
                            } else {

                            }
                            // possible to handle other result codes ...
                        }, Handler()
                    )
                }
            } catch (e: IllegalArgumentException) {
                // PixelCopy may throw IllegalArgumentException, make sure to handle it
                e.printStackTrace()
            }
        }
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

//https://cloud.tencent.com/developer/article/1734017
/**
 * 设置textView结尾...后面显示的文字和颜色
 * @param context 上下文
 * @param textView textview
 * @param minLines 最少的行数
 * @param originText 原文本
 * @param endText 结尾文字
 * @param endColorID 结尾文字颜色id
 */
fun toggleEllipsize(
    context: Context,
    textView: TextView,
    minLines: Int,
    originText: String,
    endText: String,
    endColorID: Int
) {
    if (TextUtils.isEmpty(originText)) {
        return
    }
    textView.viewTreeObserver.addOnGlobalLayoutListener(object :
        ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val paddingLeft = textView.paddingLeft
            val paddingRight = textView.paddingRight
            val paint = textView.paint
            val totalW = (textView.width - paddingLeft - paddingRight) * minLines
            val originW = textView.textSize * originText.length
            val moreW = textView.textSize * endText.length
            if (totalW > originW + moreW) {
                val temp: CharSequence = originText + endText
                val ssb = SpannableStringBuilder(temp)
                ssb.setSpan(
                    ForegroundColorSpan(context.resources.getColor(endColorID)),
                    temp.length - endText.length,
                    temp.length,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                textView.text = ssb
            } else {
                val availableTextWidth =
                    (textView.width - paddingLeft - paddingRight) * minLines - moreW
                val ellipsizeStr = TextUtils.ellipsize(
                    originText,
                    paint,
                    availableTextWidth,
                    TextUtils.TruncateAt.END
                )
                val temp: CharSequence = ellipsizeStr.toString() + endText
                val ssb = SpannableStringBuilder(temp)
                ssb.setSpan(
                    ForegroundColorSpan(context.resources.getColor(endColorID)),
                    temp.length - endText.length,
                    temp.length,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                textView.text = ssb
            }
            textView.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}

