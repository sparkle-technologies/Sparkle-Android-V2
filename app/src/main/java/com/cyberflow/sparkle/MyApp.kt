package com.cyberflow.sparkle

import android.util.Log
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
import dev.pinkroom.walletconnectkit.sign.dapp.WalletConnectKit

class MyApp : BaseApp() {

    companion object {

        const val TAG = "MyApp"

        lateinit var instance: MyApp

    }

    var walletConnectKit: WalletConnectKit? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        var st = System.currentTimeMillis()

        BRV.modelId = BR.m
        initNetSpark(cacheDir)
        CacheUtil.init(this)
        initGooglePlace()
        initRefresh()
        Logger.addLogAdapter(AndroidLogAdapter())
        Log.e(TAG, "onCreate:  time cost: ${System.currentTimeMillis() - st}")
    }

    private fun initGooglePlace() {
        Places.initialize(instance, "AIzaSyBx6ZuxE0RHbQRU7Ef7cxzHdh3VtinWE8I")
    }

    private fun initRefresh() {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            ClassicsHeader(this)
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
            ClassicsFooter(this)
        }
    }



}