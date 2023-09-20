package com.cyberflow.base

import android.app.Application

open class BaseApp : Application() {

    companion object {
        @JvmStatic
        var instance: BaseApp? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}