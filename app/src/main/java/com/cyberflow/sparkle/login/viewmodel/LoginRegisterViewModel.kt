package com.cyberflow.sparkle.login.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.scopeNetLife
import androidx.lifecycle.viewModelScope
import com.cyberflow.base.model.LoginResponseData
import com.cyberflow.base.model.RegisterRequestBean
import com.cyberflow.base.net.Api
import com.cyberflow.base.net.GsonConverter
import com.cyberflow.base.viewmodel.BaseViewModel
import com.drake.net.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider
import com.orhanobut.logger.Logger
import org.json.JSONObject

class LoginRegisterViewModel : BaseViewModel() {

    var userInfo: MutableLiveData<LoginResponseData> = MutableLiveData()

    fun login(authMsg: String, type: String) = scopeNetLife {
        userInfo.value = Post<LoginResponseData>(Api.SIGN_IN) {
            //登录类型 MetaMask、WalletConnect、Coinbase、Twitter、Discord
            //钱包地址 | Twitter授权code | Discord授权token
            json("auth_msg" to authMsg, "type" to type)
        }.await()
    }

    var registerBean : RegisterRequestBean? = null

    fun register() = scopeNetLife {
        userInfo.value = Post<LoginResponseData>(Api.COMPLETE_INFO) {
            json(GsonConverter.gson.toJson(registerBean))
        }.await()
    }


    fun loginTwitter(act: Activity) {
        val firebaseAuth = FirebaseAuth.getInstance()

        val provider = OAuthProvider.newBuilder("twitter.com")
        // 参数包括 client_id、response_type、redirect_uri、state、scope 和 response_mode

        firebaseAuth
            .startActivityForSignInWithProvider(act, provider.build())
            .addOnSuccessListener {
                // User is signed in.
                // IdP data available in
                // authResult.getAdditionalUserInfo().getProfile().
                // The OAuth access token can also be retrieved:
                // ((OAuthCredential)authResult.getCredential()).getAccessToken().
                // The OAuth secret can be retrieved by calling:
                // ((OAuthCredential)authResult.getCredential()).getSecret().

//                loge( "loginTwitter: success ${GsonConverter.gson.toJson(it)}")

                Logger.e(GsonConverter.gson.toJson(it))

                Log.e(TAG, "additionalUserInfo?.providerId= ${it.additionalUserInfo?.providerId}")
                Log.e(TAG, "additionalUserInfo?.username= ${it.additionalUserInfo?.username}")

                Log.e(TAG, "it.credential?.accessToken= ${ (it.credential as? OAuthCredential)?.accessToken }")
                Log.e(TAG, "it.credential?.secret= ${ (it.credential as? OAuthCredential)?.secret  }")
                Log.e(TAG, "it.credential?.idToken= ${ (it.credential as? OAuthCredential)?.idToken  }")

                Log.e(TAG, "it.credential?.provider= ${ (it.credential as? OAuthCredential)?.provider  }")
                Log.e(TAG, "it.credential?.signInMethod= ${ (it.credential as? OAuthCredential)?.signInMethod  }")

                viewModelScope.run {
                    (it.credential as? OAuthCredential)?.apply {
                        val auth_msg = JSONObject(mapOf("access_token" to accessToken, "access_token_secret" to secret)).toString()
                        login(auth_msg, "Twitter")
                    }
                }
            }
            .addOnFailureListener {
                // Handle failure.
                it.printStackTrace()
                Log.e(TAG, "loginTwitter: fail")
            }
    }

    fun loginGoogle(act: Activity) {

    }


    var previous: MutableLiveData<Int> = MutableLiveData()
    var next: MutableLiveData<Int> = MutableLiveData()

    fun clickPrevious() {
        previous.value = next.value?.minus(1)
    }

    fun clickNext() {
        next.value = next.value?.plus(1)
    }
}