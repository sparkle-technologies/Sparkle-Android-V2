package com.cyberflow.sparkle.profile.widget

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cyberflow.sparkle.R

class MyDialog(context: Context, themeResId: Int) : Dialog(context, themeResId) {
    override fun setContentView(layoutResID: Int) {
        setContentView(
            LayoutInflater.from(context).inflate(layoutResID, null, false)
        )
    }

    override fun setContentView(view: View) {
        setContentView(
            view, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        val root = LayoutInflater.from(context).inflate(R.layout.dialog_pangu_bottom_wrapper, null, false)
        val container = root.findViewById<ViewGroup>(R.id.pangu_bottom_dialog_container)
        container.addView(view, params)
        super.setContentView(
            root, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }
}