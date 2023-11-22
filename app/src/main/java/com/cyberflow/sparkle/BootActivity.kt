package com.cyberflow.sparkle

import android.os.Bundle
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.databinding.ActivityBootBinding
import com.cyberflow.sparkle.register.view.RegisterAct

class BootActivity : BaseDBAct<BaseViewModel, ActivityBootBinding>() {

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initData() {
        mDataBinding.root.postDelayed({
            jump()
        }, 3 * 1000)
    }

    private fun jump(){
//        LoginAct.go(this)
        RegisterAct.go(this)
    }
}