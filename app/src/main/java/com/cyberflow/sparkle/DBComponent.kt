package com.cyberflow.sparkle

import android.graphics.Color
import androidx.databinding.BindingAdapter
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
}