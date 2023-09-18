package com.cyberflow.sparkle

import com.cyberflow.base.BaseApp
import com.cyberflow.base.net.initNetSpark
import com.cyberflow.base.util.CacheUtil
import com.drake.brv.utils.BRV
import com.google.android.libraries.places.api.Places
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout

class MyApp : BaseApp() {
    override fun onCreate() {
        super.onCreate()

        BRV.modelId = BR.m
        initNetSpark()
        CacheUtil.init(this)
        initGooglePlace()
        initRefresh()
        Logger.addLogAdapter(AndroidLogAdapter())
    }


    private fun initGooglePlace(){
        Places.initialize(instance, "AIzaSyBx6ZuxE0RHbQRU7Ef7cxzHdh3VtinWE8I")
    }

    private fun initRefresh(){
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            ClassicsHeader(this)
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
            ClassicsFooter(this)
        }
    }
}