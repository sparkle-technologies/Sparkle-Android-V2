package com.cyberflow.sparkle

import android.content.Intent
import android.os.Bundle
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.chat.ui.IMTestActivity
import com.cyberflow.sparkle.databinding.ActivityTmpBinding

class TmpActivity : BaseDBAct<BaseViewModel, ActivityTmpBinding>() {


    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.btn.setOnClickListener {
            val intent = Intent(this, IMTestActivity::class.java)
            startActivity(intent)
        }
    }

    override fun initData() {

    }
}