package com.cyberflow.sparkle.setting.widget

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.cyberflow.base.resources.R
import com.cyberflow.sparkle.widget.ShadowTxtButton

class DisconnectHintDialog {

    private var context: Context? = null
    private var mDialog: Dialog? = null

    constructor(c: Context, callback: Callback) {
        if (c == null || callback == null) {
            return
        }

        context = c
        mCallback = callback
        initView()
        initData()
    }

    private var tvTitle: TextView? = null
    private var tvHint: TextView? = null
    private var btn: ShadowTxtButton? = null

    private fun initView() {
        mDialog = Dialog(context!!, R.style.forward_dialog)
        mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog?.setContentView(com.cyberflow.sparkle.R.layout.dialog_disconnect_hint)
//        mDialog?.setCancelable(false)
//        mDialog?.setCanceledOnTouchOutside(false)
        val window = mDialog?.window
        if (window != null) {
            val lp = window.attributes
            lp.gravity = Gravity.CENTER_VERTICAL
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = lp
        }

        mDialog?.apply {

            tvTitle = findViewById<TextView>(com.cyberflow.sparkle.R.id.tv_title)
            tvHint = findViewById<TextView>(com.cyberflow.sparkle.R.id.tv_hint)
            btn = findViewById<ShadowTxtButton>(com.cyberflow.sparkle.R.id.btn)

            btn?.setClickListener(object : ShadowTxtButton.ShadowClickListener {
                override fun clicked(disable: Boolean) {
                    mCallback?.onSelected(true)
                }
            })
        }
    }

    interface Callback {
        fun onSelected(select: Boolean)
    }

    private var mCallback: Callback? = null

    fun dismiss() {
        mDialog?.dismiss()
    }

    fun show() {
        mDialog?.show()
    }

    fun onDestroy() {
        if (mDialog != null) {
            mDialog!!.dismiss()
            mDialog = null
        }
    }


    private fun initData() {

    }
}
