package com.cyberflow.sparkle.main.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.util.bus.SparkleEvent
import com.cyberflow.base.util.safeClick
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.FragmentMainHoroscopeBinding
import com.cyberflow.sparkle.im.DBManager
import com.cyberflow.sparkle.main.widget.SelectMonthDialog
import com.cyberflow.sparkle.main.widget.SelectYearDialog
import com.cyberflow.sparkle.main.widget.calendar.CalendarDialog
import com.cyberflow.sparkle.main.widget.calendar.DateBean
import com.flyco.tablayout.listener.OnTabSelectListener
import com.youth.banner.Banner
import com.youth.banner.listener.OnPageChangeListener
import com.youth.banner.transformer.AlphaPageTransformer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar

class MainHoroscopeFragment : BaseDBFragment<BaseViewModel, FragmentMainHoroscopeBinding>() {

    private val TAG = "MainHoroscopeFragment"

    private fun topBar(position: Int){
        selectMode = position
        startDate = today()
        setAdapter()
    }

    private fun calendar(select: DateBean?){
        select?.let {
            startDate = it
            setAdapter()
        }
    }

    private var startDate : DateBean? = null

    private fun today(): DateBean{
        val c = Calendar.getInstance()
        return DateBean(year = c[Calendar.YEAR], month = c[Calendar.MONTH] + 1, day = c[Calendar.DAY_OF_MONTH])
    }

    var horoAdpter:HoroscopeAdapter? = null
    var realPos = 0
    var previousPos = 0

