package com.cyberflow.sparkle.main.view

import android.os.Bundle
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.DBComponent
import com.cyberflow.sparkle.databinding.FragmentMainProfileBinding
import com.cyberflow.sparkle.profile.view.ProfileAct

class MainProfileFragment : BaseDBFragment<BaseViewModel, FragmentMainProfileBinding>() {
    override fun initData() {

    }

    override fun initView(savedInstanceState: Bundle?) {
        mDatabind.ivHead.setOnClickListener {
            ProfileAct.go(requireActivity(), CacheUtil.getUserInfo()?.user?.open_uid.orEmpty())
        }

        CacheUtil.getUserInfo()?.user?.apply {
            DBComponent.loadAvatar(mDatabind.ivHead, avatar, gender)
        }
    }
}