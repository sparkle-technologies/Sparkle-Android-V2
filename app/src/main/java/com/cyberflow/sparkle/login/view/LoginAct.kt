package com.cyberflow.sparkle.login.view

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseVBAct
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.sparkle.databinding.ActivityLoginBinding
import com.cyberflow.sparkle.login.viewmodel.LoginRegisterViewModel
import com.cyberflow.sparkle.login.widget.ShadowImgButton
import com.cyberflow.sparkle.login.widget.ShadowTxtButton
import com.cyberflow.sparkle.register.view.RegisterAct
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class LoginAct : BaseVBAct<LoginRegisterViewModel, ActivityLoginBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        initAnim()
        mViewBind.btnTwitterLogin.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                viewModel.login(LoginWeb3AuthUnipassAct.testAccount[2], "MetaMask")
            }
        })

        mViewBind.btnWalletLogin.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked(dis: Boolean) {
                Log.e(TAG, "initView:  button clicked")
                val intent = Intent(this@LoginAct, RegisterAct::class.java)
                startActivity(intent)
            }
        })
    }

    override fun initData() {
        viewModel.userInfo.observe(this) {
            Log.e(TAG, "initView: $it")
            CacheUtil.setUserInfo(it)

            val token = CacheUtil.getUserInfo()?.token.orEmpty()
            Log.e(TAG, "got  token from login :  $token")
        }
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