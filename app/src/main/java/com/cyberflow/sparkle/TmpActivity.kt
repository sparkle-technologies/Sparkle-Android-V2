package com.cyberflow.sparkle

import android.os.Bundle
import com.bumptech.glide.Glide
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.databinding.ActivityTmpBinding

class TmpActivity : BaseDBAct<BaseViewModel, ActivityTmpBinding>() {


    override fun initView(savedInstanceState: Bundle?) {

        Glide.with(this)
            .load(R.drawable.avatar)
            .skipMemoryCache(true)
            .into(mDataBinding.iv)
    }

    override fun initData() {

    }
}