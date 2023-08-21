package com.cyberflow.sparkle

import android.os.Bundle
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.ext.COLOR_TRANSPARENT
import com.cyberflow.base.ext.immersive
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.databinding.ActivityMainBinding

class MainActivity : BaseDBAct<BaseViewModel, ActivityMainBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        immersive(COLOR_TRANSPARENT, true)
    }

    override fun initData() {

    }
}