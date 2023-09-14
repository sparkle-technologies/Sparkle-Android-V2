package com.cyberflow.sparkle.main.view

import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.lifecycle.ViewModelProvider
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.util.dp2px
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.FragmentMainLeftBinding
import com.cyberflow.sparkle.main.viewmodel.MainViewModel
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import kotlin.math.abs

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
            addType<HoroscopeHeadItem>(R.layout.item_horoscope_head)
            onClick(R.id.left){ anima(0) }
            onClick(R.id.center){ anima(1) }
            onClick(R.id.right){ anima(2) }
        }.models = getData()

        mDatabind.bgLove.setOnClickListener { anima(0) }
        mDatabind.bgFortune.setOnClickListener { anima(1) }
        mDatabind.bgCareer.setOnClickListener { anima(2) }

        mDatabind.barLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
//            Log.e("TAG", "verticalOffset: $verticalOffset" )
//            Log.e("TAG", "totel: ${appBarLayout.totalScrollRange}" )
            val rate = 1 - abs(verticalOffset) / appBarLayout.totalScrollRange.toFloat()
//            Log.e("TAG", "rate: $rate", )
            mDatabind.layPan.alpha = rate
        }
    }


    private val arrays = arrayOf(
        com.cyberflow.base.resources.R.drawable.main_bg_page_half_left_200_radius,
        com.cyberflow.base.resources.R.drawable.main_bg_page_selected_0_radius,
        com.cyberflow.base.resources.R.drawable.main_bg_page_half_right_200_radius)

    private var lastIndex = 0
    private fun anima(index: Int){

        Log.e("Click", "anima:  index=$index  lastIndex=$lastIndex" )

        if(index == lastIndex) return
        val dis = mDatabind.bgCareer.width + dp2px(5f).toFloat()
        var st = lastIndex * dis
        var et = index * dis
//        Log.e("TAG", "anima: dis=$dis")
        val anim = ObjectAnimator.ofFloat(mDatabind.bgTmp, "translationX", st, et).apply {
            duration = 100
            addUpdateListener {
//                Log.e("TAG", "anima: ${it.animatedValue}", )
                if(it.animatedValue as Float > dis/2){
                    mDatabind.bgTmp.setBackgroundResource(
                        arrays[index]
                    )
                }
            }
        }
        lastIndex = index
        anim.start()
    }


    private fun getData(): List<Any> {
        return listOf(
            HoroscopeHeadItem(),
            HoroscopeItem(),
            HoroscopeItem(),
            HoroscopeItem(),
            HoroscopeItem(),
            HoroscopeItem()
        )
    }
}