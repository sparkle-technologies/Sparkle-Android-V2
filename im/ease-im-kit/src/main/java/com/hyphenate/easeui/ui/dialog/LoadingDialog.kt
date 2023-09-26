package com.hyphenate.easeui.ui.dialog

import android.content.Context
import android.util.Log
import com.drake.tooltip.dialog.BubbleDialog
import com.hyphenate.easeui.utils.getWrappedActivity
import java.lang.ref.WeakReference

private const val TAG = "LoadingDialog"

interface ILoadingDialog {
    fun show(context: Context)
    fun hide()
}

object LoadingDialogHolder {

    private var loadingDialog: ILoadingDialog? = null

    fun setLoadingDialog(loadingDialog: ILoadingDialog) {
        LoadingDialogHolder.loadingDialog = loadingDialog
    }

    fun getLoadingDialog(): ILoadingDialog? {
        return loadingDialog
    }
}

class LoadingDialog {

    companion object : ILoadingDialog {
        private var cache: WeakReference<BubbleDialog>? = null

        override fun show(context: Context) {
            ThreadUtil.assertInMainThread()
            val a = context.getWrappedActivity() ?: return
            val dialog = cache?.get()
            if (dialog != null) {
                val wrappedActivity = dialog.context.getWrappedActivity()
                if (a == wrappedActivity) {
                    Log.d(TAG, "use cache")
                    dialog.show()
                    return
                } else {
                    Log.d(TAG, "lifecycle owner mismatch")
                    try {
                        dialog.dismiss()
                    } catch (e: Exception) {
                        Log.e(TAG, Log.getStackTraceString(e))
                    }
                }
            } else {
                Log.d(TAG, "no cache")
            }
            BubbleDialog(context, "loading").also {
                cache = WeakReference(it)
                it.show()
            }
        }

        override fun hide() {
            ThreadUtil.assertInMainThread()
            cache?.get()?.dismiss()
        }
    }
}

