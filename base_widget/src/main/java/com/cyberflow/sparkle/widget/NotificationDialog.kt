package com.cyberflow.sparkle.widget

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.lang.Math.abs
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue

private val TAG = "NotificationDialog"

interface IToastDialog {
    fun show(context: Context, type: Int, content: String)
}

object ToastDialogHolder {

    private var toastDialog: IToastDialog? = null

    fun setDialog(dialog: IToastDialog) {
        toastDialog = dialog
    }

    fun getDialog(): IToastDialog? {
        return toastDialog
    }
}


class ToastDialog {

    companion object : IToastDialog {
        private var cache: WeakReference<NotificationDialog>? = null
        var count = 1

        override fun show(context: Context, type: Int, content: String) {
            if(Settings.canDrawOverlays(context)){
                assertInMainThread()
                val dialog = cache?.get()
                if (dialog != null) {
                    dialog.addMsg(type, content)
                    return
                } else {
                    Log.d(TAG, "no cache")
                }
                NotificationDialog(context).also {
                    cache = WeakReference(it)
                    it.addMsg(type, content)
                }
            }else{
                if(count <= 3){
                    Toast.makeText(context, context.getString(R.string.permission_hint), Toast.LENGTH_SHORT).show()
                    count++
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
//                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                    intent.setAction(Settings.ACTION_SETTINGS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }else{
                    Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

fun assertInMainThread(inOrNot: Boolean = true) {
    val condition = if (inOrNot) {
        !isMainThread()
    } else {
        isMainThread()
    }
    if (condition) {
        val msg = "cannot be invoked in thread ${Thread.currentThread().id}"
        Log.e(TAG, msg)
    }
}

fun isMainThread(): Boolean {
    return Looper.getMainLooper().thread == Thread.currentThread()
}


class NotificationDialog(context: Context) : Dialog(context, com.cyberflow.base.resources.R.style.share_dialog) {

    private var mListener: OnNotificationClick? = null
    private var mStartY: Float = 0F
    private var mView: View? = null
    private var mHeight: Int? = 0

    init {
        mView = LayoutInflater.from(context).inflate(R.layout.common_layout_notifacation, null)
    }

    private val TAG = "NotificationDialog"
    var iv: ImageView? = null
    var tv: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
//        Log.e(TAG, "onCreate: ")
        super.onCreate(savedInstanceState)
        setContentView(mView!!)
        iv = mView?.findViewById(R.id.iv)
        tv = mView?.findViewById(R.id.tv)
        window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        window?.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
        val layoutParams = window?.attributes

        val dm: DisplayMetrics = context.resources.displayMetrics
        val width = (dm.widthPixels.toFloat() * 335 / 375).toInt()
        layoutParams?.width = width
        layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        window?.attributes = layoutParams
        window?.setWindowAnimations(com.cyberflow.base.resources.R.style.dialog_animation)
        //按空白处不能取消
        setCanceledOnTouchOutside(false)

        setOnDismissListener {
//            Log.e(TAG, "onDismiss: ")
            afterDismiss()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isOutOfBounds(event)) {
                    mStartY = event.y
                }
            }

            MotionEvent.ACTION_UP -> {
                if (mStartY > 0 && isOutOfBounds(event)) {
                    val moveY = event.y
                    if (abs(mStartY - moveY) >= 15) {  //滑动超过20认定为滑动事件
                        dismiss()  //Dialog消失
                    } else {                //认定为点击事件
                        //Dialog的点击事件
                        mListener?.onClick()
                    }
                    dismiss()
                }
            }
        }
        return false
    }

    private fun isOutOfBounds(event: MotionEvent): Boolean {
        val yValue = event.y
        if (yValue > 0 && yValue <= (mHeight ?: (0 + 40))) {
            return true
        }
        return false
    }

    private fun setDialogSize() {
        mView?.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            mHeight = v?.height
        }
    }

    data class ToastBody(val type: Int, val content: String)

    companion object {
        const val TYPE_SUCCESS = 0
        const val TYPE_ERROR = 1
        const val TYPE_WARN = 2

        const val EVENT_SUCCESS = "event_toast_success"
        const val EVENT_ERROR = "event_toast_error"
        const val EVENT_WARN = "event_toast_warn"
    }

    private val queue = ConcurrentLinkedQueue<ToastBody>()

    fun addMsg(type: Int, content: String) {
//        Log.e(TAG, "addMsg: ", )
        queue.add(ToastBody(type, content))
        showDialogAutoDismiss()
    }

    fun addMsg(content: String){
        queue.add(ToastBody(0, content))
    }
    private fun afterDismiss() {
//        Log.e(TAG, "afterDismiss: ", )
        if(queue.isNotEmpty()){
            showDialogAutoDismiss()
        }
    }

    private fun initData() {
//        Log.e(TAG, "initData: ")
        if(queue.isNotEmpty()){
            val toastBody = queue.remove()
            iv?.setImageResource(when (toastBody.type) {
                TYPE_SUCCESS -> com.cyberflow.base.resources.R.drawable.toast_ic_success
                TYPE_ERROR -> com.cyberflow.base.resources.R.drawable.toast_ic_error
                TYPE_WARN -> com.cyberflow.base.resources.R.drawable.toast_ic_warn
                else -> 0
            })
            tv?.text = toastBody.content
        }
    }

   fun showDialogAutoDismiss() {
//       Log.e(TAG, "showDialogAutoDismiss: ", )
        if (!isShowing && queue.isNotEmpty()) {
            if(isMainThread()){
                show()
                initData()
                setDialogSize()
                var time = 3000L
                if(queue.size > 1){
                    time = 1000L
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    assertInMainThread()
                    if (isShowing) {
                        dismiss()
                    }
                }, time)
            }
        }
    }

    //处理通知的点击事件
    fun setOnNotificationClickListener(listener: OnNotificationClick) {
        mListener = listener
    }

    interface OnNotificationClick {
        fun onClick()
    }
}