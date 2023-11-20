package com.cyberflow.sparkle.setting.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.net.GsonConverter
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.ToastUtil
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.MyApp
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
import com.cyberflow.sparkle.databinding.ActivitySettingBinding
import com.cyberflow.sparkle.im.DBManager
import com.cyberflow.sparkle.login.view.LoginAct
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.drake.net.utils.withMain
import com.google.firebase.auth.FirebaseAuth
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.launch

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
        mDataBinding.btnLogout.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked(d: Boolean) {
                Toast.makeText(this@SettingsActivity, "ready logout now", Toast.LENGTH_LONG).show()
                logout()
            }
        })
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

            withMain{
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

    private fun web3Auth() {

    }

    override fun initData() {
        initFlutter()
    }

    // aar 方式导入module
    // https://medium.com/@FlavioAro/how-to-integrate-a-flutter-module-into-your-native-android-application-52c41eeb6154

    // 依赖方式  目前用这种
    // https://flutter.cn/docs/add-to-app/android/project-setup#option-b---depend-on-the-modules-source-code
    // https://github.com/flutter/flutter/issues/99735

    // 数据传递  消息双向通行
    //https://juejin.cn/post/7220295071060164663
    // https://blog.csdn.net/zhujiangtaotaise/article/details/111352652

    // 初始化的时候带参数过去  考虑用框架
    // https://github.com/alibaba/flutter_boost/blob/master/docs/install.md

    // 原生 flutter 相互调用方法
    //https://blog.csdn.net/zhujiangtaotaise/article/details/111352652

    // 打包release报错  ...':flutter:copyFlutterAssetsRelease' (type 'Copy').
    // 改了代码 /Users/blackjack/Desktop/flutter/flutter/packages/flutter_tools/gradle/src/main/groovy/flutter.groovy
    // https://github.com/flutter/flutter/issues/129471

    /**

    // if (!isUsedAsSubproject) {
    //     def variantOutput = variant.outputs.first()
    //     def processResources = variantOutput.hasProperty("processResourcesProvider") ?
    //     variantOutput.processResourcesProvider.get() : variantOutput.processResources
    //     processResources.dependsOn(copyFlutterAssetsTask)
    // }

        def variantOutput = variant.outputs.first()
        def processResources = variantOutput.hasProperty("processResourcesProvider") ?
        variantOutput.processResourcesProvider.get() : variantOutput.processResources
        processResources.dependsOn(copyFlutterAssetsTask)

    useful commands:
        flutter clean
        flutter pub get

     */

    private val ENGINE_ID_EDIT_PROFILE = "eidt_profile"
    private val ENGINE_ID_ACCOUNT_PRIVACY = "account_privacy"
    private val ENGINE_ID_CONNECTED_ACCOUNT = "connected_account"

    lateinit var flutterEngine_edit_profile: FlutterEngine
    lateinit var flutterEngine_account_privacy: FlutterEngine
    private fun initFlutter() {
        flutterEngine_edit_profile = FlutterEngine(this)
        flutterEngine_account_privacy = FlutterEngine(this)

        flutterEngine_edit_profile.navigationChannel.setInitialRoute("/profile/edit")
        flutterEngine_account_privacy.navigationChannel.setInitialRoute("/profile/accountPrivacy")

        flutterEngine_edit_profile.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
        flutterEngine_account_privacy.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())

        FlutterEngineCache.getInstance().put(ENGINE_ID_EDIT_PROFILE, flutterEngine_edit_profile)
        FlutterEngineCache.getInstance().put(ENGINE_ID_ACCOUNT_PRIVACY, flutterEngine_account_privacy)

        methodChannel = MethodChannel(flutterEngine_edit_profile.dartExecutor.binaryMessenger, "flutter_bridge")
        methodChannel?.setMethodCallHandler { call, result ->
            // handle flutter caller
            Log.e(TAG, "handle flutter event   method: ${call.method}" )

            if (call.method == "openMethodChannel") {
                val name = call.argument<String>("name")
                val age = call.argument<Int>("age")
                Log.e("flutter", "android receive form:$name ,$age ")
                result.success("success")
            }

            if(call.method == "flutterDestroy"){
//                FlutterActivity.withCachedEngine(ENGINE_ID_EDIT_PROFILE).destroyEngineWithActivity(false)
                SettingsActivity.go(this) // singleTask
                result.success("success")
            }

            if(call.method == "flutterInitalized"){
                result.success("success")
                callFlutter()
            }
        }
    }

    private var methodChannel: MethodChannel? = null

    private fun callFlutter() {
        CacheUtil.getUserInfo()?.apply {
            val openUid = user?.open_uid.orEmpty()
            val token = token
            var map = mutableMapOf<String, String>()
            map["local"] = "zh"
            map["token"] = token
            map["openuid"] = openUid
            val params = GsonConverter.gson.toJson(map)
            Log.e(TAG, "callFlutter:  params: $params" )
            methodChannel?.invokeMethod("nativeShareParams", map, object : MethodChannel.Result {
                override fun success(result: Any?) {
                    Log.e(TAG, "callFlutter success: ")
                }

                override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                    Log.e(TAG, "callFlutter errorCode: ")
                }

                override fun notImplemented() {
                    Log.e(TAG, "callFlutter notImplemented: ")
                }
            })
        }
    }

    private fun goEditProfile() {
        startActivity(FlutterActivity.withCachedEngine(ENGINE_ID_EDIT_PROFILE).build(this))
    }

    private fun goAccountPrivacy() {
        startActivity(FlutterActivity.withCachedEngine(ENGINE_ID_ACCOUNT_PRIVACY).build(this))
    }

    private fun goConnetedAccount() {
        ToastUtil.show(this, "coming soon ")
    }
}