package com.hyphenate.easeui.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import com.cyberflow.base.BaseApp

fun Context?.getWrappedActivity(): Activity? {
    var cur = this
    while (true) {
        if (cur is Activity) {
            return cur
        }
        cur = if (cur is ContextWrapper) {
            cur.baseContext
        } else {
            return null
        }
    }
}

/**
 * try get host activity from view.
 * views hosted on floating window like dialog and toast will sure return null.
 * @return host activity; or null if not available
 */
fun View.getActivityFromView(): Activity? {
    var context = this.context
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun app(): Application = BaseApp.instance!!