package com.cyberflow.sparkle.main.widget.calendar

import android.app.Dialog
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.cyberflow.base.util.dialogSlipDismiss
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.widget.ShadowImgButton
import java.util.Calendar

class CalendarDialog {

    private var mContext: Context? = null
    private var mCallback: Callback? = null

    private var mDialog: Dialog? = null
    private var isWeek = false
    private var birthDate: DateBean? = null

    interface Callback {
        fun onSelected(select: DateBean?)
    }

    constructor(context: Context, week: Boolean, birth: DateBean?, callback: Callback) {
        if (context == null || callback == null) {
            return
        }

        mContext = context
        mCallback = callback
        isWeek = week
        birthDate = birth

        initView()
        initData()
    }


    private fun initView() {
        mDialog = Dialog(mContext!!, com.cyberflow.base.resources.R.style.forward_dialog)
        mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog?.setContentView(R.layout.dialog_calendar)

        //2、通过Resources获取
        val dm: DisplayMetrics = mContext!!.resources.displayMetrics
        val width = (dm.heightPixels.toFloat() * 700 / 812).toInt()
        mDialog?.setCancelable(true)
        mDialog?.setCanceledOnTouchOutside(true)
        val window = mDialog?.window
        if (window != null) {
            val lp = window.attributes
            lp.gravity = Gravity.BOTTOM
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = width
            window.attributes = lp
            window.setWindowAnimations(com.cyberflow.base.resources.R.style.BottomDialog_Animation)
//            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        }

        mDialog?.apply {
            root = findViewById(R.id.root)
            tvMonth = findViewById(R.id.tv_month)
            tvYear = findViewById(R.id.tv_year)
            viewPager2 = findViewById(R.id.vpContainer)
            calendarAdapter = CalendarAdapter(mCallback, isWeek, birthDate)
            viewPager2?.adapter = calendarAdapter
            btnPrevious = findViewById(R.id.btn_previous)
            btnNext = findViewById(R.id.btn_next)
        }

        mDialog?.window?.decorView?.dialogSlipDismiss {
            mDialog?.dismiss()
        }
       /* root?.dialogSlipDismiss {
            mDialog?.dismiss()
        }*/

        btnPrevious?.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                viewPager2?.apply {
                    if (currentItem > 0) {
                        setCurrentItem(currentItem - 1, true)
                    }
                }
            }
        })

        btnNext?.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                viewPager2?.apply {
                    if (currentItem < calendarAdapter?.itemCount!! - 1) {
                        setCurrentItem(currentItem + 1, true)
                    }
                }
            }
        })
    }


    private var root: View? = null
    private var tvMonth: TextView? = null
    private var tvYear: TextView? = null
    private var viewPager2: ViewPager2? = null
    private var calendarAdapter: CalendarAdapter? = null
    private var btnPrevious: ShadowImgButton? = null
    private var btnNext: ShadowImgButton? = null


    private fun initData() {
        val data: MutableList<Calendar> = ArrayList()
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1

        Log.e(
            "TAG",
            "initData: $birthDate    currentYear=$currentYear  currentMonth=$currentMonth",
        )

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
            offscreenPageLimit = 3
            setCurrentItem(currentIdx + 1, false)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val year = data[position][Calendar.YEAR]
                    val month = data[position][Calendar.MONTH] + 1
                    tvMonth?.text = getMonthEngStr(month)
                    tvYear?.text = year.toString()
//                    val recyclerView = viewPager2!!.getChildAt(0) as RecyclerView
//                    val view = recyclerView.layoutManager!!.findViewByPosition(position)
//                    view?.let { updatePagerHeightForChild(it, viewPager2) }
                }
            })
        }
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

    fun show() {
        mDialog!!.show()
    }

    fun onDestroy() {
        if (mDialog != null) {
            mDialog!!.dismiss()
            mDialog = null
        }
    }
}