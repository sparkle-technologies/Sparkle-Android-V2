package com.cyberflow.sparkle.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.scopeLife
import com.cyberflow.base.net.Api
import com.cyberflow.base.viewmodel.BaseViewModel
import com.drake.net.Post

class LoginVM : BaseViewModel() {

    var userInfo: MutableLiveData<UserLoginBean> = MutableLiveData()

    fun login(address : String, wallet: String) = scopeLife {
        userInfo.value = Post<UserLoginBean>(Api.SIGN_IN){
            json("auth_msg" to address, "type" to wallet)
        }.await()
    }
}