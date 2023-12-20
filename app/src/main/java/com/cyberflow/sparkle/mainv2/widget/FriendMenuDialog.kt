package com.cyberflow.sparkle.mainv2.widget

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.cyberflow.base.resources.R
import com.cyberflow.base.util.dp2px

class FriendMenuDialog {

    private var mContext: Context? = null
    private var mCallback: Callback? = null
    private var mDialog: Dialog? = null

    interface Callback {
        fun onSelected(idx: Int)
    }

    companion object{
        const val IDX_ADD = 0
        const val IDX_CONTACTS = 1
        const val IDX_SCAN = 2
    }

    constructor(context: Context, callback: Callback) {
        if (context == null || callback == null) {
            return
        }

        mContext = context
        mCallback = callback

        initView()
        initData()
    }

    private fun initData() {

    }

    private var lay_add_friends: LinearLayout? = null
    private var lay_contacts: LinearLayout? = null
    private var lay_scan: LinearLayout? = null
    private var tv_num: TextView? = null
    private fun initView() {
        mDialog = Dialog(mContext!!, R.style.share_dialog)
        mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog?.setContentView(com.cyberflow.sparkle.R.layout.dialog_friend_menu)
//        mDialog?.setCancelable(false)
//        mDialog?.setCanceledOnTouchOutside(true)
        val window = mDialog?.window
        if (window != null) {
            val lp = window.attributes
            lp.y = dp2px(48f)
            lp.x = dp2px(20f)
            lp.gravity = Gravity.TOP or Gravity.RIGHT
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = lp
//            window.setWindowAnimations(R.style.BottomDialog_Animation)
//            window.setFlags(
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//            )
        }

        mDialog?.apply {
            lay_add_friends = findViewById<LinearLayout>(com.cyberflow.sparkle.R.id.lay_add_friends)
            lay_contacts = findViewById<LinearLayout>(com.cyberflow.sparkle.R.id.lay_contacts)
            lay_scan = findViewById<LinearLayout>(com.cyberflow.sparkle.R.id.lay_scan)
            tv_num = findViewById<TextView>(com.cyberflow.sparkle.R.id.tv_num)

            lay_add_friends?.setOnClickListener {
                mCallback?.onSelected(IDX_ADD)
            }
            lay_contacts?.setOnClickListener {
                mCallback?.onSelected(IDX_CONTACTS)
            }
            lay_scan?.setOnClickListener {
                mCallback?.onSelected(IDX_SCAN)
            }
        }
    }

    fun showFriendRequestNum(totalUnread: Int){
        tv_num?.apply {
            if(isVisible){
                if(totalUnread > 0){
                    isVisible = true
                    if(totalUnread > 99){
                        text = "···"
                    }else{
                        text = "$totalUnread"
                    }
                }else{
                    isVisible = false
                }
            }
        }
    }

    fun click(){
        if(mDialog?.isShowing == true){
            mDialog?.dismiss()
        }else{
            mDialog?.show()
        }
    }

    fun show() {
        mDialog?.show()
    }

    fun hide(){
        mDialog?.dismiss()
    }
    fun onDestroy() {
        if (mDialog != null) {
            mDialog!!.dismiss()
            mDialog = null
        }
    }
}