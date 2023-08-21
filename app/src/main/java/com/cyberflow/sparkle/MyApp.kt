package com.cyberflow.sparkle

import com.cyberflow.base.BaseApp
import com.cyberflow.base.util.DisplayUtil

class MyApp : BaseApp() {
    override fun onCreate() {
        super.onCreate()
        DisplayUtil.init(this)
    }
}