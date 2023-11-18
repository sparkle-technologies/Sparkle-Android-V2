package com.cyberflow.sparkle.main.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.util.ToastUtil
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.databinding.FragmentMainHoroscopeBinding
import com.cyberflow.sparkle.main.widget.SelectMonthDialog
import com.cyberflow.sparkle.main.widget.SelectYearDialog
import com.cyberflow.sparkle.main.widget.calendar.CalendarDialog
import com.cyberflow.sparkle.main.widget.calendar.DateBean
import com.flyco.tablayout.listener.OnTabSelectListener
import com.youth.banner.Banner
import com.youth.banner.listener.OnPageChangeListener
import com.youth.banner.transformer.AlphaPageTransformer

class MainHoroscopeFragment : BaseDBFragment<BaseViewModel, FragmentMainHoroscopeBinding>() {

    private val TAG = "MainHoroscopeFragment"

    override fun initData() {
        mDatabind.tabLayout.setTabData(arrayOf("Daily", "Weekly", "Monthly", "Yearly"))
        mDatabind.tabLayout.setOnTabSelectListener(object : OnTabSelectListener{
            override fun onTabSelect(position: Int) {
                Log.e(TAG, "onTabSelect: position=$position", )
                selectMode = position
            }

            override fun onTabReselect(position: Int) {
                Log.e(TAG, "onTabReselect: position=$position", )
            }
        })

        val d1 = DateBean(year = 2023, month = 11, day = 17)
        val d2 = DateBean(year = 2023, month = 11, day = 18)
        val d3 = DateBean(year = 2023, month = 11, day = 19)

        val horoAdpter = HoroscopeAdapter(arrayListOf(HoroscopeReq(DAILY, 0, d1), HoroscopeReq(DAILY, 1, d2), HoroscopeReq(DAILY, 2, d3)))
        var realPos = 0
        var previousPos = 0

        var banner2 = (mDatabind.banner as Banner<HoroscopeReq, HoroscopeAdapter>)
        banner2.apply {
            addBannerLifecycleObserver(requireActivity())
            viewPager2.offscreenPageLimit = 1
            setAdapter(horoAdpter)
            removeIndicator()
            isAutoLoop(false)
            setBannerGalleryEffect(10, 10, 1f)
            addPageTransformer(AlphaPageTransformer())
            addOnPageChangeListener(object : OnPageChangeListener{
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
//                    Log.e(TAG, "onPageScrolled: position=$position positionOffset=$positionOffset   positionOffsetPixels=$positionOffsetPixels" )
                }

                // 初始化是0  左滑是2  右滑是1   201   012  120
                override fun onPageSelected(position: Int) {
                    Log.e(TAG, "onPageSelected: position=$position" )
                    if(position == 0){
                        if(previousPos == 2) realPos++
                        if(previousPos == 1) realPos--
                    }
                    if(position == 1){
                        if(previousPos == 0) realPos++
                        if(previousPos == 2) realPos--
                    }
                    if(position == 2){
                        if(previousPos == 1) realPos++
                        if(previousPos == 0) realPos--
                    }
                    previousPos = position

                    Log.e(TAG, "onPageSelected: realPos=${horoAdpter.getRealPosition(position)}", )

                    horoAdpter.slideUpdate(position, realPos)
                }

                override fun onPageScrollStateChanged(state: Int) {
//                    Log.e(TAG, "onPageScrollStateChanged: state=$state" )
                }
            })
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView(savedInstanceState: Bundle?) {
        mDatabind.layCalendar.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    mDatabind.layCalendar.visibility = View.INVISIBLE
                    mDatabind.layCalendarShadow.visibility = View.VISIBLE
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mDatabind.layCalendar.visibility = View.VISIBLE
                    mDatabind.layCalendarShadow.visibility = View.INVISIBLE
                }
            }
            false
        }

        mDatabind.layCalendar.setOnClickListener {
            showCalendarDialog()
        }
    }

    companion object {
        const val DAILY = 0
        const val WEEKLY = 1
        const val MONTH = 2
        const val YEAR = 3
    }

    private var selectMode = DAILY

    private fun showCalendarDialog() {
        when(mDatabind.tabLayout.currentTab){
            DAILY -> { showDailyOrWeekly(false) }
            WEEKLY -> { showDailyOrWeekly(true)  }
            MONTH -> { showMonth() }
            YEAR -> { showYear() }
        }
    }

    private var yearDialog : SelectYearDialog? = null
    private fun showYear(){
        Log.e("TAG", "showYear: " )
        yearDialog = SelectYearDialog(requireActivity() ,  object : SelectYearDialog.Callback {
            override fun onSelected(select: DateBean?) {
                Log.e(TAG, "onSelected: $select" )
                ToastUtil.show(requireContext(), "${select?.year}")
//                yearDialog?.onDestroy()
            }
        })
        yearDialog?.show()
    }

    private var monthDialog : SelectMonthDialog? = null

    private fun showMonth(){
        monthDialog = SelectMonthDialog(requireActivity() ,  object : SelectMonthDialog.Callback {
            override fun onSelected(select: DateBean?) {
                Log.e(TAG, "onSelected: $select" )
                ToastUtil.show(requireContext(), "${select?.month}")
//                monthDialog?.onDestroy()
            }
        })
        monthDialog?.show()
    }

    private var calendarDialog : CalendarDialog? = null
    private fun showDailyOrWeekly(isWeek: Boolean){
        calendarDialog = CalendarDialog(requireActivity(), isWeek ,  object : CalendarDialog.Callback {
            override fun onSelected(select: DateBean??) {
                Log.e(TAG, "onSelected: $select" )
                ToastUtil.show(requireContext(), "${select?.year}-${select?.month}-${select?.day}")
//                calendarDialog?.onDestroy()
            }
        })
        calendarDialog?.show()
    }
}