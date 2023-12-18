package com.cyberflow.sparkle.setting.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.databinding.ActivitySettingBinding
import com.cyberflow.sparkle.flutter.FlutterProxyActivity
import com.cyberflow.sparkle.flutter.FlutterProxyActivity.Companion.handleFlutterCommonEvent
import com.cyberflow.sparkle.flutter.FlutterProxyActivity.Companion.initParams
import com.cyberflow.sparkle.flutter.FlutterProxyActivity.Companion.prepareFlutterEngine
import com.cyberflow.sparkle.login.view.LoginAct
import com.cyberflow.sparkle.widget.NotificationDialog
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.cyberflow.sparkle.widget.ToastDialogHolder
import com.hjq.language.MultiLanguages
import com.hjq.language.OnLanguageListener
import io.flutter.embedding.engine.FlutterEngineCache
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

    // 0. clear cache   . just clear cache, then at login act  well do anything for logout, more clear I think
    private fun logout() {

        CacheUtil.setUserInfo(null)
        CacheUtil.savaString(CacheUtil.LOGIN_METHOD, "")
        CacheUtil.savaString(CacheUtil.UNIPASS_PUBK, "")
        CacheUtil.savaString(CacheUtil.UNIPASS_PRIK, "")

        LoginAct.go(this@SettingsActivity)
        finish()
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
        lifecycleScope.launch {
            editMethodChannel = prepareFlutterEngine(this@SettingsActivity, FlutterProxyActivity.ENGINE_ID_EDIT_PROFILE, FlutterProxyActivity.ROUTE_EDIT_PROFILE, FlutterProxyActivity.CHANNEL_SETTING, FlutterProxyActivity.SCENE_SETTING_EDIT){
                    scene, method, call, result ->
                handleFlutterEvent(scene, method, call, result)
            }

            privacyMethodChannel = prepareFlutterEngine(this@SettingsActivity, FlutterProxyActivity.ENGINE_ID_ACCOUNT_PRIVACY, FlutterProxyActivity.ROUTE_ACCOUNT_PRIVACY, FlutterProxyActivity.CHANNEL_SETTING, FlutterProxyActivity.SCENE_SETTING_PRIVACY){
                    scene, method, call, result ->
                handleFlutterEvent(scene, method, call, result)
            }
        }
    }

    private fun handleFlutterEvent(
        scene: Int,
        method: MethodChannel,
        call: MethodCall,
        result: MethodChannel.Result
    ) {
        if (call.method == "flutterToast") {
            val type = call.argument<Int>("type") ?: NotificationDialog.TYPE_SUCCESS
            val content = call.argument<String>("content")
            if (content?.isNotEmpty() == true) {
                ToastDialogHolder.getDialog()?.show(this@SettingsActivity, type, content)
            }
            result.success("success")
        }else{
            handleFlutterCommonEvent(this, scene, method, call, result)
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
        ConnectedAccountActivity.go(this)
    }

    private fun goLanguage() {
        LanguageActivity.go(this)
    }

    override fun onDestroy() {
        super.onDestroy()
//        FlutterEngineCache.getInstance().get(FlutterProxyActivity.ENGINE_ID_EDIT_PROFILE)?.destroy()
        FlutterEngineCache.getInstance().get(FlutterProxyActivity.ENGINE_ID_ACCOUNT_PRIVACY)?.destroy()
    }
}
