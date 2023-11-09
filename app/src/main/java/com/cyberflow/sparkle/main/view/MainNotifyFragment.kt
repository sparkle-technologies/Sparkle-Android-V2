package com.cyberflow.sparkle.main.view

import android.os.Bundle
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.FragmentMainNotifyBinding
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.android.RenderMode
import io.flutter.embedding.android.TransparencyMode
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor

class MainNotifyFragment : BaseDBFragment<BaseViewModel, FragmentMainNotifyBinding>() {
    override fun initData() {

    }

    override fun initView(savedInstanceState: Bundle?) {
        initFlutter()
    }

    private val ENGINE_ID_NOTIFY = "notiry"
    lateinit var flutterEngine: FlutterEngine

    private fun initFlutter() {
        flutterEngine = FlutterEngine(requireActivity())
        flutterEngine.navigationChannel.setInitialRoute("/notification/list")
        flutterEngine.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
        FlutterEngineCache.getInstance().put(ENGINE_ID_NOTIFY, flutterEngine)
        val fragment = FlutterFragment.withCachedEngine(ENGINE_ID_NOTIFY)
            .renderMode(RenderMode.texture)
            .transparencyMode(TransparencyMode.transparent)
            .build<FlutterFragment>()
        requireActivity()
            .supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()
    }
}