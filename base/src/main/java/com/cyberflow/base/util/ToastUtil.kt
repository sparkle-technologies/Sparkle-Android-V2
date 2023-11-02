package com.cyberflow.base.util

import android.app.Activity
import android.content.Context
import android.widget.Toast
import java.lang.ref.WeakReference

object ToastUtil {

    private var lastToast: WeakReference<Toast>? = null

    fun show(context: Context, text: String, short: Boolean = true) {
        if (context is Activity && context.isFinishing) {
            return
        }

        val last = lastToast?.get()
        //
        // 这里为什么不复用last了，原因如下：
        //
        // 原生Toast直接set上一个会有显示时间不够的问题，连续点击会有一段时间无法展示
        // 所以别说复用上一个Toast对象是优化了
        //
        // 对于自定义Toast，如果页面跳转，前Toast持有的Activity是无效的
        //
        // 所以这里的用处就是立即隐藏上一个Toast
        //
        last?.cancel()

        val duration = if (short) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
        Toast.makeText(context, text, duration)?.let {
            it.show()
            lastToast = WeakReference<Toast>(it)
        }
    }
}