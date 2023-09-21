package com.hyphenate.easeui.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.constraintlayout.widget.ConstraintLayout
import com.hyphenate.easeui.databinding.LoadingLayoutBinding
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

class LoadingDialog(context: Context) : BaseDialog(context) {

    companion object : ILoadingDialog {
        private var cache: WeakReference<LoadingDialog>? = null

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
            LoadingDialog(context).also {
                cache = WeakReference(it)
                it.show()
            }
        }

        override fun hide() {
            ThreadUtil.assertInMainThread()
            cache?.get()?.dismiss()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val l = LoadingLayout(context)
        setContentView(l, ViewGroup.LayoutParams(-2, -2))
    }

    override fun show() {
        super.show()
//        removeDialogBorder(this, .6f)
        removeDialogBorder(this, 0f)
    }

    private fun removeDialogBorder(dialog: Dialog, dimAccount: Float) {
        val window = dialog.window
        if (window != null) {
            val layoutParams = window.attributes
            layoutParams.gravity = Gravity.CENTER
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.dimAmount = dimAccount
            window.decorView.setPadding(0, 0, 0, 0)
            window.decorView.setBackgroundColor(Color.TRANSPARENT)

            window.setWindowAnimations(com.hyphenate.easeui.R.style.PictureThemeDialogWindowStyle)
            window.attributes = layoutParams
            window.setBackgroundDrawable(null)
        }
    }
}

class LoadingLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var _binding: LoadingLayoutBinding? = null
    private val binding: LoadingLayoutBinding get() = _binding!!
    private var anim: Animation? = null

    init {
        initView()
    }

    private fun initView() {
        _binding = LoadingLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private fun startAnim() {
        if (anim == null) {
            anim = RotateAnimation(
                360.0f,
                0.0f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            ).apply {
                interpolator = LinearInterpolator()
                repeatCount = Int.MAX_VALUE
                duration = 600
                fillAfter = true
            }
        }
        binding.pbLoading.startAnimation(anim)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //startAnim()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        /*anim?.let {
            try {
                it.cancel()
            } catch (e: java.lang.Exception) {
                LogUtil.d(TAG, Log.getStackTraceString(e))
            }
            anim = null
        }*/
    }
}