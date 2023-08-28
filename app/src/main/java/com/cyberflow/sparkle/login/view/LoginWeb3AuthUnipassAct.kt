package com.cyberflow.sparkle.login.view

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import com.auth0.android.jwt.JWT
import com.cyberflow.base.act.BaseVBAct
import com.cyberflow.sparkle.databinding.ActivityLoginBinding
import com.cyberflow.sparkle.databinding.ActivityLoginWebauthUnipassTestBinding
import com.cyberflow.sparkle.login.viewmodel.LoginViewModel
import com.web3auth.singlefactorauth.SingleFactorAuth
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.SingleFactorAuthArgs
import com.web3auth.singlefactorauth.types.TorusKey
import org.torusresearch.fetchnodedetails.types.TorusNetwork
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException


class LoginWeb3AuthUnipassAct : BaseVBAct<LoginViewModel, ActivityLoginWebauthUnipassTestBinding>() {

    val testAccount = arrayListOf(
        "0x150E4AB89Ddd5fa7f8Fb8cae501b48961Ce703A4",
        "0x0c42ad43BEEDaCe4927E1065c10C776f2C604b5C",
        "0x73cf3CB3dc0D6872878a316509aFb7510E7cd44d",
        "0xE1c026085863e37321DbF7871c6d28a79153c888",
        "0xDc58a843c8096943Ca2899b31Db004eB8B417C13",  //account5  new one
    )

    override fun initView(savedInstanceState: Bundle?) {
        viewModel.userInfo.observe(this) {
            Log.e(TAG, "initView: $it")
            mViewBind.tvMsg.text = "$it"
            it.id_token.also { id_token ->
                Log.e(TAG, "got  id_token from login :  $id_token")
                signInJWT(id_token)
            }
        }

        mViewBind.btnLogin.setOnClickListener {
            viewModel.login(testAccount[2], "MetaMask")
        }

        mViewBind.btnGoogleLogin.setOnClickListener {
            viewModel.loginGoogle(this)
        }

        mViewBind.btnTwitterLogin.setOnClickListener {
            viewModel.loginTwitter(this)
        }
    }

    override fun initData() {
        initJWT()
    }


    /*************************************************************************************/

    private lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var singleFactorAuthArgs: SingleFactorAuthArgs
    private lateinit var loginParams: LoginParams
    private var torusKey: TorusKey? = null
    private var publicAddress: String = ""

    private fun initJWT() {
        singleFactorAuthArgs = SingleFactorAuthArgs(TorusNetwork.TESTNET)
        singleFactorAuth = SingleFactorAuth(singleFactorAuthArgs)

        val sessionResponse: CompletableFuture<TorusKey> =
            singleFactorAuth.initialize(this.applicationContext)
        sessionResponse.whenComplete { torusKey, error ->
            if (torusKey != null) {
                publicAddress = torusKey?.publicAddress.toString()
                Log.e(TAG, "Private Key: ${torusKey.privateKey?.toString(16)}".trimIndent())
            } else {
                Log.e(TAG, error.message ?: "Something went wrong")
            }
        }
    }

    private fun signInJWT(idToken: String) {
        val jwt = JWT(idToken)
        val issuer = jwt.issuer //get registered claims
        Log.d(ContentValues.TAG, "Issuer = $issuer")
        val open_id = jwt.getClaim("open_id").asString() //get sub claims
        Log.d(ContentValues.TAG, "open_id = $open_id")

        loginParams = LoginParams("Sparkle-Custom-Auth-01", "$open_id", "$idToken")
        try {
            torusKey = singleFactorAuth.getKey(loginParams, this.applicationContext, 86400).get()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        publicAddress = torusKey?.publicAddress.toString()
        Log.e(TAG, "Private Key: ${torusKey?.privateKey?.toString(16)}".trimIndent())
        Log.e(TAG, "Public Address: $publicAddress".trimIndent())
    }
}