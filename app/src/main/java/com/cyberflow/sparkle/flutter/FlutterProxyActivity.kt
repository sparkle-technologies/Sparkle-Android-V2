package com.cyberflow.sparkle.flutter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.beust.klaxon.Klaxon
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.model.User
import com.cyberflow.base.net.GsonConverter
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.util.bus.SparkleEvent
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.MyApp
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ActivityFlutterProxyBinding
import com.cyberflow.sparkle.widget.NotificationDialog
import com.hjq.language.LocaleContract
import com.hjq.language.MultiLanguages
import com.hyphenate.easeui.ui.dialog.LoadingDialogHolder
import dev.pinkroom.walletconnectkit.core.chains.toJson
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.android.RenderMode
import io.flutter.embedding.android.TransparencyMode
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel


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
class FlutterProxyActivity : BaseDBAct<BaseViewModel, ActivityFlutterProxyBinding>() {

    companion object {

        const val ENGINE_ID_EDIT_PROFILE = "eidt_profile"
        const val ROUTE_EDIT_PROFILE = "/profile/edit"

        const val ENGINE_ID_ACCOUNT_PRIVACY = "account_privacy"
        const val ROUTE_ACCOUNT_PRIVACY = "/profile/accountPrivacy"

        const val ENGINE_ID_NOTIFICATION_LIST = "notification_list"
        const val ROUTE_NOTIFICATION_LIST = "/notification/list"

        const val ENGINE_ID_ASTRO_CODE = "profile_astro_code"
        const val ROUTE_ASTRO_CODEE = "/profile/chart"

        const val ENGINE_ID_SYNASTRY = "profile_synastry"
        const val ROUTE_SYNASTRY = "/profile/astrolabe"

        const val ENGINE_ID_TAROT = "chat_tarot"
        const val ROUTE_TAROT = "/tarot"


        const val ENGINE_ID_COMMON = "common_engin"
        const val ROUTE_COMMON = "/"


        const val CHANNEL_SETTING = "settingChannel"
        const val CHANNEL_NOTIFICATION = "notificationChannel"
        const val CHANNEL_START_SIGN = "starSignChannel"
        const val CHANNEL_SYNASTRY = "synastryChannel"
        const val CHANNEL_TAROT = "tarotChannel"
        const val CHANNEL_COMMON = "commonChannel"

        const val EVENT_BUS_DESTROY = "event_bus_destroy_flutter_proxy"

        const val SCENE_COMMON = 1000
        const val SCENE_SETTING_EDIT = 1001
        const val SCENE_SETTING_PRIVACY = 1002
        const val SCENE_PROFILE_EDIT = 1003
        const val SCENE_NOTIFICATION_LIST = 1004
        const val SCENE_ASTRO_CODE = 1005
        const val SCENE_SYNASTRY = 1006
        const val SCENE_TAROT = 1007


        fun go(context: Context, engineName: String) {
            val intent = Intent(context, FlutterProxyActivity::class.java)
            intent.putExtra("engineName", engineName)
            context.startActivity(intent)
        }

        fun prepareFlutterEngine(context: Context, engineName: String, routeName: String, channelName: String, scene: Int, handleFlutterEvent: (scene:Int, methodChannel: MethodChannel, call: MethodCall, result: MethodChannel.Result) -> Unit) : MethodChannel? {
//            val flutterEngine = FlutterEngine(context)
//            flutterEngine.navigationChannel.setInitialRoute(routeName)
//            flutterEngine.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
            val flutterEngine = MyApp.instance.engines.createAndRunEngine(context, DartExecutor.DartEntrypoint.createDefault(), routeName)
            FlutterEngineCache.getInstance().put(engineName, flutterEngine)
            val methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)
            methodChannel?.setMethodCallHandler { call, result ->
                handleFlutterEvent.invoke(scene, methodChannel, call, result)
            }
            return methodChannel
        }


