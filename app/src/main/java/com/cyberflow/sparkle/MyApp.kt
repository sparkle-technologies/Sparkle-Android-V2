package com.cyberflow.sparkle

import android.content.ContentValues
import android.util.Log
import com.auth0.android.jwt.JWT
import com.cyberflow.base.BaseApp
import com.cyberflow.base.act.BaseVMAct
import com.cyberflow.base.net.initNetSpark
import com.cyberflow.base.util.CacheUtil
import com.drake.brv.utils.BRV
import com.google.android.libraries.places.api.Places
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.web3auth.singlefactorauth.SingleFactorAuth
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.SingleFactorAuthArgs
import com.web3auth.singlefactorauth.types.TorusKey
import dev.pinkroom.walletconnectkit.core.WalletConnectKitConfig
import dev.pinkroom.walletconnectkit.sign.dapp.WalletConnectKit
import org.torusresearch.fetchnodedetails.types.TorusNetwork
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class MyApp : BaseApp() {


    companion object {

        const val TAG = "MyApp"

        lateinit var instance: MyApp

    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        var st = System.currentTimeMillis()

        Log.e(TAG, "onCreate: ---0--- time cost: ${System.currentTimeMillis() - st}")
        st = System.currentTimeMillis()

        BRV.modelId = BR.m
        initNetSpark(cacheDir) // 34ms

        Log.e(TAG, "onCreate: ---1--- time cost: ${System.currentTimeMillis() - st}")
        st = System.currentTimeMillis()

        CacheUtil.init(this)

        Log.e(TAG, "onCreate: ---2--- time cost: ${System.currentTimeMillis() - st}")
        st = System.currentTimeMillis()

        initGooglePlace()

        Log.e(TAG, "onCreate: ---3--- time cost: ${System.currentTimeMillis() - st}")
        st = System.currentTimeMillis()

        initWalletConnect()  // 2495ms

        Log.e(TAG, "onCreate: ---4--- time cost: ${System.currentTimeMillis() - st}")
        st = System.currentTimeMillis()

        initWeb3Auth()  // 739ms

        Log.e(TAG, "onCreate: ---5--- time cost: ${System.currentTimeMillis() - st}")
        st = System.currentTimeMillis()

        initRefresh()

        Log.e(TAG, "onCreate: ---6--- time cost: ${System.currentTimeMillis() - st}")
        st = System.currentTimeMillis()

        Logger.addLogAdapter(AndroidLogAdapter())

        Log.e(TAG, "onCreate: ---7--- time cost: ${System.currentTimeMillis() - st}")
        st = System.currentTimeMillis()
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

    var walletConnectKit: WalletConnectKit? = null

    // TheRouterFlowTask.APP_ONCREATE：当Application的onCreate()执行后初始化
    // TheRouterFlowTask.APP_ONSPLASH：当应用的首个Activity.onCreate()执行后初始化
//    @FlowTask(taskName = "walletConnect", dependsOn = TheRouterFlowTask.APP_ONSPLASH, async = true)
    fun initWalletConnect() {
        Log.e(TAG, "initWalletConnect: ")
        val config = WalletConnectKitConfig(
            projectId = "216dc6e2b36be94b855cd28ea41fda6d",
            appUrl = "https://sparkle.fun",
        )
        walletConnectKit = WalletConnectKit.builder(instance).config(config).build()
    }

    /************************************** 这个以后再弄  得搞个新接口刷新JWT ***********************************************/

    private lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var singleFactorAuthArgs: SingleFactorAuthArgs
    private lateinit var loginParams: LoginParams
    private var torusKey: TorusKey? = null
    private var publicAddress: String = ""

//    @FlowTask(taskName = "web3auth", dependsOn = TheRouterFlowTask.APP_ONSPLASH, async = true)
    fun initWeb3Auth() {
        Log.e(TAG, "initWeb3Auth: ")
        singleFactorAuthArgs = SingleFactorAuthArgs(TorusNetwork.TESTNET)
        singleFactorAuth = SingleFactorAuth(singleFactorAuthArgs)

        val sessionResponse: CompletableFuture<TorusKey> = singleFactorAuth.initialize(instance)
        sessionResponse.whenComplete { torusKey, error ->
            if (torusKey != null) {
                publicAddress = torusKey?.publicAddress.toString()
                Log.e(
                    BaseVMAct.TAG,
                    "Private Key: ${torusKey.privateKey?.toString(16)}".trimIndent()
                )
            } else {
                Log.e(BaseVMAct.TAG, error.message ?: "Something went wrong")
            }
        }
    }

    fun signInJWT(idToken: String) {
        Thread {
            val jwt = JWT(idToken)
            val issuer = jwt.issuer //get registered claims
            Log.d(ContentValues.TAG, "Issuer = $issuer")
            val open_id = jwt.getClaim("open_id").asString() // get sub claims
            Log.d(ContentValues.TAG, "open_id = $open_id")

            loginParams = LoginParams("Sparkle-Custom-Auth-01", "$open_id", "$idToken")
            try {
                torusKey = singleFactorAuth.getKey(loginParams, instance, 86400).get()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            publicAddress = "${torusKey?.publicAddress.toString()}".trimIndent()
            val privateKey = "${torusKey?.privateKey?.toString(16)}}".trimIndent()

            Log.e(TAG, "Public Address: $publicAddress")
            Log.e(TAG, "Private Key: $privateKey")

            CacheUtil.savaString(CacheUtil.UNIPASS_PUBK, publicAddress)
            CacheUtil.savaString(CacheUtil.UNIPASS_PRIK, privateKey)

        }.start()
    }
}