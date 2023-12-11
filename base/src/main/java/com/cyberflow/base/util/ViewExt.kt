package com.cyberflow.base.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView


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
            val totalW =  (textView.width - paddingLeft - paddingRight) * minLines
            val originW = textView.textSize * originText.length
            val moreW = textView.textSize * endText.length
            if(totalW > originW + moreW){
                val temp: CharSequence = originText + endText
                val ssb = SpannableStringBuilder(temp)
                ssb.setSpan(ForegroundColorSpan(context.resources.getColor(endColorID)), temp.length - endText.length, temp.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                textView.text = ssb
            }else{
                val availableTextWidth = (textView.width - paddingLeft - paddingRight) * minLines - moreW
                val ellipsizeStr = TextUtils.ellipsize(originText, paint, availableTextWidth, TextUtils.TruncateAt.END)
                val temp: CharSequence = ellipsizeStr.toString() + endText
                val ssb = SpannableStringBuilder(temp)
                ssb.setSpan(ForegroundColorSpan(context.resources.getColor(endColorID)), temp.length - endText.length, temp.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                textView.text = ssb
            }
            if (Build.VERSION.SDK_INT > 16) {
                textView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            } else {
                textView.viewTreeObserver.removeGlobalOnLayoutListener(this)
            }
        }
    })
}
