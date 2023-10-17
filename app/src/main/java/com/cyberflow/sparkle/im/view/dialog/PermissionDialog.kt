package com.cyberflow.sparkle.im.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.annotation.StyleRes
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.login.widget.ShadowTxtButton

class PermissionDialog @JvmOverloads constructor(
    context: Context,
    var title: String = "",
    var content: String = "",
    var listener: PermissionClickListener? = null,
    @StyleRes themeResId: Int = com.cyberflow.base.resources.R.style.Permission_Dialog
) : Dialog(context, themeResId) {

    var tvTitle: TextView? = null
    var tvContent: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_permission)
        setCanceledOnTouchOutside(true)

        tvTitle = findViewById<TextView>(R.id.tv_title)
        tvContent = findViewById<TextView>(R.id.tv_content)

        tvTitle?.text = title
        tvContent?.text = content

        findViewById<ShadowTxtButton>(R.id.btn_cancel).setClickListener(object :
            ShadowTxtButton.ShadowClickListener {
            override fun clicked(disable: Boolean) {
                listener?.leftClicked()
                dismiss()
            }
        })

        findViewById<ShadowTxtButton>(R.id.btn_go_setting).setClickListener(object :
            ShadowTxtButton.ShadowClickListener {
            override fun clicked(disable: Boolean) {
                listener?.rightClicked()
                dismiss()
            }
        })
    }

    interface PermissionClickListener {
        fun leftClicked()
        fun rightClicked()
    }

    override fun show() {
        runMain {
            super.show()
        }
    }
}

fun runMain(block: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        block()
    } else {
        Handler(Looper.getMainLooper()).post { block() }
    }
}