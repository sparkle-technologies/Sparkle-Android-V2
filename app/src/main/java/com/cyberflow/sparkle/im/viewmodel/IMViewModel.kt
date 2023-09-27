package com.cyberflow.sparkle.im.viewmodel

import androidx.lifecycle.MutableLiveData
import com.cyberflow.base.model.LoginResponseData
import com.cyberflow.base.model.RegisterRequestBean
import com.cyberflow.base.viewmodel.BaseViewModel

class IMViewModel : BaseViewModel() {

    var userInfo: MutableLiveData<LoginResponseData> = MutableLiveData()


}