        fun handleFlutterCommonEvent(activity: Activity, scene: Int, methodChannel: MethodChannel, call: MethodCall, result: MethodChannel.Result) {
            // handle flutter caller
            Log.e(TAG, "handle flutter event   method: ${call.method}")

            if (call.method == "flutterDestroy") {
                result.success("success")
//                methodChannel.setMethodCallHandler(null)
                LiveDataBus.get().with(EVENT_BUS_DESTROY).postValue(System.currentTimeMillis())
//                FlutterActivity.withCachedEngine(ENGINE_ID_EDIT_PROFILE).destroyEngineWithActivity(false)
//            go(this) // singleTask
//            recreate()
            }

            if (call.method == "flutterInitalized") {
                result.success("success")
                initParams(scene, methodChannel)
            }

            if(call.method == "flutterShowLoadingView"){
                LoadingDialogHolder.getLoadingDialog()?.hide()
                result.success("success")
            }

            if(call.method == "flutterHideLoadingView"){
                LoadingDialogHolder.getLoadingDialog()?.show(activity)
                result.success("success")
            }

            if (call.method == "flutterToast") {
                val type = call.argument<Int>("type") ?: NotificationDialog.TYPE_SUCCESS
                val content = call.argument<String>("content")
                if (content?.isNotEmpty() == true) {
                    (activity as? BaseDBAct<*, *>)?.myToast(type, content)
                }
                result.success("success")
            }

            if (call.method == "saveProfileSuccess") {
                val userStr = call.argument<HashMap<String, String>>("user")
                result.success("success")

                Log.e("flutter", "android receive form:${userStr.toJson()} ")
                val user = GsonConverter.gson.fromJson(userStr.toJson(), User::class.java)
                Log.e("flutter", "android receive form:$user ")

                CacheUtil.getUserInfo()?.also {
                    it.user?.apply {
                        user?.also { new ->
                            birth_time = new.birth_time
                            birthdate = new.birthdate
                            birthplace_info = new.birthplace_info
                            location_info = new.location_info
                            nick = new.nick
                            signature = new.signature
                            profile_permission = new.profile_permission
                            gender = new.gender
                            label_list = new.label_list

                            CacheUtil.setUserInfo(it)
                            LiveDataBus.get().with(SparkleEvent.PROFILE_CHANGED).postValue("time:${System.currentTimeMillis()}")
                        }
                    }
                }
            }
        }
        
        fun initParams(scene: Int, methodChannel: MethodChannel) {
            var local = "zh-Hans-CN"
            val current = MultiLanguages.getAppLanguage()
            if (current.language.equals(LocaleContract.getEnglishLocale().language)) {
                local = "en_US"
            }
            CacheUtil.getUserInfo()?.apply {
                val openUid = user?.open_uid.orEmpty()
                val token = token
                var map = mutableMapOf<String, Any>()

                map["token"] = token
                map["openuid"] = openUid
                map["localeLanguage"] = local
                map["userAgent"] = "Sparkle/1.0 (iPhone; iOS 16.4; Scale/3.00)"

                if(scene == SCENE_PROFILE_EDIT){
                    map["editBio"] = 1
                }

                Log.e(TAG, "initParams: scene=$scene" )

                if(scene == SCENE_PROFILE_EDIT || scene == SCENE_SETTING_EDIT || scene == SCENE_SETTING_PRIVACY){
                    val jsonString = GsonConverter.gson.toJson(user)
                    val userMap = Klaxon().parse<Map<String, String>>(jsonString).orEmpty()
                    map["user"] = userMap
                }

                if(scene == SCENE_ASTRO_CODE){
                    val jsonString = GsonConverter.gson.toJson(user?.star_sign)
                    val strList = Klaxon().parseArray<Map<String, String>>(jsonString).orEmpty()
                    map["star_sign"] = strList
                }

                val params = GsonConverter.gson.toJson(map)
                Log.e(TAG, "initParams:  params: $params")

                invoke(methodChannel, "nativeShareParams", map)
            }
        }

        fun initParams(openUid: String, methodChannel: MethodChannel) {
            var local = "zh-Hans-CN"
            val current = MultiLanguages.getAppLanguage()
            if (current.language.equals(LocaleContract.getEnglishLocale().language)) {
                local = "en_US"
            }
            CacheUtil.getUserInfo()?.apply {
                val token = token
                var map = mutableMapOf<String, Any>()
                map["token"] = token
                map["open_uid"] = openUid
//                map["token"] = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJVaWQiOjMxNCwiT3BlblVpZCI6IjE2OTMyMjI0LTcxNjEtNDY3My04MjEwLTQ2NTk4NmRmMDA5MiIsIk9wZW5JZCI6IjB4NTc4YzY3MDg4MDU4MjYyZGVmNjUyMTBiYzkzM2E3MjcxMTZiMTUyOSIsIkF1dGhUeXBlIjoyLCJCdWZmZXJUaW1lIjoyNTkyMDAsImV4cCI6MTcwMTk1MDE4MCwiaXNzIjoic3BhcmtsZSIsIm5iZiI6MTcwMTM0NDM4MH0.nLgje_92Gbyp9co3Y3yb1S08xvijGRrCXgAtNgx1u2c"
//                map["open_uid"] = "eebe94a3-fb8d-403f-9696-6be1a9e43eb3"
                map["localeLanguage"] = local
                map["userAgent"] = "Sparkle/1.0 (iPhone; iOS 16.4; Scale/3.00)"
                val params = GsonConverter.gson.toJson(map)
                Log.e(TAG, "initParams:  params: $params")
                invoke(methodChannel, "nativeShareParams", map)
            }
        }

