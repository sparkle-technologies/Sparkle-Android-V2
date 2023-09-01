package com.cyberflow.sparkle

import com.cyberflow.base.BaseApp
import com.cyberflow.base.net.initNetSpark
import com.cyberflow.base.util.CacheUtil
import com.google.android.libraries.places.api.Places

class MyApp : BaseApp() {
    override fun onCreate() {
        super.onCreate()

        initNetSpark()
        CacheUtil.init(this)
        initGooglePlace()
    }


    private fun initGooglePlace(){
        Places.initialize(instance, "AIzaSyBx6ZuxE0RHbQRU7Ef7cxzHdh3VtinWE8I")
    }
}