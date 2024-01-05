package com.cyberflow.sparkle.login.view

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.auth0.android.jwt.JWT
import com.cyberflow.base.act.BaseVBAct
import com.cyberflow.base.model.LoginResponseData
import com.cyberflow.base.net.Api
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.PageConst
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.util.callback.IMLoginResponse
import com.cyberflow.base.util.callback.IMV2Callback
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.MyApp
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.chat.IMManager
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
import com.cyberflow.sparkle.databinding.ActivityLoginBinding
import com.cyberflow.sparkle.im.DBManager
import com.cyberflow.sparkle.login.viewmodel.LoginRegisterViewModel
import com.cyberflow.sparkle.mainv2.view.MainActivityV2
import com.cyberflow.sparkle.register.view.RegisterAct
import com.cyberflow.sparkle.widget.NotificationDialog
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.cyberflow.sparkle.widget.ToastDialog
import com.drake.net.Post
import com.drake.net.utils.scopeDialog
import com.drake.net.utils.withMain
import com.drake.spannable.movement.ClickableMovementMethod
import com.drake.spannable.replaceSpan
import com.drake.spannable.span.ColorSpan
import com.github.penfeizhou.animation.apng.APNGDrawable
import com.github.penfeizhou.animation.loader.AssetStreamLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider
import com.hyphenate.easeui.ui.dialog.LoadingDialogHolder
import com.hyphenate.easeui.ui.dialog.ThreadUtil
import com.therouter.router.Route
import com.web3auth.singlefactorauth.SingleFactorAuth
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.SingleFactorAuthArgs
import com.web3auth.singlefactorauth.types.TorusKey
import dev.pinkroom.walletconnectkit.core.accounts
import dev.pinkroom.walletconnectkit.core.sessions
import dev.pinkroom.walletconnectkit.sign.dapp.sample.main.Content
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.torusresearch.fetchnodedetails.types.TorusNetwork
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

@Route(path = PageConst.App.PAGE_LOGIN)
class LoginAct : BaseVBAct<LoginRegisterViewModel, ActivityLoginBinding>() {

    companion object {
        fun go(context: Context) {
            val intent = Intent(context, LoginAct::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }

        // after login, now login to IM , then go main page or register page
        fun imLogin(activity: Activity){
            LoadingDialogHolder.getLoadingDialog()?.show(activity)
            IMManager.instance.loginToIM(object : IMV2Callback<IMLoginResponse> {
                override fun onEvent(event: IMLoginResponse) {
                    ThreadUtil.runOnMainThread{
                        LoadingDialogHolder.getLoadingDialog()?.hide()
                        if(event.success){
                            MainActivityV2.go(activity)
                            activity.finish()
                        }else{
                            val msg = event.message ?: "IM login failed"
                            LiveDataBus.get().with(NotificationDialog.EVENT_ERROR).postValue(msg)
                        }
                    }
                }
            })
        }
    }


    // 1.
    // 2. twitter
    // 3. web3Auth
    // 4. unipass
    //  walletConnect  im twitter database
    private fun logout(){
        FirebaseAuth.getInstance().signOut()
        lifecycleScope.launch {
            IMManager.instance.keepLogout()   //  make sure no login in IM
            DBManager.instance.db?.imFriendRequestDao()?.deleteAll()
            DBManager.instance.db?.imFriendInfoDao()?.deleteAll()
            DBManager.instance.db?.imConversationCacheDao()?.deleteAll()
            DBManager.instance.db?.horoscopeCacheDao()?.deleteAll()
            DBManager.instance.db?.compatibilityCacheDao()?.deleteAll()
            IMDataManager.instance.clearCache()
            ToastDialog.count = 1
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        initAnim()
        setSpan(mViewBind.tvCondition)

        if (CacheUtil.getUserInfo()?.user?.open_uid?.isNotEmpty() == true) {
            imLogin(this)
            return
        }else{
            logout()
        }


        mViewBind.btnGoogleLogin.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                toastSuccess(getString(R.string.coming_soon))
            }
        })

