package com.cyberflow.sparkle.main.view

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.FragmentMainFeedsBinding
import jp.wasabeef.blurry.Blurry


class MainFeedsFragment : BaseDBFragment<BaseViewModel, FragmentMainFeedsBinding>() {
    override fun initData() {

    }

    @SuppressLint("ResourceType")
    override fun initView(savedInstanceState: Bundle?) {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.feed_bg)
        mDatabind.iv1.setImageBitmap(bitmap)
        Blurry.with(context).radius(25).sampling(8).from(bitmap).into(mDatabind.iv1)
    }
}