package com.cyberflow.sparkle.login

import android.os.Bundle
import android.util.Log
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.act.BaseVBAct
import com.cyberflow.base.ext.COLOR_TRANSPARENT
import com.cyberflow.base.ext.immersive
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.databinding.ActivityLoginBinding
import com.cyberflow.sparkle.databinding.ActivityMainBinding

/**
 * 分包
 */
class LoginAct : BaseVBAct<LoginVM, ActivityLoginBinding>() {

    val testAccount = arrayListOf(
        "0x150E4AB89Ddd5fa7f8Fb8cae501b48961Ce703A4",
        "0x0c42ad43BEEDaCe4927E1065c10C776f2C604b5C",
        "0x73cf3CB3dc0D6872878a316509aFb7510E7cd44d",
        "0xE1c026085863e37321DbF7871c6d28a79153c888",
        "0xDc58a843c8096943Ca2899b31Db004eB8B417C13",  //account5  new one
    )

    override fun initView(savedInstanceState: Bundle?) {
        viewModel.userInfo.observe(this){
            Log.e(TAG, "initView: $it" )
            mViewBind.tvMsg.text = "$it"
        }

        mViewBind.btnLogin.setOnClickListener {
            viewModel.login(testAccount[2], "MetaMask")
        }
    }

    override fun initData() {

    }
}