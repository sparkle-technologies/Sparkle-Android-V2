package com.hyphenate.easeui.ui.dialog

import android.content.Context
import androidx.appcompat.app.AppCompatDialog

open class BaseDialog @JvmOverloads constructor(
    context: Context,
    theme: Int = 0
) : AppCompatDialog(context, theme)