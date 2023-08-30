package com.cyberflow.sparkle.login.view

import android.animation.Animator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseVBAct
import com.cyberflow.base.resources.R
import com.cyberflow.sparkle.databinding.ActivityLoginBinding
import com.cyberflow.sparkle.login.viewmodel.LoginViewModel
import com.cyberflow.sparkle.login.widget.ShadowTxtButton
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class LoginAct : BaseVBAct<LoginViewModel, ActivityLoginBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        initAnim()

        mViewBind.btnWalletLogin.setOnClickListener {
            Log.e(TAG, "initView:  button clicked")
        }

        mViewBind.loginBtnShadow.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked() {
                 Toast.makeText(this@LoginAct, "click me", Toast.LENGTH_LONG).show()
            }
        })
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