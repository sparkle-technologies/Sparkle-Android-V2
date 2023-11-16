package com.cyberflow.sparkle.main.view

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.lifecycle.ViewModelProvider
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.model.DailyHoroScopeData
import com.cyberflow.base.net.Api
import com.cyberflow.base.util.ToastUtil
import com.cyberflow.base.util.dp2px
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.FragmentMainHoroscopeBinding
import com.cyberflow.sparkle.databinding.ItemHoroscopeBinding
import com.cyberflow.sparkle.main.viewmodel.MainViewModel
import com.cyberflow.sparkle.main.widget.SelectMonthDialog
import com.cyberflow.sparkle.main.widget.SelectYearDialog
import com.cyberflow.sparkle.main.widget.calendar.CalendarDialog
import com.cyberflow.sparkle.main.widget.calendar.DateBean
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Post
import com.drake.net.utils.scope
import kotlin.math.abs

class MainHoroscopeFragment : BaseDBFragment<BaseViewModel, FragmentMainHoroscopeBinding>() {

    private val TAG = "MainHoroscopeFragment"

    override fun initData() {

        mDatabind.tabLayout.setTabData(arrayOf("Daily", "Weekly", "Monthly", "Yearly"))

        actVm?.apply {
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
        }

//        setAlarm()
    }

    private fun requestData() {
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
                anima(INDEX_LOVE)
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

        mDatabind.rv.linear().setup {
            addType<HoroscopeItem>(R.layout.item_horoscope)
            addType<HoroscopeHeadItem>(R.layout.item_horoscope_head)
            addType<EmptyItem>(R.layout.item_horoscope_empty)
            onClick(R.id.left) { anima(INDEX_LOVE) }
            onClick(R.id.center) { anima(INDEX_FORTUNE) }
            onClick(R.id.right) { anima(INDEX_CAREER) }
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

        mDatabind.bgLove.setOnClickListener { anima(INDEX_LOVE) }
        mDatabind.bgFortune.setOnClickListener { anima(INDEX_FORTUNE) }
        mDatabind.bgCareer.setOnClickListener { anima(INDEX_CAREER) }

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

        const val ALARM_EVENT = "com.cyberflow.sparkle.main.view.MainLeftFragment.AlarmReceiver"
        const val LIVEDATA_BUS_EVENT = "alarm_refresh_horoscope"
    }

   /* private var alarmReceiver: AlarmReceiver? = null


    class AlarmReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Toast.makeText(context, "There's A New Day Coming", Toast.LENGTH_LONG).show()
            Log.e("TAG", "AlarmReceiver onReceive: ")
            LiveDataBus.get().with(LIVEDATA_BUS_EVENT).postValue("AlarmReceiver ${System.currentTimeMillis()}")
        }
    }*/

   /* private fun setAlarm() {
        val intent = Intent(ALARM_EVENT)
        val pIntent = PendingIntent.getBroadcast(
            requireActivity(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        (requireActivity().getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager).also {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            it.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pIntent)
        }

        LiveDataBus.get().with(LIVEDATA_BUS_EVENT).observe(this) {
            requestData()
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            alarmReceiver = AlarmReceiver()
            IntentFilter(ALARM_EVENT).also {
                requireActivity().registerReceiver(alarmReceiver, it)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().unregisterReceiver(alarmReceiver)
        }
    }*/

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

        /*return listOf(
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
        )*/
    }

    val des = arrayOf(
        "During this period of time, you will have great challenges no matter from the spiritual or material level change.",
        "Some birds are not meant to be caged, that's all. Their feathers are too bright, their songs too sweet and wild. ",
        "but still, the place where you live is that much more drab and empty for their departure. --------- I Love You - Billie Eilish ------ " +
                "when the summary time What a waste of time I can't even remember now  And what was I so worried about? It's such a beautiful world"
    )
}