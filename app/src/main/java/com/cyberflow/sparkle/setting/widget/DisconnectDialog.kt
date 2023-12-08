package com.cyberflow.sparkle.setting.widget

import android.app.Dialog
import android.graphics.Typeface
import android.text.style.AbsoluteSizeSpan
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.cyberflow.base.model.BindBean
import com.cyberflow.base.net.Api
import com.cyberflow.base.resources.R
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.drake.net.Post
import com.drake.net.utils.scopeDialog
import com.drake.spannable.replaceSpanFirst
import com.drake.spannable.span.HighlightSpan

class DisconnectDialog {

    private val TAG = "DisconnectDialog"
    private var context: FragmentActivity? = null
    private var bindBean: BindBean? = null
    private var mDialog: Dialog? = null

    constructor(c: FragmentActivity, bind: BindBean?, callback: Callback) {
        if (c == null || callback == null) {
            return
        }

        context = c
        bindBean = bind
        mCallback = callback
        initView()
        initData()
    }

    private var tvTitle: TextView? = null
    private var tvHint: TextView? = null
    private var btnDisconnect: ShadowTxtButton? = null

    private fun initView() {
        mDialog = Dialog(context!!, R.style.forward_dialog)
        mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog?.setContentView(com.cyberflow.sparkle.R.layout.dialog_disconnect)
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
            btnDisconnect = findViewById<ShadowTxtButton>(com.cyberflow.sparkle.R.id.btn_disconnect)

            btnDisconnect?.setClickListener(object : ShadowTxtButton.ShadowClickListener {
                override fun clicked(disable: Boolean) {
                    request()
                }
            })
        }
    }

    interface Callback {
        fun onSelected(select: Boolean)
    }

    private var mCallback: Callback? = null

    fun updateData(bind: BindBean?) {
        bindBean = bind
        initData()
    }

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
        prepare()
    }

    private fun prepare() {
        Log.e(TAG, "prepare: $bindBean" )
        bindBean?.let {
            val first = context?.getString(com.cyberflow.sparkle.R.string.dis_1)
            val typeName = if(it.type == "MetaMask") context?.getString(com.cyberflow.sparkle.R.string.dis_2) else it.type
            val account = context?.getString(com.cyberflow.sparkle.R.string.dis_3)
            val full = "$first$typeName$account${it.nick}"

            tvHint?.text = full
                .replaceSpanFirst(it.nick, ignoreCase = true){
                    HighlightSpan("#737373", Typeface.defaultFromStyle(Typeface.BOLD))
                }
                .replaceSpanFirst(it.nick, ignoreCase = true){
                    AbsoluteSizeSpan(14, true)
                }
        }
    }

    private fun request() {
        context?.apply {
           scopeDialog {
               val data = Post<String>(Api.LOGIN_UNBIND) {
                   json("auth_type" to bindBean?.type.orEmpty())
               }.await()
               data?.let {
                   Log.e(TAG, "  ---- $it ", )
                   mCallback?.onSelected(true)
               }
            }
        }
    }
}
