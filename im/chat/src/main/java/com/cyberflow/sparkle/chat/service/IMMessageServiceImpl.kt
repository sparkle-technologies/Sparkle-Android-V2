package com.cyberflow.sparkle.chat.service

import android.app.Application
import android.content.Context
import android.util.Log
import com.cyberflow.base.util.callback.IMV2Callback
import com.cyberflow.sparkle.chat.IMManager
import com.therouter.inject.ServiceProvider

private const val TAG = "IMMessageServiceImpl"

@ServiceProvider(returnType = IMMessageService::class)
class IMMessageServiceImpl : IMMessageService {

    private val im get() = IMManager.instance

    fun init(context: Context) {
        Log.e(TAG, "init")
        val app = context.applicationContext as Application
        im.init(app)
    }

    override fun onLoginSuccess(callback: IMV2Callback<Boolean>) {
        Log.e(TAG, "im login")
        im.login(callback)
    }

    override fun onLogout(callback: IMV2Callback<Boolean>) {
        Log.e(TAG, "logout")
        im.logout(callback)
    }
}