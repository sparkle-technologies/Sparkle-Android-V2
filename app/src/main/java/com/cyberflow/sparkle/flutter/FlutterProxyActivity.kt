package com.cyberflow.sparkle.flutter

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
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ActivityFlutterProxyBinding
import com.hjq.language.LocaleContract
import com.hjq.language.MultiLanguages
import dev.pinkroom.walletconnectkit.core.chains.toJson
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.android.RenderMode
import io.flutter.embedding.android.TransparencyMode
import io.flutter.embedding.engine.FlutterEngine
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

        const val ENGINE_ID_HOROSCOPE = "profile_horoscope"
        const val ROUTE_HOROSCOPE = "/profile/astrolabe"

        const val CHANNEL_SETTING = "settingChannel"
        const val CHANNEL_NOTIFICATION = "notificationChannel"
        const val CHANNEL_HOROSCOPE = "horoscopeChannel"

        const val EVENT_BUS_DESTROY = "event_bus_destroy_flutter_proxy"

        const val SCENE_SETTING_EDIT = 1001
        const val SCENE_SETTING_PRIVACY = 1002
        const val SCENE_PROFILE_EDIT = 1003
        const val SCENE_NOTIFICATION_LIST = 1004
        const val SCENE_HOROSCOPE = 1005

        fun go(context: Context, engineName: String) {
            val intent = Intent(context, FlutterProxyActivity::class.java)
            intent.putExtra("engineName", engineName)
            context.startActivity(intent)
        }

        fun prepareFlutterEngine(context: Context, engineName: String, routeName: String, channelName: String, scene: Int, handleFlutterEvent: (scene:Int, methodChannel: MethodChannel, call: MethodCall, result: MethodChannel.Result) -> Unit) : MethodChannel? {
            val flutterEngine = FlutterEngine(context)
            flutterEngine.navigationChannel.setInitialRoute(routeName)
            flutterEngine.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
            FlutterEngineCache.getInstance().put(engineName, flutterEngine)
            val methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)
            methodChannel?.setMethodCallHandler { call, result ->
                handleFlutterEvent.invoke(scene, methodChannel, call, result)
            }
            return methodChannel
        }


        fun handleFlutterCommonEvent(scene: Int, methodChannel: MethodChannel, call: MethodCall, result: MethodChannel.Result) {
            // handle flutter caller
            Log.e(TAG, "handle flutter event   method: ${call.method}")

            if (call.method == "flutterDestroy") {
                result.success("success")
                LiveDataBus.get().with(EVENT_BUS_DESTROY).postValue(System.currentTimeMillis())
//                FlutterActivity.withCachedEngine(ENGINE_ID_EDIT_PROFILE).destroyEngineWithActivity(false)
//            go(this) // singleTask
//            recreate()
            }

            if (call.method == "flutterInitalized") {
                result.success("success")
                initParams(scene, methodChannel)
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

                            CacheUtil.setUserInfo(it)
                            LiveDataBus.get().with(SparkleEvent.PROFILE_CHANGED)
                                .postValue("time:${System.currentTimeMillis()}")
                        }
                    }
                }
            }

            if (call.method == "flutterSaveLabelList") {
                val newList = call.argument<List<String>>("label_list")
                result.success("success")
//                Log.e("flutter", "android receive form:${newList.toJson()} ")
                val cache = CacheUtil.getUserInfo()
//                Log.e(TAG, "handleFlutterCommonEvent:-  before edit: ${GsonConverter.gson.toJson(cache)}"  )
                cache?.user?.label_list = newList
                CacheUtil.setUserInfo(cache)
//                val cache2 = CacheUtil.getUserInfo()
//                Log.e(TAG, "handleFlutterCommonEvent:- after edit: ${GsonConverter.gson.toJson(cache2)}"  )
                LiveDataBus.get().with(SparkleEvent.PROFILE_CHANGED).postValue("time:${System.currentTimeMillis()}")
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

                val jsonString = GsonConverter.gson.toJson(user)
                val userMap = Klaxon().parse<Map<String, String>>(jsonString).orEmpty()

                map["token"] = token
                map["openuid"] = openUid
                map["localeLanguage"] = local

                if(scene == SCENE_PROFILE_EDIT){
                    map["editBio"] = 1
                }

//                Log.e(TAG, "initParams: scene=$scene" )

                if(scene == SCENE_PROFILE_EDIT || scene == SCENE_SETTING_EDIT || scene == SCENE_SETTING_PRIVACY){
                    map["user"] = userMap
                }

                val params = GsonConverter.gson.toJson(map)
                Log.e(TAG, "initParams:  params: $params")

                methodChannel?.apply { this.invokeMethod("nativeShareParams", map, object : MethodChannel.Result {
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