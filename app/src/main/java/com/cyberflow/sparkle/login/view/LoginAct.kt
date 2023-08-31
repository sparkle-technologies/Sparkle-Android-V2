package com.cyberflow.sparkle.login.view

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseVBAct
import com.cyberflow.sparkle.databinding.ActivityLoginBinding
import com.cyberflow.sparkle.login.viewmodel.LoginViewModel
import com.cyberflow.sparkle.login.widget.ShadowTxtButton
import com.cyberflow.sparkle.register.view.RegisterAct
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class LoginAct : BaseVBAct<LoginViewModel, ActivityLoginBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        initAnim()

        mViewBind.btnWalletLogin.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked() {
                Log.e(TAG, "initView:  button clicked")
                val intent = Intent(this@LoginAct, RegisterAct::class.java)
                startActivity(intent)
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