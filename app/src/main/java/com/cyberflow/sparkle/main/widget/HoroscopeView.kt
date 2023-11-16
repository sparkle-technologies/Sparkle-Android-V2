package com.cyberflow.sparkle.main.widget

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HoroscopeView : RecyclerView.ViewHolder {

    constructor(itemView: View) : super(itemView) {
//        initView(itemView)
    }

    /*
    private fun initView(itemView: View) {
        mDatabind.rv.linear().setup {
            addType<HoroscopeItem>(R.layout.item_horoscope)
            addType<HoroscopeHeadItem>(R.layout.item_horoscope_head)
            addType<EmptyItem>(R.layout.item_horoscope_empty)
            onClick(R.id.left) { anima(MainHoroscopeFragment.INDEX_LOVE) }
            onClick(R.id.center) { anima(MainHoroscopeFragment.INDEX_FORTUNE) }
            onClick(R.id.right) { anima(MainHoroscopeFragment.INDEX_CAREER) }
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
        }

        mDatabind.bgLove.setOnClickListener { anima(MainHoroscopeFragment.INDEX_LOVE) }
        mDatabind.bgFortune.setOnClickListener { anima(MainHoroscopeFragment.INDEX_FORTUNE) }
        mDatabind.bgCareer.setOnClickListener { anima(MainHoroscopeFragment.INDEX_CAREER) }

        mDatabind.barLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
//            Log.e("TAG", "verticalOffset: $verticalOffset" )
//            Log.e("TAG", "totel: ${appBarLayout.totalScrollRange}" )
            val rate = 1 - abs(verticalOffset) / appBarLayout.totalScrollRange.toFloat()
//            Log.e("TAG", "rate: $rate", )
            if(rate < 0.05f )
                mDatabind.layPan.alpha = 0.0f
            else
                mDatabind.layPan.alpha = 1.0f
        }
    }*/

    fun initData(data: String) {
        /*actVm?.apply {
            horoScopeData.observe(this@MainHoroscopeFragment) {
                Log.e("TAG", "horoScopeData.observe ")
                mDatabind.state.showContent()
                freshData(it)
            }
            requestData()
        }

        mDatabind.state.onError {
            findViewById<ShadowTxtButton>(R.id.btn).setClickListener(object :
                ShadowTxtButton.ShadowClickListener {
                override fun clicked(disable: Boolean) {
                    requestData()
                }
            })
        }*/
    }

   /* private fun requestData() {
        mDatabind.state.showLoading()
        mDatabind.state.scope {
            actVm?.horoScopeData?.value = Post<DailyHoroScopeData>(Api.DAILY_HOROSCOPE) {}.await()
        }
    }

    private fun freshData(data: DailyHoroScopeData?) {
        data?.also {
            mDatabind.apply {
                showTotalAnima(it.total_score)

                tvLove.text = it.love_score.toString()
                tvFortune.text = it.wealth_score.toString()
                tvCareer.text = it.career_score.toString()
                anima(MainHoroscopeFragment.INDEX_LOVE)
            }
        }
    }

    private fun showTotalAnima(dd: Int) {
        mDatabind.smc.setPercentWithAnimation(dd)

        ValueAnimator.ofInt(0, dd).apply {
            duration = (12 * dd).toLong()
            interpolator = LinearInterpolator()
            addUpdateListener {
                mDatabind.tvRange.text = it.animatedValue.toString()
            }
            start()
        }
    }


    private val arrays = arrayOf(
        com.cyberflow.base.resources.R.drawable.main_bg_page_half_left_200_radius,
        com.cyberflow.base.resources.R.drawable.main_bg_page_selected_0_radius,
        com.cyberflow.base.resources.R.drawable.main_bg_page_half_right_200_radius
    )

    private var lastIndex = -1
    private fun anima(index: Int = INDEX_LOVE) {
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


    companion object {
        const val INDEX_LOVE = 0
        const val INDEX_FORTUNE = 1
        const val INDEX_CAREER = 2
    }

    // 数据转化
    private fun getData(index: Int = INDEX_LOVE): List<Any> {
        val result = arrayListOf<Any>()
        actVm?.horoScopeData?.value?.also {
            val data = when (index) {
                INDEX_LOVE -> {
                    it.love_progress_list?.map { horo ->
                        HoroscopeItem(name = horo.title, desc = horo.content, line = index)
                    }
                }

                INDEX_FORTUNE -> {
                    it.wealth_progress_list?.map { horo ->
                        HoroscopeItem(name = horo.title, desc = horo.content, line = index)
                    }
                }

                INDEX_CAREER -> {
                    it.career_progress_list?.map { horo ->
                        HoroscopeItem(name = horo.title, desc = horo.content, line = index)
                    }
                }

                else -> {
                    arrayListOf<Any>()
                }
            }

            if (data.isNullOrEmpty()) {
                result.add(HoroscopeHeadItem())
                result.add(EmptyItem())
            } else {
                result.add(HoroscopeHeadItem())
                result.addAll(data)
            }
        }

        return result
    }*/


}