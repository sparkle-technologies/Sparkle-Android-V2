package com.cyberflow.sparkle.profile.widget

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.cyberflow.base.resources.R

class ShareDialog : View.OnClickListener {

    private var mContext: Context? = null
    private var mCallback: Callback? = null

    private var mPickerDialog: Dialog? = null


    interface Callback {
        fun onTimeSelected(timestamp: Long)
    }

    constructor(context: Context, callback: Callback){
        if (context == null || callback == null) {
            return
        }

        mContext = context
        mCallback = callback

        initView()
    }

    private fun initView() {
        mPickerDialog = Dialog(mContext!!, R.style.share_dialog)
        mPickerDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mPickerDialog?.setContentView(com.cyberflow.sparkle.R.layout.dialog_share)
        val window = mPickerDialog?.window
        if (window != null) {
            val lp = window.attributes
            lp.gravity = Gravity.BOTTOM
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = lp
            window.setWindowAnimations(R.style.BottomDialog_Animation)
        }
//        mPickerDialog?.findViewById<View>(com.cyberflow.sparkle.R.id.tv_cancel)?.setOnClickListener(this)
//        mPickerDialog?.findViewById<View>(com.cyberflow.sparkle.R.id.tv_confirm)?.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {

    }

    fun show() {
        mPickerDialog!!.show()
    }

    fun onDestroy() {
        if (mPickerDialog != null) {
            mPickerDialog!!.dismiss()
            mPickerDialog = null
        }
    }
}