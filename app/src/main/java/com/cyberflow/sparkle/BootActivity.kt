package com.cyberflow.sparkle

import android.os.Bundle
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.callback.IMLoginResponse
import com.cyberflow.base.util.callback.IMV2Callback
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.chat.IMManager
import com.cyberflow.sparkle.databinding.ActivityBootBinding
import com.cyberflow.sparkle.login.view.LoginAct
import com.cyberflow.sparkle.mainv2.view.MainActivityV2

class BootActivity : BaseDBAct<BaseViewModel, ActivityBootBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        if (CacheUtil.getUserInfo()?.user?.open_uid?.isNotEmpty() == true) {
            imLogin()
        }
    }

    private fun imLogin(){
        IMManager.instance.loginToIM(object : IMV2Callback<IMLoginResponse> {
            override fun onEvent(event: IMLoginResponse) {
                if(event.success){
                    usedGun = true
                }
            }
        })
    }

    private var usedGun = false

    override fun initData() {
        mDataBinding.root.postDelayed({
            jump()
        }, 1 * 1000)
    }

    private fun jump(){
        if(usedGun){
            MainActivityV2.go(this)
        }else{
            LoginAct.go(this)
        }
    }
}