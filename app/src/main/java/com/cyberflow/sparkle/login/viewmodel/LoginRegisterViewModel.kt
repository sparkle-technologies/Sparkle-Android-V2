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
                Log.e(TAG, "loginTwitter: success $it")

                Log.e(TAG, "additionalUserInfo?.providerId= ${it.additionalUserInfo?.providerId}")
                Log.e(TAG, "additionalUserInfo?.username= ${it.additionalUserInfo?.username}")
                Log.e(TAG, "it.credential?.accessToken= ${ (it.credential as? OAuthCredential)?.accessToken }")
                Log.e(TAG, "it.user?.providerId= ${it.user?.providerId}")
                Log.e(TAG, "it.credential?.signInMethod= ${it.credential?.signInMethod}")

                viewModelScope.run {
//                    login((it.credential as? OAuthCredential)?.accessToken ?: "", "Twitter") //TODO  接口还没好
                }
            }
            .addOnFailureListener {
                // Handle failure.
                it.printStackTrace()
                Log.e(TAG, "loginTwitter: fail")
            }
    }

    fun loginGoogle(act: Activity) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            Log.e(TAG, " firebaseUser is null")
            return
        }
        Log.e(TAG, "loginGoogle: ${firebaseUser.toString()}")
        val provider = OAuthProvider.newBuilder("twitter.com")
        firebaseUser
            .startActivityForReauthenticateWithProvider(act, provider.build())
            .addOnSuccessListener {
                // User is re-authenticated with fresh tokens and
                // should be able to perform sensitive operations
                // like account deletion and email or password
                // update.
                Log.e(TAG, " ${it.additionalUserInfo.toString()}")
                Log.e(TAG, " ${it.user.toString()}")
                Log.e(TAG, " ${it.credential.toString()}")
            }
            .addOnFailureListener {
                // Handle failure.
            }
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