    private fun setAdapter(){
        realPos = 0
        previousPos = 0

        startDate?.let {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, it.year)
            calendar.set(Calendar.MONTH, it.month - 1)
            calendar.set(Calendar.DAY_OF_MONTH, it.day)

            var d1 = DateBean(year = it.year, month = it.month, day = it.day)
            var d2 = DateBean(year = it.year, month = it.month, day = it.day)
            var d3 = DateBean(year = it.year, month = it.month, day = it.day)

            var banner2 = (mDatabind.banner as Banner<HoroscopeReq, HoroscopeAdapter>)
            banner2.setUserInputEnabled(true)

            handleScrollRange(banner2, calendar, selectMode)
            showSelectDateTitle(calendar, selectMode)

            when(selectMode){
                DAILY -> {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    updateDate(d2, calendar)
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    updateDate(d3, calendar)
                }
                WEEKLY -> {
                    calendar.add(Calendar.WEEK_OF_MONTH, 1)
                    updateDate(d2, calendar)
                    calendar.add(Calendar.WEEK_OF_MONTH, 1)
                    updateDate(d3, calendar)
                }
                MONTH -> {
                    calendar.add(Calendar.MONTH, 1)
                    updateDate(d2, calendar)
                    calendar.add(Calendar.MONTH, 1)
                    updateDate(d3, calendar)
                }
                YEAR -> {
                    calendar.add(Calendar.YEAR, 1)
                    updateDate(d2, calendar)
                    calendar.add(Calendar.YEAR, 1)
                    updateDate(d3, calendar)
                    banner2.setUserInputEnabled(false)
                }
            }

            horoAdpter = HoroscopeAdapter(arrayListOf(HoroscopeReq(selectMode, 0, d1), HoroscopeReq(selectMode, 1, d2), HoroscopeReq(selectMode, 2, d3)))
            banner2.setAdapter(horoAdpter)
        }
    }

    private fun updateDate(dd: DateBean, calendar: Calendar){
        val realYear = calendar.get(Calendar.YEAR)
        val realMonth = calendar.get(Calendar.MONTH) + 1
        val realDay = calendar.get(Calendar.DAY_OF_MONTH)
        dd.year = realYear
        dd.month = realMonth
        dd.day = realDay
    }

    override fun initData() {
//        mDatabind.tabLayout.setTabData(arrayOf(getString(com.cyberflow.base.resources.R.string.daily), getString(com.cyberflow.base.resources.R.string.weekly), getString(com.cyberflow.base.resources.R.string.monthly), getString(com.cyberflow.base.resources.R.string.yearly)))
        mDatabind.tabLayout.setTabData(arrayOf(getString(com.cyberflow.base.resources.R.string.daily), getString(com.cyberflow.base.resources.R.string.monthly), getString(com.cyberflow.base.resources.R.string.yearly)))
        mDatabind.tabLayout.setOnTabSelectListener(object : OnTabSelectListener{
            override fun onTabSelect(position: Int) {
                Log.e(TAG, "onTabSelect: position=$position")

                when(position){
                    0 -> topBar(DAILY)
//                    1 -> topBar(WEEKLY)
                    1 -> topBar(MONTH)
                    2 -> topBar(YEAR)
                }
            }

            override fun onTabReselect(position: Int) {
                Log.e(TAG, "onTabReselect: position=$position")
            }
        })

        var banner2 = (mDatabind.banner as Banner<HoroscopeReq, HoroscopeAdapter>)
        banner2.apply {
            addBannerLifecycleObserver(requireActivity())
            viewPager2.offscreenPageLimit = 3
//            setAdapter(horoAdpter)
            removeIndicator()

            isAutoLoop(false)
            setBannerGalleryEffect(10, 10, 1f)
            addPageTransformer(AlphaPageTransformer(0.2f))
            addOnPageChangeListener(object : OnPageChangeListener{
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    Log.e(TAG, "onPageScrolled: position=$position positionOffset=$positionOffset   positionOffsetPixels=$positionOffsetPixels" )
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

                    startDate?.let {
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.YEAR, it.year)
                        calendar.set(Calendar.MONTH, it.month - 1)
                        calendar.set(Calendar.DAY_OF_MONTH, it.day)
                        when(selectMode){
                            DAILY -> {
                                calendar.add(Calendar.DAY_OF_MONTH, realPos)
                            }
                            WEEKLY -> {
                                calendar.add(Calendar.WEEK_OF_MONTH, realPos)
                            }
                            MONTH -> {
                                calendar.add(Calendar.MONTH, realPos)
                            }
                            YEAR -> {
                                calendar.add(Calendar.YEAR, realPos)
                            }
                        }
                        handleScrollRange(banner2, calendar, selectMode)
                        showSelectDateTitle(calendar, selectMode)
                    }
//                    Log.e(TAG, "onPageSelected: realPos=$realPos" )
                    horoAdpter?.slideUpdate(position, realPos)
                }

                override fun onPageScrollStateChanged(state: Int) {
                    Log.e(TAG, "onPageScrollStateChanged: state=$state" )
                }
            })

            setScrollResult { scrollLeft, scrollRight ->
                if(!scrollLeft){
                    (requireActivity() as? BaseDBAct<*, *>)?.toastSuccess(getString(R.string.happy_birthday_hint))
                }
            }
        }
        initBirthDate()
        topBar(DAILY)
    }


    private fun handleScrollRange(
        banner2: Banner<HoroscopeReq, HoroscopeAdapter>,
        calendar: Calendar,
        selectMode: Int
    ) {
        banner2.scrollLeft = true
        banner2.scrollRight = true

        when(selectMode){
            DAILY -> {
                val min = birthDate?.let {
                    it.year * 365 + it.month * 30 + it.day
                } ?: (1900 * 365 + 1 * 30 + 1)

                val max = currentDate?.let {
                    it.year * 365 + it.month * 30 + it.day
                } ?: (2100 * 365 + 1 * 30 + 1)

                val select = calendar.get(Calendar.YEAR) * 365 + (calendar.get(Calendar.MONTH) + 1) * 30 + calendar.get(Calendar.DAY_OF_MONTH)
                Log.e(TAG, "handleScrollRange: min=$min max=$max select=$select" )

                banner2.scrollLeft = select > min
                banner2.scrollRight = select <= max

                if(select == max + 1){
                    (requireActivity() as? BaseDBAct<*, *>)?.toastSuccess(getString(R.string.future_horoscope_hint))
                }
            }
            WEEKLY -> {
                 // todo
            }
            MONTH -> {
                val min = birthDate?.let {
                    it.year * 12 + it.month
                } ?: (1900 * 12 + 1)

                val max = currentDate?.let {
                    it.year * 12 + it.month
                } ?: (2100 * 12 + 1)

//                Log.e(TAG, "onPageSelected: birthDate=$birthDate " )
//                Log.e(TAG, "onPageSelected: currentDate=$currentDate " )

                val select = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH) + 1
                Log.e(TAG, "handleScrollRange: min=$min max=$max select=$select" )
                banner2.scrollLeft = select > min
                banner2.scrollRight = select <= max

                if(select == max + 1){
                    (requireActivity() as? BaseDBAct<*, *>)?.toastSuccess(getString(R.string.future_horoscope_hint))
                }
            }
            YEAR -> {
                // todo
            }
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
        mDatabind.layCalendar.safeClick{
            showCalendarDialog()
        }
    }

    // take a look: https://www.digitalocean.com/community/tutorials/java-simpledateformat-java-date-format
    private fun showSelectDateTitle(calendar: Calendar, selectMode : Int){
        var pattern = "MM-dd-yyyy"
        when(selectMode){
            DAILY -> {
                // Dec 13, 2023
                pattern = "MMM dd, yyyy"
            }
            WEEKLY -> {
                // Dec 13-Dec 23
                pattern = "MMM dd"
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
                calendar.add(Calendar.DAY_OF_MONTH, -dayOfWeek)
            }
            MONTH -> {
                // Dec 2023
                pattern = "MMM yyyy"
            }
            YEAR -> {
                // 2023
                pattern ="yyyy"
            }
        }
        val simpleDateFormat = SimpleDateFormat(pattern)
        val dateStr = simpleDateFormat.format(calendar.time)
        Log.e(TAG, " $dateStr" )
        if(selectMode == WEEKLY){
            calendar.add(Calendar.DAY_OF_MONTH, 6)
            val weekStr = simpleDateFormat.format(calendar.time)
            mDatabind.tvCalendar.text = "$dateStr-$weekStr"
            mDatabind.tvCalendarShadow.text = "$dateStr-$weekStr"
        }else{
            mDatabind.tvCalendar.text = dateStr
            mDatabind.tvCalendarShadow.text = dateStr
        }
    }

    companion object {
        const val DAILY = 0
        const val WEEKLY = -1  // 暂时不做
        const val MONTH = 1
        const val YEAR = 2
    }

    private var selectMode = DAILY

    private fun showCalendarDialog() {
        when(mDatabind.tabLayout.currentTab){
            0 -> { showDailyOrWeekly(false) }
//            1 -> { showDailyOrWeekly(true)  }
            1 -> { showMonth() }
            2 -> { showYear() }
        }
    }

    private var yearDialog : SelectYearDialog? = null
    private fun showYear(){
        Log.e("TAG", "showYear: " )
        yearDialog = SelectYearDialog(requireActivity() , birthDate, currentDate, object : SelectYearDialog.Callback {
            override fun onSelected(select: DateBean?) {
                Log.e(TAG, "onSelected: $select" )
//                ToastUtil.show(requireContext(), "${select?.year}")
                calendar(select)
                yearDialog?.onDestroy()
            }
        })
        yearDialog?.show()
    }

    private var monthDialog : SelectMonthDialog? = null

    private fun showMonth(){
        monthDialog = SelectMonthDialog(requireActivity(), birthDate, currentDate, object : SelectMonthDialog.Callback {
            override fun onSelected(select: DateBean?) {
                Log.e(TAG, "onSelected: $select" )
//                ToastUtil.show(requireContext(), "${select?.month}")
                calendar(select)
                monthDialog?.onDestroy()
            }
        })
        monthDialog?.show()
    }

    private var calendarDialog : CalendarDialog? = null
    private fun showDailyOrWeekly(isWeek: Boolean){
        calendarDialog = CalendarDialog(requireActivity(), isWeek, birthDate, currentDate,  object : CalendarDialog.Callback {
            override fun onSelected(select: DateBean??) {
                Log.e(TAG, "onSelected: $select" )
//                ToastUtil.show(requireContext(), "${select?.year}-${select?.month}-${select?.day}")
                calendar(select)
                calendarDialog?.onDestroy()
            }
        })
        calendarDialog?.show()
    }

    private var birthDate : DateBean? = null
    private var currentDate : DateBean? = null

    private fun initBirthDate(){
        LiveDataBus.get().with(SparkleEvent.PROFILE_CHANGED, String::class.java).observe(this){
            updateBirthday()
        }
        updateBirthday()
    }

    private fun updateBirthday(){
        CacheUtil.getUserInfo()?.user?.apply {
            try{
                Log.e(TAG, "initBirthDate: birthdate=$birthdate" )
                val format = "yyyy-MM-dd"
                val date = SimpleDateFormat(format).parse(birthdate)
                val calendar = Calendar.getInstance()
                currentDate = DateBean(year = calendar[Calendar.YEAR], month = calendar[Calendar.MONTH] + 1, day = calendar[Calendar.DAY_OF_MONTH])
                calendar.time = date
                birthDate = DateBean(year = calendar[Calendar.YEAR], month = calendar[Calendar.MONTH] + 1, day = calendar[Calendar.DAY_OF_MONTH])
//                birthDate = DateBean(year = 2000, month = 7, day = 26)
            }catch (e: Exception){}
        }
    }

    // update data if a new day coming
    private var lastMillis = System.currentTimeMillis()
    private var lastDayTag = ""
    override fun onResume() {
        super.onResume()
        if(System.currentTimeMillis() - lastMillis > 1000 * 60 * 30){  // every 30 minutes
            val calendar = Calendar.getInstance()
            val curMonth = calendar[Calendar.MONTH]
            val curDay = calendar[Calendar.DAY_OF_MONTH]
            if(lastDayTag.isEmpty()){
                lastDayTag = "$curMonth-$curDay"
            }else{
                if(lastDayTag != "$curMonth-$curDay"){
                    lastDayTag = "$curMonth-$curDay"
                }
            }
            lastMillis = System.currentTimeMillis()
            lifecycleScope.launch {
                DBManager.instance.db?.horoscopeCacheDao()?.deleteAll()   // clear cache
            }
        }
    }
}