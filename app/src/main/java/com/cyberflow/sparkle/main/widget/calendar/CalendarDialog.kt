package com.cyberflow.sparkle.main.widget.calendar

import android.app.Dialog
import android.content.Context
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.cyberflow.base.model.IMUserInfo
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.widget.ShadowImgButton
import java.util.Calendar

class CalendarDialog {

    private var mContext: Context? = null
    private var mCallback: Callback? = null

    private var mDialog: Dialog? = null

    interface Callback {
        fun onSelected(user: IMUserInfo?, type: Int)
    }


    constructor(context: Context, callback: Callback) {
        if (context == null || callback == null) {
            return
        }

        mContext = context
        mCallback = callback

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
            tvMonth = findViewById(R.id.tv_month)
            tvYear = findViewById(R.id.tv_year)
            viewPager2 = findViewById(R.id.vpContainer)
            calendarAdapter = CalendarAdapter()
            viewPager2?.adapter = calendarAdapter
            btnPrevious = findViewById(R.id.btn_previous)
            btnNext = findViewById(R.id.btn_next)
        }

        btnPrevious?.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                viewPager2?.apply {
                    if (currentItem != 0) {
                        setCurrentItem(currentItem - 1, false)
                    }
                }
            }
        })

        btnNext?.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                viewPager2?.apply {
                    if (currentItem != 11) {
                        setCurrentItem(currentItem + 1, false)
                    }
                }
            }
        })
    }

    private var tvMonth: TextView? = null
    private var tvYear: TextView? = null
    private var viewPager2: ViewPager2? = null
    private var calendarAdapter: CalendarAdapter? = null
    private var btnPrevious: ShadowImgButton? = null
    private var btnNext: ShadowImgButton? = null

    private var monthCount = 240
    private fun initData() {
        val data: MutableList<Calendar> = ArrayList()
        for (i in monthCount downTo 0) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -i)
            data.add(calendar)
        }
        calendarAdapter?.refreshData(data)
//        val txt =  "${data[data.size - 1][Calendar.YEAR]}-${(data[data.size - 1][Calendar.MONTH] + 1)}"
        data[data.size - 1].apply {
            tvMonth?.text = DateBean.getMonthEngStr(get(Calendar.MONTH) + 1)
            tvYear?.text = get(Calendar.YEAR).toString()
        }
        viewPager2?.apply {
            offscreenPageLimit = 5
            setCurrentItem(monthCount, false)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val year = data[position][Calendar.YEAR]
                    val month = data[position][Calendar.MONTH] + 1
                    tvMonth?.text = DateBean.getMonthEngStr(month)
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
            val weightMeasureSpec = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
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