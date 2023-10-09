package com.cyberflow.sparkle

import android.util.Log
import com.cyberflow.base.BaseApp
import com.cyberflow.base.net.initNetSpark
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.sparkle.chat.IMManager
import com.drake.brv.utils.BRV
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import dev.pinkroom.walletconnectkit.core.WalletConnectKitConfig
import dev.pinkroom.walletconnectkit.sign.dapp.WalletConnectKit

class MyApp : BaseApp() {

    companion object {

        const val TAG = "MyApp"

        lateinit var instance: MyApp

    }

    var walletConnectKit: WalletConnectKit? = null

    fun checkWalletConnect(){
        if(walletConnectKit == null){
            val config = WalletConnectKitConfig(
                projectId = "216dc6e2b36be94b855cd28ea41fda6d",
                appUrl = "https://sparkle.fun",
            )
            walletConnectKit = WalletConnectKit.builder(MyApp.instance).config(config).build()
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        var st = System.currentTimeMillis()

        BRV.modelId = BR.m
        initNetSpark(cacheDir)
        CacheUtil.init(this)
        initRefresh()
        FirebaseApp.initializeApp(this)
        Logger.addLogAdapter(AndroidLogAdapter())

        IMManager.instance.initUI()
        IMManager.instance.initSDKAndDB(this)

        Log.e(TAG, "onCreate:  time cost: ${System.currentTimeMillis() - st}")
    }

    fun initGooglePlace() {
        if(!Places.isInitialized()){
            Places.initialize(instance, "AIzaSyBx6ZuxE0RHbQRU7Ef7cxzHdh3VtinWE8I")
        }
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