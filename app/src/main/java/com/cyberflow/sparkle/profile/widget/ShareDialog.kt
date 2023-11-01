package com.cyberflow.sparkle.profile.widget

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.cyberflow.base.resources.R
import com.cyberflow.sparkle.widget.ShadowImgButton

class ShareDialog : View.OnClickListener {

    private var mContext: Context? = null
    private var mCallback: Callback? = null

    private var mDialog: Dialog? = null


    interface Callback {
        fun onSelected(openUid: String, type: Int)
    }

    companion object{
        const val TYPE_SHARE = 0
        const val TYPE_MORE = 1
        const val TYPE_COPY_LINK = 2
        const val TYPE_DOWNLOAD = 3
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
        mDialog = Dialog(mContext!!, R.style.share_dialog)
        mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog?.setContentView(com.cyberflow.sparkle.R.layout.dialog_share)
//        mDialog?.setCancelable(false)
        mDialog?.setCanceledOnTouchOutside(false)
        val window = mDialog?.window
        if (window != null) {
            val lp = window.attributes
            lp.gravity = Gravity.BOTTOM
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = lp
            window.setWindowAnimations(R.style.BottomDialog_Animation)
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        }

        mDialog?.apply {
            findViewById<ShadowImgButton>(com.cyberflow.sparkle.R.id.btn_more).setClickListener(object : ShadowImgButton.ShadowClickListener{
                override fun clicked() {
                     mCallback?.onSelected("", TYPE_MORE)
                }
            })
            findViewById<ShadowImgButton>(com.cyberflow.sparkle.R.id.btn_copy_link).setClickListener(object : ShadowImgButton.ShadowClickListener{
                override fun clicked() {
                    mCallback?.onSelected("", TYPE_COPY_LINK)
                }
            })
            findViewById<ShadowImgButton>(com.cyberflow.sparkle.R.id.btn_download).setClickListener(object : ShadowImgButton.ShadowClickListener{
                override fun clicked() {
                    mCallback?.onSelected("", TYPE_DOWNLOAD)
                }
            })
        }
    }

    override fun onClick(p0: View?) {

    }

    fun show() {
        mDialog!!.show()
    }

    fun onDestroy() {
        if (mDialog != null) {
            mDialog!!.dismiss()
            mDialog = null
        }
    }
}