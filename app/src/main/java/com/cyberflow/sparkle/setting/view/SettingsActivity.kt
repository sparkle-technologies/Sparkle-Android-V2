package com.cyberflow.sparkle.setting.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.ToastUtil
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.MyApp
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
import com.cyberflow.sparkle.databinding.ActivitySettingBinding
import com.cyberflow.sparkle.flutter.FlutterProxyActivity
import com.cyberflow.sparkle.flutter.FlutterProxyActivity.Companion.handleFlutterCommonEvent
import com.cyberflow.sparkle.flutter.FlutterProxyActivity.Companion.initParams
import com.cyberflow.sparkle.flutter.FlutterProxyActivity.Companion.prepareFlutterEngine
import com.cyberflow.sparkle.im.DBManager
import com.cyberflow.sparkle.login.view.LoginAct
import com.cyberflow.sparkle.widget.NotificationDialog
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.cyberflow.sparkle.widget.ToastDialogHolder
import com.drake.net.utils.withMain
import com.google.firebase.auth.FirebaseAuth
import com.hjq.language.MultiLanguages
import com.hjq.language.OnLanguageListener
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsActivity : BaseDBAct<BaseViewModel, ActivitySettingBinding>() {

    companion object {
        fun go(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.llBack.setOnClickListener {
            onBackPressed()
        }
        CacheUtil.apply {
            getUserInfo()?.let {
                it.user?.let {

                }
            }
            getString(LOGIN_METHOD)?.let {
                loginMethod = it
            }
            getString(UNIPASS_PUBK)?.let {
                publicKey = it
            }
            getString(UNIPASS_PRIK)?.let {
                privateKey = it
            }
        }
        mDataBinding.layEditProfile.setOnClickListener {
            goEditProfile()
        }
        mDataBinding.layAccountPrivacy.setOnClickListener {
            goAccountPrivacy()
        }
        mDataBinding.layConnectedAccounts.setOnClickListener {
            goConnetedAccount()
        }
        mDataBinding.layLanguage.setOnClickListener {
            goLanguage()
        }
        mDataBinding.btnLogout.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked(d: Boolean) {
                logout()
            }
        })
    }

    private fun goLanguage() {
        LanguageActivity.go(this)
    }

    // 0. clear cache
    // 1. walletConnect
    // 2. twitter
    // 3. web3Auth
    // 4. unipass
    private fun logout() {
        if (loginMethod == "Twitter") {
            twitter()
        } else {
            walletConnect()
        }

        CacheUtil.setUserInfo(null)
        CacheUtil.savaString(CacheUtil.LOGIN_METHOD, "")
        CacheUtil.savaString(CacheUtil.UNIPASS_PUBK, "")
        CacheUtil.savaString(CacheUtil.UNIPASS_PRIK, "")

        lifecycleScope.launch {
            DBManager.instance.db?.imUserInfoDao()?.deleteAll()
            IMDataManager.instance.clearCache()

            withMain {
                LoginAct.go(this@SettingsActivity)
                finish()
            }
        }
    }


    var loginMethod = ""
    var publicKey = ""
    var privateKey = ""

    private fun walletConnect() {
        MyApp.instance.checkWalletConnect()
        MyApp.instance.walletConnectKit?.disconnect {
            it.printStackTrace()
        }
    }

    private fun twitter() {
        FirebaseAuth.getInstance().signOut()
    }

    override fun initData() {
        initFlutter()
//        initMultiLanguage()
    }

    private fun initMultiLanguage() {
        MultiLanguages.setOnLanguageListener(object : OnLanguageListener {
            override fun onAppLocaleChange(oldLocale: Locale?, newLocale: Locale?) {
                Log.e(TAG, "onAppLocaleChange: old=$oldLocale  \t new=$newLocale")
//                MultiLanguages.updateAppLanguage(this@SettingsActivity)
            }

            override fun onSystemLocaleChange(oldLocale: Locale?, newLocale: Locale?) {

            }
        })
    }

    private var editMethodChannel : MethodChannel? = null
    private var privacyMethodChannel : MethodChannel? = null

    private fun initFlutter() {
        editMethodChannel = prepareFlutterEngine(this, FlutterProxyActivity.ENGINE_ID_EDIT_PROFILE, FlutterProxyActivity.ROUTE_EDIT_PROFILE, FlutterProxyActivity.CHANNEL_SETTING, FlutterProxyActivity.SCENE_SETTING_EDIT){
            scene, method, call, result ->
            handleFlutterEvent( method, call, result)
        }

        privacyMethodChannel = prepareFlutterEngine(this, FlutterProxyActivity.ENGINE_ID_ACCOUNT_PRIVACY, FlutterProxyActivity.ROUTE_ACCOUNT_PRIVACY, FlutterProxyActivity.CHANNEL_SETTING, FlutterProxyActivity.SCENE_SETTING_PRIVACY){
                engineName, method, call, result ->
            handleFlutterEvent( method, call, result)
        }
    }

    private fun handleFlutterEvent(
        method: MethodChannel,
        call: MethodCall,
        result: MethodChannel.Result
    ) {
        if (call.method == "flutterToast") {
            val type = call.argument<String>("type")
            val content = call.argument<String>("content")
            if (content?.isNotEmpty() == true) {
                val t = when (type) {
                    "success" -> NotificationDialog.TYPE_SUCCESS
                    "error" -> NotificationDialog.TYPE_ERROR
                    else -> NotificationDialog.TYPE_WARN
                }
                ToastDialogHolder.getDialog()?.show(this@SettingsActivity, t, content)
            }
            result.success("success")
        }else{
            handleFlutterCommonEvent(0, method, call, result)
        }
    }

    private var isFirstTimeForEdit = true

    private fun goEditProfile() {
        if(isFirstTimeForEdit){
            isFirstTimeForEdit = false
        }else{
            editMethodChannel?.let { initParams(FlutterProxyActivity.SCENE_SETTING_EDIT, it) }
        }
        FlutterProxyActivity.go(this, FlutterProxyActivity.ENGINE_ID_EDIT_PROFILE)
    }

    private var isFirstTimeForPrivacy = true

    private fun goAccountPrivacy() {
        if(isFirstTimeForPrivacy){
            isFirstTimeForPrivacy = false
        }else{
            privacyMethodChannel?.let { initParams(FlutterProxyActivity.SCENE_SETTING_PRIVACY, it) }
        }
        FlutterProxyActivity.go(this, FlutterProxyActivity.ENGINE_ID_ACCOUNT_PRIVACY)
    }

    private fun goConnetedAccount() {
        ToastUtil.show(this, "coming soon ")
    }
}
