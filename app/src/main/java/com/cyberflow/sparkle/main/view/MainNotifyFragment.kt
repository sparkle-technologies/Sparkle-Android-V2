package com.cyberflow.sparkle.main.view

import android.os.Bundle
import android.util.Log
import com.cyberflow.base.act.BaseVMAct
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.net.GsonConverter
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.FragmentMainNotifyBinding
import com.cyberflow.sparkle.widget.NotificationDialog
import com.cyberflow.sparkle.widget.ToastDialogHolder
import com.hjq.language.LocaleContract
import com.hjq.language.MultiLanguages
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.android.RenderMode
import io.flutter.embedding.android.TransparencyMode
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel

class MainNotifyFragment : BaseDBFragment<BaseViewModel, FragmentMainNotifyBinding>() {
    override fun initData() {

    }

    override fun initView(savedInstanceState: Bundle?) {
        initFlutter()
    }

    private val ENGINE_ID_NOTIFY = "notiry"
    lateinit var flutterEngine: FlutterEngine
    private var methodChannel: MethodChannel? = null

    private fun initFlutter() {
        flutterEngine = FlutterEngine(requireActivity())
        flutterEngine.navigationChannel.setInitialRoute("/notification/list")
        flutterEngine.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
        FlutterEngineCache.getInstance().put(ENGINE_ID_NOTIFY, flutterEngine)

        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "notificationChannel")
        methodChannel?.setMethodCallHandler { call, result ->
            Log.e(BaseVMAct.TAG, "handle flutter event   method: ${call.method}" )

            if(call.method == "flutterDestroy"){
                result.success("success")
            }

            if(call.method == "flutterInitalized"){
                result.success("success")
                callFlutter()
            }

            if(call.method == "flutterToast"){
                val type = call.argument<String>("type")
                val content = call.argument<String>("content")
                if(content?.isNotEmpty() == true){
                    val t = when(type){
                        "success" -> NotificationDialog.TYPE_SUCCESS
                        "error" -> NotificationDialog.TYPE_ERROR
                        else -> NotificationDialog.TYPE_WARN
                    }
                    ToastDialogHolder.getDialog()?.show(requireActivity().applicationContext, t, content)
                }
                result.success("success")
            }
        }

        val fragment = FlutterFragment.withCachedEngine(ENGINE_ID_NOTIFY)
            .renderMode(RenderMode.texture)
            .transparencyMode(TransparencyMode.transparent)
            .build<FlutterFragment>()
        requireActivity()
            .supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()
    }

    private fun callFlutter() {
        var local = "zh-Hans-CN"
        val current = MultiLanguages.getAppLanguage()
        if(current.language.equals(LocaleContract.getEnglishLocale().language)){
            local = "en_US"
        }
        CacheUtil.getUserInfo()?.apply {
            val openUid = user?.open_uid.orEmpty()
            val token = token
            var map = mutableMapOf<String, Any>()
            map["token"] = token
            map["localeLanguage"] = local
            map["openuid"] = openUid
            val params = GsonConverter.gson.toJson(map)
            Log.e(BaseVMAct.TAG, "callFlutter:  params: $params" )
            methodChannel?.invokeMethod("nativeShareParams", map, object : MethodChannel.Result {
                override fun success(result: Any?) {
                    Log.e(BaseVMAct.TAG, "callFlutter success: ")
                }

                override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                    Log.e(BaseVMAct.TAG, "callFlutter errorCode: ")
                }

                override fun notImplemented() {
                    Log.e(BaseVMAct.TAG, "callFlutter notImplemented: ")
                }
            })
        }
    }
}