        fun initTarotParams(msgId: String?, questionStr: String?,  methodChannel: MethodChannel?) {
            var local = "zh-Hans-CN"
            val current = MultiLanguages.getAppLanguage()
            if (current.language.equals(LocaleContract.getEnglishLocale().language)) {
                local = "en_US"
            }
            CacheUtil.getUserInfo()?.apply {
                val token = token
                var map = mutableMapOf<String, Any>()
                map["token"] = token
                map["localeLanguage"] = local
                map["userAgent"] = "Sparkle/1.0 (iPhone; iOS 16.4; Scale/3.00)"
                map["msgId"] = msgId.orEmpty()
                map["question"] = questionStr.orEmpty()
                invoke(methodChannel, "nativeShareParams", map)
            }
        }

        fun nativeTarotResult(msgId: String?, hasResult: String, methodChannel: MethodChannel?) {
            var map = mutableMapOf<String, Any>()
            map["msgId"] = msgId.orEmpty()
            map["hasResult"] = hasResult
//            map["result"] = result
            invoke(methodChannel, "nativeNotifyTarotResult", map)
        }

        /**

        channel.invokeMethod('flutterOpenFlutterVC', {
            'route': '/profile/chart/details',
            'params': {'index': index, 'star_sign': starSign}, //map
        });

        starSign.value = result['params']['star_sign'] ?? [];
        starSignIndex = result['params']['index'] ?? 0;

         */
        fun initParams(params: Any?, methodChannel: MethodChannel) {
            var local = "zh-Hans-CN"
            val current = MultiLanguages.getAppLanguage()
            if (current.language.equals(LocaleContract.getEnglishLocale().language)) {
                local = "en_US"
            }
            CacheUtil.getUserInfo()?.apply {
                val token = token
                var map = mutableMapOf<String, Any>()
                map["token"] = token
                map["params"] = params ?: ""
//                map["token"] = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJVaWQiOjMxNCwiT3BlblVpZCI6IjE2OTMyMjI0LTcxNjEtNDY3My04MjEwLTQ2NTk4NmRmMDA5MiIsIk9wZW5JZCI6IjB4NTc4YzY3MDg4MDU4MjYyZGVmNjUyMTBiYzkzM2E3MjcxMTZiMTUyOSIsIkF1dGhUeXBlIjoyLCJCdWZmZXJUaW1lIjoyNTkyMDAsImV4cCI6MTcwMTk1MDE4MCwiaXNzIjoic3BhcmtsZSIsIm5iZiI6MTcwMTM0NDM4MH0.nLgje_92Gbyp9co3Y3yb1S08xvijGRrCXgAtNgx1u2c"
//                map["open_uid"] = "eebe94a3-fb8d-403f-9696-6be1a9e43eb3"
                map["localeLanguage"] = local
                map["userAgent"] = "Sparkle/1.0 (iPhone; iOS 16.4; Scale/3.00)"
                val params = GsonConverter.gson.toJson(map)
                Log.e(TAG, "initParams:  params: $params")
                invoke(methodChannel, "nativeShareParams", map)
            }
        }

        private fun invoke(methodChannel: MethodChannel?, method: String, map: MutableMap<String, Any>) {
            methodChannel?.apply { this.invokeMethod(method, map, object : MethodChannel.Result {
                override fun success(result: Any?) {
                    Log.e(TAG, "initParams success: ")
                }

                override fun error(
                    errorCode: String,
                    errorMessage: String?,
                    errorDetails: Any?
                ) {
                    Log.e(TAG, "initParams errorCode: ")
                }

                override fun notImplemented() {
                    Log.e(TAG, "initParams notImplemented: ")
                }
            })
            }
        }
    }



    override fun initView(savedInstanceState: Bundle?) {
        intent.getStringExtra("engineName")?.let {
            val fragment = FlutterFragment.withCachedEngine(it).renderMode(RenderMode.texture)
                .transparencyMode(TransparencyMode.transparent).build<FlutterFragment>()
            this.supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun initData() {
      LiveDataBus.get().with(EVENT_BUS_DESTROY).observe(this){
            finish()
      }
    }
}