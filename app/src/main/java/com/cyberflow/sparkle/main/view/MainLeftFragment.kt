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
import com.cyberflow.sparkle.databinding.ItemHoroscopeBinding
import com.cyberflow.sparkle.main.viewmodel.MainViewModel
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import kotlin.math.abs
import kotlin.random.Random

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
            onClick(R.id.left) { anima(0) }
            onClick(R.id.center) { anima(1) }
            onClick(R.id.right) { anima(2) }
            onBind {
//                Log.e("TAG", "modelCount: $modelCount  layoutPosition: $layoutPosition ")
                when (itemViewType) {
                    R.layout.item_horoscope -> {
                        getBinding<ItemHoroscopeBinding>().apply {
                            getModel<HoroscopeItem>().line.apply {
                                val left = if (this == 0) View.VISIBLE else View.INVISIBLE
                                dotLeft.visibility = left
                                lineLeftUp.visibility = left
                                lineLeftDown.visibility = left

                                val center = if (this == 1) View.VISIBLE else View.INVISIBLE
                                dotCenter.visibility = center
                                lineCenterUp.visibility = center
                                lineCenterDown.visibility = center

                                val right = if (this == 2) View.VISIBLE else View.INVISIBLE
                                dotRight.visibility = right
                                lineRightUp.visibility = right
                                lineRightDown.visibility = right
                            }
                            if (modelCount == layoutPosition + 1) {   // handle footer view
                                lineCenterDown.visibility = View.INVISIBLE
                                lineLeftDown.visibility = View.INVISIBLE
                                lineRightDown.visibility = View.INVISIBLE
                            }
                        }
                    }
                }
            }
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
        com.cyberflow.base.resources.R.drawable.main_bg_page_half_right_200_radius
    )

    private var lastIndex = 0
    private fun anima(index: Int) {
        val dd = Random.nextInt(100)
        Log.e("TAG", "anima: dd = $dd", )
        mDatabind.smc.setPercentWithAnimation(dd)
        mDatabind.tvRange.text = dd.toString()

//        Log.e("Click", "anima:  index=$index  lastIndex=$lastIndex")

        if (index == lastIndex) return

        mDatabind.rv.models = getData(index)

        val dis = mDatabind.bgCareer.width + dp2px(5f).toFloat()
        var st = lastIndex * dis
        var et = index * dis
//        Log.e("TAG", "anima: dis=$dis")
        val anim = ObjectAnimator.ofFloat(mDatabind.bgTmp, "translationX", st, et).apply {
            duration = 100
            addUpdateListener {
//                Log.e("TAG", "anima: ${it.animatedValue}", )
                if (it.animatedValue as Float > dis / 2) {
                    mDatabind.bgTmp.setBackgroundResource(
                        arrays[index]
                    )
                }
            }
        }
        lastIndex = index
        anim.start()
    }

    val des = arrayOf(
        "During this period of time, you will have great challenges no matter from the spiritual or material level change.",
        "Some birds are not meant to be caged, that's all. Their feathers are too bright, their songs too sweet and wild. ",
        "but still, the place where you live is that much more drab and empty for their departure. --------- I Love You - Billie Eilish ------ " +
                "when the summary time What a waste of time I can't even remember now  And what was I so worried about? It's such a beautiful world"
    )

    private fun getData(index: Int = 0): List<Any> {
        return listOf(
            HoroscopeHeadItem(),
            HoroscopeItem(name = "Pattern $index", desc = des[(index) % des.size], line = index),
            HoroscopeItem(name = "Pattern $index", desc = des[(index) % des.size], line = index),
            HoroscopeItem(name = "Pattern $index", desc = des[(index) % des.size], line = index),
            HoroscopeItem(name = "Pattern $index", desc = des[(index) % des.size], line = index),
            HoroscopeItem(name = "Pattern $index", desc = des[(index) % des.size], line = index),
            HoroscopeItem(name = "Pattern $index", desc = des[(index) % des.size], line = index),
            HoroscopeItem(name = "Pattern $index", desc = des[(index) % des.size], line = index),
            HoroscopeItem(name = "Pattern $index", desc = des[(index) % des.size], line = index),
            HoroscopeItem(name = "Pattern $index", desc = des[(index) % des.size], line = index),
            HoroscopeItem(name = "Pattern $index", desc = des[(index) % des.size], line = index),
            HoroscopeItem(name = "Pattern $index", desc = des[(index) % des.size], line = index),
        )
    }
}