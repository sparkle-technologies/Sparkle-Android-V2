package com.cyberflow.sparkle.login.view

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseVBAct
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.sparkle.MyApp
import com.cyberflow.sparkle.databinding.ActivityLoginBinding
import com.cyberflow.sparkle.login.viewmodel.LoginRegisterViewModel
import com.cyberflow.sparkle.login.widget.ShadowImgButton
import com.cyberflow.sparkle.main.view.MainActivity
import com.cyberflow.sparkle.register.view.RegisterAct
import dev.pinkroom.walletconnectkit.core.accounts
import dev.pinkroom.walletconnectkit.core.sessions
import dev.pinkroom.walletconnectkit.sign.dapp.sample.main.Content
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class LoginAct : BaseVBAct<LoginRegisterViewModel, ActivityLoginBinding>() {

    companion object {
        fun go(context: Context) {
            val intent = Intent(context, LoginAct::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }


    override fun initView(savedInstanceState: Bundle?) {
        initAnim()

        if (CacheUtil.getUserInfo()?.user?.open_uid?.isNotEmpty() == true) {
            MainActivity.go(this)
            return
        }

        initWalletConnect()

        mViewBind.btnGoogleLogin.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {

            }
        })

        mViewBind.btnIgLogin.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                //viewModel.login(LoginWeb3AuthUnipassAct.testAccount[2], "MetaMask")

            }
        })

        mViewBind.btnTwitterLogin.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                viewModel.loginTwitter(this@LoginAct)
            }
        })
    }

    override fun initData() {
        viewModel.userInfo.observe(this) {
            CacheUtil.setUserInfo(it)

            MyApp.instance.signInJWT(it.id_token)

            if (it.user?.open_uid.isNullOrEmpty()) {
                RegisterAct.go(this)
            } else {
                MainActivity.go(this)
            }
        }
    }

    /********************* wallet connect ******************************/

    private fun initWalletConnect() {
        mViewBind.composeView.setContent {
            MyApp.instance.walletConnectKit?.let { Content(it) }
        }

        lifecycleScope.launch {
            MyApp.instance.walletConnectKit?.activeSessions?.collect {
                if (it.isNotEmpty()) {
                    it.forEach { session ->
                        // Session(
                        //      pairingTopic=ab511d8e56f0a02dbd90e0ea17c777a5d30ac2f7c059133c4d7bb04144062c01,
                        //      topic=7524cc32f3a334290c29d256423d2a9ddb795a31c4e7b4a22deeef6e71fb8e84, expiry=1692258984,
                        //      namespaces={
                        //                  eip155=Session(
                        //                          chains=[eip155:1, eip155:5],
                        //                          accounts=[eip155:1:0x150E4AB89Ddd5fa7f8Fb8cae501b48961Ce703A4, eip155:5:0x150E4AB89Ddd5fa7f8Fb8cae501b48961Ce703A4],
                        //                          methods=[personal_sign, eth_signTypedData, eth_signTypedData_v3, eth_signTypedData_v4, eth_sendTransaction],
                        //                          events=[accountsChanged, chainChanged, disconnect, session_delete, display_uri, connect]
                        //                   )},
                        //     metaData=AppMetaData(name=MetaMask Wallet, description=MetaMask Wallet Integration, url=https://metamask.io/, icons=[], redirect=null, verifyUrl=null))
                        Log.e(TAG, "session foreach: $session ")
                        // Account(
                        //  topic=7524cc32f3a334290c29d256423d2a9ddb795a31c4e7b4a22deeef6e71fb8e84,
                        //  address=0x150E4AB89Ddd5fa7f8Fb8cae501b48961Ce703A4,
                        //  chainNamespace=eip155, chainReference=1, name=MetaMask Wallet, icon=null)
                        session.accounts.forEach { ac ->
                            Log.e(TAG, "accounts.forEach: $ac")
                        }
                    }

                    val address = it.flatMap { it.accounts }.map { it.address to it }
                    address.forEach {
                        Log.e(TAG, "address foreach:  ${it.first}   ${it.second} ")
                    }

                    val activated = MyApp.instance.walletConnectKit?.activeAccount
                    activated?.also { ac ->
                        val name = sessions.first().metaData?.name.orEmpty()
                        CacheUtil.savaString(CacheUtil.LOGIN_METHOD, name)
                        var wallet = "MetaMask"
                       /* scopeDialog {
                           val data = Post<LoginResponseData>(Api.SIGN_IN) {
                                //登录类型 MetaMask、WalletConnect、Coinbase、Twitter、Discord
                                //钱包地址 | Twitter授权code | Discord授权token
                                json("auth_msg" to ac.address, "type" to wallet)
                            }.await()
                            viewModel.userInfo.postValue(data)
                        }*/
                        viewModel.login(ac.address, wallet)
                    }
                }
            }
        }
    }

    /********************* anim ******************************/

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