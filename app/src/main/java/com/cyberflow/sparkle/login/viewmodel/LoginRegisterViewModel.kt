package com.cyberflow.sparkle.login.viewmodel

import androidx.lifecycle.MutableLiveData
import com.cyberflow.base.model.LoginResponseData
import com.cyberflow.base.model.RegisterRequestBean
import com.cyberflow.base.viewmodel.BaseViewModel

class LoginRegisterViewModel : BaseViewModel() {

    var userInfo: MutableLiveData<LoginResponseData> = MutableLiveData()

    var registerBean : RegisterRequestBean? = null

    var previous: MutableLiveData<Int> = MutableLiveData()
    var next: MutableLiveData<Int> = MutableLiveData()

    fun clickPrevious() {
        previous.value = next.value?.minus(1)
    }

    fun clickNext() {
        next.value = next.value?.plus(1)
    }
}