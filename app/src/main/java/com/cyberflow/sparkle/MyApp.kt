package com.cyberflow.sparkle

import com.cyberflow.base.BaseApp
import com.cyberflow.base.net.initNetSpark
import com.cyberflow.base.util.CacheUtil

class MyApp : BaseApp() {
    override fun onCreate() {
        super.onCreate()

        initNetSpark()
        CacheUtil.init(this)
    }
}