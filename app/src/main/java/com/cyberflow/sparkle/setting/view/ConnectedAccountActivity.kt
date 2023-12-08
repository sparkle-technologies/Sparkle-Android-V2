package com.cyberflow.sparkle.setting.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.model.BindBean
import com.cyberflow.base.model.BindingResult
import com.cyberflow.base.net.Api
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.util.bus.SparkleEvent
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.MyApp
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ActivityConnectAccountBinding
import com.cyberflow.sparkle.setting.widget.ConnectLoadingDialog
import com.cyberflow.sparkle.setting.widget.DisconnectDialog
import com.cyberflow.sparkle.setting.widget.DisconnectHintDialog
import com.cyberflow.sparkle.widget.NotificationDialog
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.cyberflow.sparkle.widget.ToastDialogHolder
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.net.utils.withMain
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider
import dev.pinkroom.walletconnectkit.core.accounts
import dev.pinkroom.walletconnectkit.sign.dapp.sample.main.Content
import kotlinx.coroutines.launch
import org.json.JSONObject


class ConnectedAccountActivity : BaseDBAct<BaseViewModel, ActivityConnectAccountBinding>() {

    companion object {
        fun go(context: Context) {
            val intent = Intent(context, ConnectedAccountActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        initRes()
        mDataBinding.llBack.setOnClickListener {
            onBackPressed()
        }

        mDataBinding.btnConnectX.setClickListener(object : ShadowTxtButton.ShadowClickListener{
            override fun clicked(disable: Boolean) {
                 connectX()
            }
        })

        mDataBinding.btnConnectDiscord.setClickListener(object : ShadowTxtButton.ShadowClickListener{
            override fun clicked(disable: Boolean) {
                connectDiscord()
            }
        })

        mDataBinding.btnConnectWallet.setClickListener(object : ShadowTxtButton.ShadowClickListener{
            override fun clicked(disable: Boolean) {
                connectWallet()
            }
        })

        mDataBinding.btnDisconnectX.setClickListener(object : ShadowImgButton.ShadowClickListener{
            override fun clicked() {
                 disconnectX()
            }
        })

        mDataBinding.btnDisconnectDiscord.setClickListener(object : ShadowImgButton.ShadowClickListener{
            override fun clicked() {
                disconnectDiscord()
            }
        })

        mDataBinding.btnDisconnectWallet.setClickListener(object : ShadowImgButton.ShadowClickListener{
            override fun clicked() {
                disconnectWallet()
            }
        })
    }

    var hintDialog: DisconnectHintDialog? = null
    private fun showHintDialog(){
        if(hintDialog == null){
            hintDialog = DisconnectHintDialog(this, object : DisconnectHintDialog.Callback{
                override fun onSelected(select: Boolean) {
                    hintDialog?.dismiss()
                }
            })
        }
        hintDialog?.show()
    }

    private var loadingDialog: ConnectLoadingDialog? = null

    private var titleConnect = ""
    private var txtConnect = ""
    private var titleDisConnect = ""
    private var txtDisConnect = ""

    private fun initRes(){
        titleConnect = getString(R.string.connecting)
        txtConnect = getString(R.string.the_account_is_connecting_please_wait_a_moment)
        titleDisConnect = getString(R.string.disconnecting)
        txtDisConnect = getString(R.string.the_account_is_disconnecting_please_wait_a_moment)
    }

    private fun showLoadingDialog(conn: Boolean) {
        val title = if(conn) titleConnect else titleDisConnect
        val txt = if(conn) txtConnect else txtDisConnect
        if (loadingDialog == null){
            loadingDialog = ConnectLoadingDialog(this, title, txt, object : ConnectLoadingDialog.Callback{
                override fun onSelected(select: Boolean) {
//                connectingDialog?.onDestroy()
                }
            })
        }else{
            loadingDialog?.updateUi(title, txt)
        }
        loadingDialog?.show()
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
    }

    var disconnectDialog: DisconnectDialog? = null

    private fun showDisconnectDialog(data: BindBean?){
        if(disconnectDialog == null){
            disconnectDialog = DisconnectDialog(this, data ,object : DisconnectDialog.Callback{
                override fun onSelected(select: Boolean) {
                    disconnectDialog?.dismiss()
                }
            })
        }else{
            disconnectDialog?.updateData(data)
        }
        disconnectDialog?.show()
    }

    private fun disconnectWallet() {
        if(mDataBinding.btnConnectX.isVisible && mDataBinding.btnConnectDiscord.isVisible){
            showHintDialog()
        }else{
            showDisconnectDialog(metamask)
        }
    }

    private fun disconnectDiscord() {
        if(mDataBinding.btnConnectX.isVisible && mDataBinding.btnConnectWallet.isVisible){
            showHintDialog()
        }else{
            showDisconnectDialog(discord)
        }
    }

    private fun disconnectX() {
        if(mDataBinding.btnConnectDiscord.isVisible && mDataBinding.btnConnectWallet.isVisible){
            showHintDialog()
        }else{
            showDisconnectDialog(x)
        }
    }

    private fun connectWallet() {
        Log.e(TAG, "connectWallet: ", )
        mDataBinding.composeView.isVisible = true
        mDataBinding.composeView.performClick()
//        showLoadingDialog(true)
    }

    private fun connectDiscord() {
        Log.e(TAG, "connectDiscord: ", )
//        showLoadingDialog(true)
        ToastDialogHolder.getDialog()?.show(this, NotificationDialog.TYPE_WARN, "Coming soon...")
    }

    private fun connectX() {
        Log.e(TAG, "connectX: ", )
        loginTwitter()
        showLoadingDialog(true)
    }

    private var x: BindBean? = null
    private var discord: BindBean? = null
    private var metamask: BindBean? = null

    override fun initData() {
        showUserInfo()
    }

    private fun showUserInfo(){
        val user  = CacheUtil.getUserInfo()?.user
        user?.bind_list?.forEach {

            Log.e(TAG, "bind_list: type=${it.type}    nick=${it.nick}" )


            if(it.type == "Twitter"){
                x = it
                mDataBinding.tvX.text = "@${it.nick}"
                mDataBinding.tvX.isVisible = true
                mDataBinding.btnDisconnectX.isVisible = true
            }

            if(it.type == "Discord"){
                discord = it
                mDataBinding.tvDiscord.text = "@${it.nick}"
                mDataBinding.tvDiscord.isVisible = true
                mDataBinding.btnDisconnectDiscord.isVisible = true
            }

            if(it.type == "MetaMask"){
                metamask = it
                mDataBinding.tvWallet.text = it.nick
                mDataBinding.tvWallet.isVisible = true
                mDataBinding.btnDisconnectWallet.isVisible = true
            }
        }

        mDataBinding.btnConnectX.isVisible = false
        mDataBinding.btnConnectDiscord.isVisible = false
        mDataBinding.btnConnectWallet.isVisible = false

        if(mDataBinding.btnDisconnectX.isGone){
            mDataBinding.btnConnectX.isVisible = true
        }
        if(mDataBinding.btnDisconnectDiscord.isGone){
            mDataBinding.btnConnectDiscord.isVisible = true
        }
        if(mDataBinding.btnDisconnectWallet.isGone){
            mDataBinding.btnConnectWallet.isVisible = true
        }
    }

    private fun request(authMsg: String, authType: String) {
        scopeNetLife {
            try{
                val data = Post<BindingResult>(Api.LOGIN_BIND) {
                    json("auth_type" to authType, "auth_msg" to authMsg)
                }.await()
                CacheUtil.getUserInfo()?.also {
                    it.user?.apply {
                        data?.also { new ->
                            bind_list = new.bind_list
                            CacheUtil.setUserInfo(it)
                            LiveDataBus.get().with(SparkleEvent.PROFILE_CHANGED).postValue("time:${System.currentTimeMillis()}")
                            showUserInfo()
                        }
                    }
                }
            }catch (e: Exception){

            }finally {
                withMain {
                    hideLoadingDialog()
                }
            }
        }
    }


    private var isFirst = true
    override fun onResume() {
        super.onResume()
        if(isFirst){
            isFirst = false
            initWalletConnect()
        }
    }

    /********************* twitter ******************************/
    fun loginTwitter() {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()
        val provider = OAuthProvider.newBuilder("twitter.com")
        firebaseAuth
            .startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener {
                (it.credential as? OAuthCredential)?.apply {
                    val auth_msg = JSONObject(mapOf("access_token" to accessToken, "access_token_secret" to secret)).toString()
                    request(auth_msg, "Twitter")
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                ToastDialogHolder.getDialog()?.show(this, NotificationDialog.TYPE_ERROR, "Twitter login failed, please try again")
                hideLoadingDialog()
            }
    }


    /********************* wallet connect ******************************/

    private fun initWalletConnect() {
        lifecycleScope.launch {
            MyApp.instance.checkWalletConnect()
            MyApp.instance.walletConnectKit?.disconnect()
            runOnUiThread {
                mDataBinding.composeView.setContent {
                    MyApp.instance.walletConnectKit?.let { Content(it) }
                }
                lifecycleScope.launch {
                    MyApp.instance.walletConnectKit?.activeSessions?.collect {
                        if (it.isNotEmpty()) {
                            it.forEach { session ->
                                session.accounts.forEach { ac ->
                                    Log.e(TAG, "accounts.forEach: $ac")
                                }
                            }
                            val activated = MyApp.instance.walletConnectKit?.activeAccount
                            activated?.also { ac ->
                                var wallet = "MetaMask"
                                request(ac.address, wallet)
                            }
                        }
                    }
                }
            }
        }
    }
}