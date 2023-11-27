package com.cyberflow.sparkle.main.view

import android.os.Bundle
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.FragmentMainNotifyBinding
import com.cyberflow.sparkle.flutter.FlutterProxyActivity
import com.cyberflow.sparkle.widget.NotificationDialog
import com.cyberflow.sparkle.widget.ToastDialogHolder
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.android.RenderMode
import io.flutter.embedding.android.TransparencyMode
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class MainNotifyFragment : BaseDBFragment<BaseViewModel, FragmentMainNotifyBinding>() {
    override fun initData() {

    }

    override fun initView(savedInstanceState: Bundle?) {
        initFlutter()
    }

    private var methodChannel : MethodChannel? = null

    private fun initFlutter() {
        methodChannel = FlutterProxyActivity.prepareFlutterEngine(requireActivity(), FlutterProxyActivity.ENGINE_ID_NOTIFICATION_LIST, FlutterProxyActivity.ROUTE_NOTIFICATION_LIST, FlutterProxyActivity.CHANNEL_NOTIFICATION, FlutterProxyActivity.SCENE_NOTIFICATION_LIST) { scene, method, call, result ->
            handleFlutterEvent(scene, method, call, result)
        }
        val fragment = FlutterFragment.withCachedEngine(FlutterProxyActivity.ENGINE_ID_NOTIFICATION_LIST)
            .renderMode(RenderMode.texture)
            .transparencyMode(TransparencyMode.transparent)
            .build<FlutterFragment>()
        requireActivity()
            .supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()
    }

    private fun handleFlutterEvent(
        scene: Int,
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
                ToastDialogHolder.getDialog()?.show(requireActivity(), t, content)
            }
            result.success("success")
        }else{
            FlutterProxyActivity.handleFlutterCommonEvent(scene, method, call, result)
        }
    }
}