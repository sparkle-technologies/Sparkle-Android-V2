package com.cyberflow.base.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object KeyboardUtil {

    @JvmStatic
    fun show(view: View) {
        val context = view.context.applicationContext
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
        try {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        } catch (e: Exception) {
            imm.toggleSoftInput(
                InputMethodManager.SHOW_IMPLICIT,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    @JvmStatic
    fun hide(view: View) {
        val context = view.context.applicationContext
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
        try {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (e: Exception) {
            imm.toggleSoftInput(
                InputMethodManager.SHOW_IMPLICIT,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }
}