        mViewBind.btnIgLogin.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                toastSuccess(getString(R.string.coming_soon))
            }
        })

        mViewBind.btnTwitterLogin.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                if(agreed()){
                    loginTwitter()
                }

            }
        })

        mViewBind.cb.setOnCheckedChangeListener { compoundButton, b ->
            mViewBind.layCompose.isClickable = b
        }

        mViewBind.layCompose.setMoveListener {
            toastError(getString(R.string.login_check_agree))
        }
    }

    private fun agreed() : Boolean{
        if(!mViewBind.cb.isChecked){
            toastError(getString(R.string.login_check_agree))
        }
        return mViewBind.cb.isChecked
    }

    override fun initData() {
        mViewBind.composeView.postDelayed({
            initWalletConnect()
        }, 1000)
//        initWeb3Auth()
    }

    private fun request(authMsg: String, type: String){
         scopeDialog {
             val data = Post<LoginResponseData>(Api.SIGN_IN) {
                  //登录类型 MetaMask、WalletConnect、Coinbase、Twitter、Discord
                  //钱包地址 | Twitter授权code | Discord授权token
                  json("auth_msg" to authMsg, "type" to type)
              }.await()

             data?.let {
                 CacheUtil.setUserInfo(it)
//                 signInJWT(it.id_token)
                 if (it.user?.open_uid.isNullOrEmpty() || it.im_token.isNullOrEmpty()) {
                     RegisterAct.go(this@LoginAct)
                 } else {
                     imLogin(this@LoginAct)
                 }
             }
          }
    }

    private fun setSpan(tv: TextView) {
        val s = getString(R.string.login_conditiion_hint)
        val h1 = getString(R.string.terms_of_use)
        val h2 = getString(R.string.privacy_policy)
        tv.movementMethod = ClickableMovementMethod.getInstance()
        tv.text = s.replaceSpan(h1) {
            arrayOf(URLSpan("https://www.sparkle.fun/terms/terms-of-use.html"), ColorSpan("#8B82DB"), StyleSpan(Typeface.BOLD))
        }.replaceSpan(h2){
            arrayOf(URLSpan("https://www.sparkle.fun/terms/privacy-policy.html"), ColorSpan("#8B82DB"), StyleSpan(Typeface.BOLD))
        }
    }

    /********************* twitter ******************************/
    fun loginTwitter() {
        val firebaseAuth = FirebaseAuth.getInstance()

        val provider = OAuthProvider.newBuilder("twitter.com")
        // 参数包括 client_id、response_type、redirect_uri、state、scope 和 response_mode

        firebaseAuth
            .startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener {
                // User is signed in.
                // IdP data available in
                // authResult.getAdditionalUserInfo().getProfile().
                // The OAuth access token can also be retrieved:
                // ((OAuthCredential)authResult.getCredential()).getAccessToken().
                // The OAuth secret can be retrieved by calling:
                // ((OAuthCredential)authResult.getCredential()).getSecret().

//                Logger.e(GsonConverter.gson.toJson(it))

                Log.e(BaseViewModel.TAG, "additionalUserInfo?.providerId= ${it.additionalUserInfo?.providerId}")
                Log.e(BaseViewModel.TAG, "additionalUserInfo?.username= ${it.additionalUserInfo?.username}")

                Log.e(BaseViewModel.TAG, "it.credential?.accessToken= ${ (it.credential as? OAuthCredential)?.accessToken }")
                Log.e(BaseViewModel.TAG, "it.credential?.secret= ${ (it.credential as? OAuthCredential)?.secret  }")
                Log.e(BaseViewModel.TAG, "it.credential?.idToken= ${ (it.credential as? OAuthCredential)?.idToken  }")

                Log.e(BaseViewModel.TAG, "it.credential?.provider= ${ (it.credential as? OAuthCredential)?.provider  }")
                Log.e(BaseViewModel.TAG, "it.credential?.signInMethod= ${ (it.credential as? OAuthCredential)?.signInMethod  }")

                (it.credential as? OAuthCredential)?.apply {
                    val auth_msg = JSONObject(mapOf("access_token" to accessToken, "access_token_secret" to secret)).toString()
                    CacheUtil.savaString(CacheUtil.LOGIN_METHOD, "Twitter")
                    request(auth_msg, "Twitter")
                }
            }
            .addOnFailureListener {
                // Handle failure.
                it.printStackTrace()
                toastError(getString(R.string.twitter_login_failed_please_try_again))
            }
    }


    /********************* wallet connect ******************************/

    private fun initWalletConnect() {
        Log.e(MyApp.TAG, "initWalletConnect: ")
        lifecycleScope.launch {
            MyApp.instance.checkWalletConnect()
            MyApp.instance.walletConnectKit?.disconnect()
            runOnUiThread {
                mViewBind.composeView.setContent {
                    MyApp.instance.walletConnectKit?.let { Content(it) }
                }
                lifecycleScope.launch {
                    MyApp.instance.walletConnectKit?.activeSessions?.collect {
                        if (it.isNotEmpty()) {
                            it.forEach { session ->
                                // Session(
                                //      pairingTopic=ab511d8e56f0a02dbd90e0ea17c777a5d30ac2f7c059133c4d7bb04144062c01,
                                //      topic=7524cc32f3a334290c29d256423d2a9ddb795a31c4e7b4a22deeef6e71fb8e84, expiry=1692258984,
                                //      namespaces={
                                //                  eip155=Session(
                                //                          chains=[eip155:1, eip155:5],
                                //                          accounts=[eip155:1:0x150E4AB89Ddd5fa7f8Fb8cae501b48961Ce703A4, eip155:5:0x150E4AB89Ddd5fa7f8Fb8cae501b48961Ce703A4],
                                //                          methods=[personal_sign, eth_signTypedData, eth_signTypedData_v3, eth_signTypedData_v4, eth_sendTransaction],
                                //                          events=[accountsChanged, chainChanged, disconnect, session_delete, display_uri, connect]
                                //                   )},
                                //     metaData=AppMetaData(name=MetaMask Wallet, description=MetaMask Wallet Integration, url=https://metamask.io/, icons=[], redirect=null, verifyUrl=null))
                                Log.e(TAG, "session foreach: $session ")
                                // Account(
                                //  topic=7524cc32f3a334290c29d256423d2a9ddb795a31c4e7b4a22deeef6e71fb8e84,
                                //  address=0x150E4AB89Ddd5fa7f8Fb8cae501b48961Ce703A4,
                                //  chainNamespace=eip155, chainReference=1, name=MetaMask Wallet, icon=null)
                                session.accounts.forEach { ac ->
                                    Log.e(TAG, "accounts.forEach: $ac")
                                }
                            }

                            val address = it.flatMap { it.accounts }.map { it.address to it }
                            address.forEach {
                                Log.e(TAG, "address foreach:  ${it.first}   ${it.second} ")
                            }

                            val activated = MyApp.instance.walletConnectKit?.activeAccount

                            Log.e(TAG, "initWalletConnect: $activated " )

                            activated?.also { ac ->
                                val name = sessions.first().metaData?.name.orEmpty()
                                CacheUtil.savaString(CacheUtil.LOGIN_METHOD, name)
                                var wallet = "MetaMask"
                                request(ac.address, wallet)
                            }
                        }
                    }
                }
            }
        }
    }

    /********************* anim ******************************/


    private fun initAnim() {
        lifecycleScope.launch {
            val asset = AssetStreamLoader(this@LoginAct, "login.png")
            APNGDrawable(asset).apply {
                setLoopLimit(-1)
                withMain {
                    mViewBind.ivAnima.setImageDrawable(this@apply)
                    start()
                }
            }
        }
        /*lifecycleScope.launch {
            execLottieAnim()
        }*/
    }

  /*  private suspend fun execLottieAnim() {
        suspendCoroutine {
            val lottieView = mViewBind.lavHomepage
            lottieView.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    Log.d(TAG, "lottieView is start")
                }

                override fun onAnimationEnd(animation: Animator) {
                    Log.d(TAG, "lottieView is end")
                    it.resume(Unit)
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
        }
    }*/


    /************************************** 这个以后再弄  得搞个新接口刷新JWT ***********************************************/

    private lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var singleFactorAuthArgs: SingleFactorAuthArgs
    private lateinit var loginParams: LoginParams
    private var torusKey: TorusKey? = null
    private var publicAddress: String = ""

    private fun initWeb3Auth() {
        Log.e(MyApp.TAG, "initWeb3Auth: ")
        lifecycleScope.launch {
            singleFactorAuthArgs = SingleFactorAuthArgs(TorusNetwork.TESTNET)
            singleFactorAuth = SingleFactorAuth(singleFactorAuthArgs)

            val sessionResponse: CompletableFuture<TorusKey> = singleFactorAuth.initialize(MyApp.instance)
            sessionResponse.whenComplete { torusKey, error ->
                if (torusKey != null) {
                    publicAddress = torusKey?.publicAddress.toString()
                    Log.e(TAG, "Private Key: ${torusKey.privateKey?.toString(16)}".trimIndent())
                } else {
                    Log.e(TAG, error.message ?: "Something went wrong")
                }
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
                torusKey = singleFactorAuth.getKey(loginParams, MyApp.instance, 86400).get()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            publicAddress = "${torusKey?.publicAddress.toString()}".trimIndent()
            val privateKey = "${torusKey?.privateKey?.toString(16)}}".trimIndent()

            Log.e(MyApp.TAG, "Public Address: $publicAddress")
            Log.e(MyApp.TAG, "Private Key: $privateKey")

            CacheUtil.savaString(CacheUtil.UNIPASS_PUBK, publicAddress)
            CacheUtil.savaString(CacheUtil.UNIPASS_PRIK, privateKey)

        }.start()
    }
}