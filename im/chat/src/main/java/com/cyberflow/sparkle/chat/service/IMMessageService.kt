package com.cyberflow.sparkle.chat.service

import com.cyberflow.base.util.callback.IMV2Callback


interface IMMessageService  {
    fun onLoginSuccess(callback: IMV2Callback<Boolean>)
    fun onLogout(callback: IMV2Callback<Boolean>)

}