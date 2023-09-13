package com.cyberflow.sparkle.main.view

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.databinding.FragmentMainLeftBinding
import com.cyberflow.sparkle.main.viewmodel.MainViewModel
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.cyberflow.sparkle.R

class MainLeftFragment : BaseDBFragment<BaseViewModel, FragmentMainLeftBinding>() {

    override fun initData() {

    }

    private var actVm: MainViewModel? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        actVm = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDatabind.smc.setPercentWithAnimation(100)

        mDatabind.ivOwl.setOnClickListener {
            mDatabind.smc.setPercentWithAnimation(50)
        }

        mDatabind.rv.linear().setup {
            addType<HoroscopeItem>(R.layout.item_horoscope)
        }.models = getData()

    }

    private fun getData(): List<Any> {
        return listOf(
            HoroscopeItem(),
            HoroscopeItem(),
            HoroscopeItem(),
            HoroscopeItem(),
            HoroscopeItem()
        )
    }
}