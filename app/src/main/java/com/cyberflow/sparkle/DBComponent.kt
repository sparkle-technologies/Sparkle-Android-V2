package com.cyberflow.sparkle

import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import com.cyberflow.base.util.dp2px
import com.cyberflow.sparkle.main.widget.NumView

object DBComponent {

    @BindingAdapter(value = ["colorStr", "numStr"], requireAll = false)
    @JvmStatic
    fun NumView.initDB(_color: String?, _num: String?) {
        try {
            color = Color.parseColor(_color)
            num = _num.orEmpty()
        } catch (_: Exception) {
        }
    }

    @BindingAdapter("layoutMarginBottomTop")
    @JvmStatic
    fun setLayoutMarginBottom(view: View, dimen: Float) {
        val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = dimen.toInt()
        view.layoutParams = layoutParams
    }
}