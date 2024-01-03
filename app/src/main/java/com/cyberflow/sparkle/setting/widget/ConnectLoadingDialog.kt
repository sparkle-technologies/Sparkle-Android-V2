package com.cyberflow.sparkle.setting.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.RotateDrawable
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.cyberflow.base.resources.R

class ConnectLoadingDialog {

    private var context: Context? = null
    private var mDialog: Dialog? = null
    private var title = ""
    private var txt = ""

    private var tvTitle: TextView? = null
    private var tvTxt: TextView? = null

    constructor(c: Context, _title: String, _txt: String, callback: Callback) {
        if (c == null || callback == null) {
            return
        }

        context = c
        title = _title
        txt = _txt
        mCallback = callback
        initView()
        initData()
    }

    private var iv: ImageView? = null

    private fun initView() {
        mDialog = Dialog(context!!, R.style.forward_dialog)
        mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog?.setContentView(com.cyberflow.sparkle.R.layout.dialog_connect_loading)
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
            iv = findViewById(com.cyberflow.sparkle.R.id.iv)
            tvTitle = findViewById(com.cyberflow.sparkle.R.id.tv_title)
            tvTxt = findViewById(com.cyberflow.sparkle.R.id.tv_txt)
            initAnim()
            tvTitle?.text = title
            tvTxt?.text = txt
        }
    }

    fun updateUi(title: String, txt: String){
        tvTitle?.text = title
        tvTxt?.text = txt
    }

    fun dismiss(){
        mDialog?.dismiss()
    }

    interface Callback {
        fun onSelected(select: Boolean)
    }

    private var mCallback: Callback? = null

    fun show() {
        mDialog?.show()
    }

    private fun initAnim() {
        val rotateDrawable = iv?.background as RotateDrawable
        ObjectAnimator.ofInt(rotateDrawable, "level", 0, 10000).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
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
