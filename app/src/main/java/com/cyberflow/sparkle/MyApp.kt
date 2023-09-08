package com.cyberflow.sparkle

import com.cyberflow.base.BaseApp
import com.cyberflow.base.net.initNetSpark
import com.cyberflow.base.util.CacheUtil
import com.google.android.libraries.places.api.Places
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

class MyApp : BaseApp() {
    override fun onCreate() {
        super.onCreate()

        initNetSpark()
        CacheUtil.init(this)
        initGooglePlace()

        Logger.addLogAdapter(AndroidLogAdapter())
    }


    private fun initGooglePlace(){
        Places.initialize(instance, "AIzaSyBx6ZuxE0RHbQRU7Ef7cxzHdh3VtinWE8I")
    }
}