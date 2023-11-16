package com.cyberflow.sparkle.main.view

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.util.ToastUtil
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.databinding.FragmentMainHoroscopeBinding
import com.cyberflow.sparkle.main.viewmodel.MainViewModel
import com.cyberflow.sparkle.main.widget.SelectMonthDialog
import com.cyberflow.sparkle.main.widget.SelectYearDialog
import com.cyberflow.sparkle.main.widget.calendar.CalendarDialog
import com.cyberflow.sparkle.main.widget.calendar.DateBean
import com.youth.banner.Banner
import com.youth.banner.transformer.AlphaPageTransformer

class MainHoroscopeFragment : BaseDBFragment<BaseViewModel, FragmentMainHoroscopeBinding>() {

    private val TAG = "MainHoroscopeFragment"

    override fun initData() {
        mDatabind.tabLayout.setTabData(arrayOf("Daily", "Weekly", "Monthly", "Yearly"))

        var banner2 = (mDatabind.banner as Banner<String, HoroscopeAdapter>)
        banner2.apply {
            addBannerLifecycleObserver(requireActivity())
            setAdapter(HoroscopeAdapter(imageUrls))
            removeIndicator()
            isAutoLoop(false)
            setBannerGalleryEffect(10, 10, 1f)
            addPageTransformer(AlphaPageTransformer())
        }
    }

    private var imageUrls = listOf(
        "https://img.zcool.cn/community/01b72057a7e0790000018c1bf4fce0.png",
        "https://img.zcool.cn/community/016a2256fb63006ac7257948f83349.jpg",
        "https://img.zcool.cn/community/01700557a7f42f0000018c1bd6eb23.jpg"
    )

    private var actVm: MainViewModel? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        actVm = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView(savedInstanceState: Bundle?) {
        mDatabind.ivOwl.setOnClickListener {
//            mDatabind.smc.setPercentWithAnimation(50)
        }

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

    private fun showCalendarDialog() {
        when(mDatabind.tabLayout.currentTab){
            0 -> { showDailyOrWeekly(false) }
            1 -> { showDailyOrWeekly(true)  }
            2 -> { showMonth() }
            3 -> { showYear() }
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