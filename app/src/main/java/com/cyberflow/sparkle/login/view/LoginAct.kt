package com.cyberflow.sparkle.login.view

import android.animation.Animator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseVBAct
import com.cyberflow.base.resources.R
import com.cyberflow.sparkle.databinding.ActivityLoginBinding
import com.cyberflow.sparkle.login.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class LoginAct : BaseVBAct<LoginViewModel, ActivityLoginBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        initAnim()

        mViewBind.btnWalletLogin.setOnClickListener {
            Log.e(TAG, "initView:  button clicked")
        }

        animBtn()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun animBtn() {
        mViewBind.nextButton.setOnClickListener {

        }

        mViewBind.nextButton.setOnTouchListener { v, motionEvent ->
            staticButtonTouchAnim(motionEvent, mViewBind.nextButton, mViewBind.nextButtonShadow, mViewBind.nextButtonTextView,
                R.drawable.button_start, R.drawable.button_start_shadow)
             false
        }
    }


    private fun staticButtonTouchAnim(
        motionEvent: MotionEvent, button: ImageView, buttonShadow: ImageView, buttonText: TextView,
        buttonBackground: Int, buttonShadowBackground: Int
    ) {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                button.visibility = View.INVISIBLE
                buttonText.visibility = View.INVISIBLE
                buttonShadow.setImageResource(buttonBackground)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                button.visibility = View.VISIBLE
                buttonText.visibility = View.VISIBLE
                buttonShadow.setImageResource(buttonShadowBackground)
            }
        }
    }

    override fun initData() {

    }


    private fun initAnim() {
        lifecycleScope.launch {
            execLottieAnim()
        }
    }

    private suspend fun execLottieAnim() {
        suspendCoroutine {
            val lottieView = mViewBind.lavHomepage
            lottieView.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    Log.d(TAG, "lottieView is start")
                }

                override fun onAnimationEnd(animation: Animator) {
                    Log.d(TAG, "lottieView is end")
                    it.resume(Unit)
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
        }
    }
}