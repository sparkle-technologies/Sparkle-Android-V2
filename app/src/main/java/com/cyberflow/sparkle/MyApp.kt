package com.cyberflow.sparkle

import android.content.ContentValues
import android.util.Log
import com.auth0.android.jwt.JWT
import com.cyberflow.base.BaseApp
import com.cyberflow.base.act.BaseVMAct
import com.cyberflow.base.net.initNetSpark
import com.cyberflow.base.util.CacheUtil
import com.drake.brv.utils.BRV
import com.drake.net.utils.scope
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
import org.torusresearch.fetchnodedetails.types.TorusNetwork
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class MyApp : BaseApp() {

    private val TAG = "MyApp"

    companion object {
        lateinit var instance: MyApp
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        BRV.modelId = BR.m
        initNetSpark()
        CacheUtil.init(this)
        initGooglePlace()
        initWeb3Auth()
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

    /************************************** 这个以后再弄  得搞个新接口刷新JWT ***********************************************/

    private lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var singleFactorAuthArgs: SingleFactorAuthArgs
    private lateinit var loginParams: LoginParams
    private var torusKey: TorusKey? = null
    private var publicAddress: String = ""

    private fun initWeb3Auth(){
        singleFactorAuthArgs = SingleFactorAuthArgs(TorusNetwork.TESTNET)
        singleFactorAuth = SingleFactorAuth(singleFactorAuthArgs)

        val sessionResponse: CompletableFuture<TorusKey> = singleFactorAuth.initialize(this.applicationContext)
        sessionResponse.whenComplete { torusKey, error ->
            if (torusKey != null) {
                publicAddress = torusKey?.publicAddress.toString()
                Log.e(BaseVMAct.TAG, "Private Key: ${torusKey.privateKey?.toString(16)}".trimIndent())
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
            publicAddress = torusKey?.publicAddress.toString()
            Log.e(TAG, "Private Key: ${torusKey?.privateKey?.toString(16)}".trimIndent())
            Log.e(TAG, "Public Address: $publicAddress".trimIndent())
        }.start()
    }
}