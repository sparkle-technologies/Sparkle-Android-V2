package com.cyberflow.sparkle.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.scopeLife
import com.cyberflow.base.net.Api
import com.cyberflow.base.viewmodel.BaseViewModel
import com.drake.net.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import org.json.JSONObject

class LoginVM : BaseViewModel() {

    var userInfo: MutableLiveData<UserLoginBean> = MutableLiveData()

    fun login(address: String, wallet: String) = scopeLife {
        userInfo.value = Post<UserLoginBean>(Api.SIGN_IN) {
            json("auth_msg" to address, "type" to wallet)
        }.await()
    }

    fun loginTwitter(act: LoginAct) {
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
                Log.e(TAG, "loginTwitter: success ${JSONObject.wrap(it).toString()}")
                Log.e(TAG, " ${it.additionalUserInfo?.providerId}")
                Log.e(TAG, " ${it.additionalUserInfo?.username}")
                Log.e(TAG, " ${it.user?.email}")
                Log.e(TAG, " ${it.credential?.signInMethod}")

            }
            .addOnFailureListener {
                // Handle failure.
                it.printStackTrace()
                Log.e(TAG, "loginTwitter: fail")
            }
    }

    fun loginGoogle(act: LoginAct) {
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
}