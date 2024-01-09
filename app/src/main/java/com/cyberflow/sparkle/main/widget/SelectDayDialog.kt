package com.cyberflow.sparkle.main.widget

import android.content.Context
import android.content.res.Configuration
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.main.widget.calendar.CalendarAdapter
import com.cyberflow.sparkle.main.widget.calendar.DateBean
import com.cyberflow.sparkle.main.widget.calendar.getMonthEngStr
import com.cyberflow.sparkle.widget.BaseDialog
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.gyf.immersionbar.ImmersionBar
import java.util.Calendar

class SelectDayDialog: BaseDialog  {

    override fun onStart() {
        super.onStart()
        dialog!!.setCanceledOnTouchOutside(true)

        val dm: DisplayMetrics = mContext!!.resources.displayMetrics
        val width = (dm.heightPixels.toFloat() * 700 / 812).toInt()
        mWindow?.apply {
            setGravity(Gravity.BOTTOM)
            setWindowAnimations(com.cyberflow.base.resources.R.style.BottomDialog_Animation)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, width)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
//        mWindow?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, mWidthAndHeight!![1] / 2)
    }

    override fun initImmersionBar() {
        super.initImmersionBar()
        ImmersionBar.with(this).navigationBarColor(com.cyberflow.base.resources.R.color.white_2nd).init()
    }


    private var mContext: Context? = null
    private var mCallback: Callback? = null

    private var isWeek = false
    private var birthDate: DateBean? = null
    private var currentDate: DateBean? = null

    interface Callback {
        fun onSelected(select: DateBean?)
    }

    constructor(context: Context, week: Boolean, birth: DateBean?, current: DateBean?, callback: Callback) {
        if (context == null || callback == null) {
            return
        }

        mContext = context
        mCallback = callback
        isWeek = week
        birthDate = birth
        currentDate = current
    }

    override fun setLayoutId() = R.layout.dialog_calendar

    private var tvMonth: TextView? = null
    private var tvYear: TextView? = null
    private var viewPager2: ViewPager2? = null
    private var calendarAdapter: CalendarAdapter? = null
    private var btnPrevious: ShadowImgButton? = null
    private var btnNext: ShadowImgButton? = null

    override fun initView() {
        mRootView?.apply {
            tvMonth = findViewById(R.id.tv_month)
            tvYear = findViewById(R.id.tv_year)
            viewPager2 = findViewById(R.id.vpContainer)
            calendarAdapter = CalendarAdapter(mCallback, isWeek, birthDate, currentDate)
            viewPager2?.adapter = calendarAdapter
            btnPrevious = findViewById(R.id.btn_previous)
            btnNext = findViewById(R.id.btn_next)

            btnPrevious?.setClickListener(object : ShadowImgButton.ShadowClickListener {
                override fun clicked() {
                    viewPager2?.apply {
                        if (currentItem > 0) {
                            setCurrentItem(currentItem - 1, true)
                        }
                    }
                    handleBtn()
                }
            })

            btnNext?.setClickListener(object : ShadowImgButton.ShadowClickListener {
                override fun clicked() {
                    viewPager2?.apply {
                        if (currentItem < calendarAdapter?.itemCount!! - 1) {
                            setCurrentItem(currentItem + 1, true)
                        }
                    }
                    handleBtn()
                }
            })
        }
    }

    override fun initData() {
        val data: MutableList<Calendar> = ArrayList()
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
//        Log.e("TAG", "initData: $birthDate    currentYear=$currentYear  currentMonth=$currentMonth",)
        birthDate?.let {  // 从出生日期到去年
            val count = currentYear * 12 + currentMonth - (it.year * 12 + it.month)
//            Log.e("TAG", "--1---count--: $count", )
            for (i in count downTo 1) {
//                Log.e("TAG", "-----1-----: i=$i", )
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MONTH, -i)
                data.add(calendar)
            }
        }
        val currentIdx = data.size - 1
//        Log.e("TAG", "after count1    data.size=${data.size}" )
        // 从今年 到  2100 年
        val count = 2100 * 12 - (currentYear * 12 + currentMonth)
//        Log.e("TAG", "---2---:  count=$count" )

        for (i in 0 until count) {
//            Log.e("TAG", "-----2-----: i=$i", )
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, i)
            data.add(calendar)
        }

        Log.e("TAG", "initData: data.size=${data.size}")
        calendarAdapter?.refreshData(data)
//        val txt =  "${data[data.size - 1][Calendar.YEAR]}-${(data[data.size - 1][Calendar.MONTH] + 1)}"

        data[currentIdx + 1].apply {
            tvMonth?.text = getMonthEngStr(get(Calendar.MONTH) + 1)
            tvYear?.text = get(Calendar.YEAR).toString()
        }

        viewPager2?.apply {
            offscreenPageLimit = 1
            setCurrentItem(currentIdx + 1, false)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val year = data[position][Calendar.YEAR]
                    val month = data[position][Calendar.MONTH] + 1
                    tvMonth?.text = getMonthEngStr(month)
                    tvYear?.text = year.toString()

                    val recyclerView = viewPager2!!.getChildAt(0) as RecyclerView
                    val view = recyclerView.layoutManager!!.findViewByPosition(position)
                    view?.let { updatePagerHeightForChild(it, viewPager2) }
                }
            })
        }

        handleBtn()
    }

    private fun handleBtn(){
        val cur = viewPager2?.currentItem ?: 0
        val size = calendarAdapter?.itemCount ?: 0
        Log.e("TAG", "handleBtn: cur=$cur  size=$size" )
        btnPrevious?.disableBg(cur == 0)
        btnNext?.disableBg((size == 0) || (cur == (size - 1)) )
    }

    fun updatePagerHeightForChild(view: View, pager: ViewPager2?) {
        view.post {
            val weightMeasureSpec =
                View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val heightMeasureSpec =
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(weightMeasureSpec, heightMeasureSpec)
            pager?.apply {
                if (layoutParams.height != view.measuredHeight) {
                    val layoutParams = pager.layoutParams
                    layoutParams.height = view.measuredHeight
                    pager.layoutParams = layoutParams
                }
            }
        }
    }

    fun show(activity: FragmentActivity) {
        activity?.apply {
            if (dialog == null) {
                show(supportFragmentManager, "SelectDayDialogFragment")
            }
        }
    }
}