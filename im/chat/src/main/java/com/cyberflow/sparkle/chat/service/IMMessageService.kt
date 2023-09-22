package com.cyberflow.sparkle.chat.service

import android.content.Context
import com.cyberflow.base.util.callback.IMV2Callback


interface IMMessageService  {
    fun init(context: Context)

    fun onLoginSuccess(callback: IMV2Callback<Boolean>)
    fun onLogout(callback: IMV2Callback<Boolean>)

}