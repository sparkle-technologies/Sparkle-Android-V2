package com.cyberflow.sparkle.main.widget

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ItemHoroscopeSelectMonthBinding
import com.cyberflow.sparkle.main.widget.calendar.DateBean
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import java.util.Calendar

class SelectYearDialog {

    private var mContext: Context? = null
    private var mCallback: Callback? = null

    private var mDialog: Dialog? = null

    interface Callback {
        fun onSelected(select: DateBean?)
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
        mDialog?.setContentView(R.layout.dialog_select_month)

        val dm: DisplayMetrics = mContext!!.resources.displayMetrics
        val height = (dm.heightPixels.toFloat() * 450 / 812).toInt()
        mDialog?.setCancelable(true)
        mDialog?.setCanceledOnTouchOutside(true)
        val window = mDialog?.window
        if (window != null) {
            val lp = window.attributes
            lp.gravity = Gravity.BOTTOM
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = height
            window.attributes = lp
            window.setWindowAnimations(com.cyberflow.base.resources.R.style.BottomDialog_Animation)
        }

        mDialog?.apply {
            tvYear = findViewById(R.id.tv_year)
            btnPrevious = findViewById(R.id.btn_previous)
            btnNext = findViewById(R.id.btn_next)
            rv = findViewById(R.id.rv)
        }

        btnPrevious?.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                action(false)
            }
        })

        btnNext?.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                action(true)
            }
        })

        rv?.setup {
            singleMode = true
            addType<DateBean>(R.layout.item_horoscope_select_month)
            onChecked { position, checked, allChecked ->
//                Log.e("TAG", "onChecked:  position=$position  checked=$checked")
                val model = getModel<DateBean>(position)
                model.checked = checked
                model.notifyChange()
            }
            onBind {
                val model = getModel<DateBean>()
                getBinding<ItemHoroscopeSelectMonthBinding>().apply {
                    if (model.year == currentYear) {
                        ivSelected.setImageResource(com.cyberflow.base.resources.R.drawable.main_bg_horoscope_month_selected_yellow)
                    } else {
                        ivSelected.setImageDrawable(null)
                    }
                    if(model.year > currentYear){
                        tvData.setTextColor(ResourcesCompat.getColor(context.resources, com.cyberflow.base.resources.R.color.color_7D7D80, null))
                    }else{
                        tvData.setTextColor(Color.BLACK)
                    }
                    tvData.text = "${model.year}"
                }
            }
            R.id.item.onClick {
                setChecked(layoutPosition, true)
                mCallback?.onSelected(getModel<DateBean>(layoutPosition))
            }
        }
    }

    private var tvYear: TextView? = null
    private var btnPrevious: ShadowImgButton? = null
    private var btnNext: ShadowImgButton? = null
    private var rv: RecyclerView? = null

    private var selectYear = 0
    private var currentYear = 0

    private fun initData() {
        val calendar = Calendar.getInstance()
        currentYear = calendar[Calendar.YEAR]
        selectYear = 2030 - COUNT
        action(true)
    }

    private val COUNT = 12

    private fun action(next: Boolean) {
        if (next) {
            selectYear += COUNT
        } else {
            selectYear -= COUNT
        }
        tvYear?.text = "${selectYear - COUNT + 1} - $selectYear"
        getYearData(selectYear).apply {
            rv?.models = this
            this.forEachIndexed { index, dateBean ->
                if (dateBean.year == currentYear) {
                    rv?.bindingAdapter?.setChecked(index, true)
                }
            }
        }
    }

    private fun getYearData(year: Int): List<DateBean> {
//        Log.e("TAG", "getYearData:  year=$year", )
        val data = arrayListOf<DateBean>()
        repeat(COUNT){
//            Log.e("TAG", "getYearData:  ${year - COUNT + it + 1}   it=$it", )
            data.add(DateBean(year - COUNT + it + 1, 0))
        }
        return data
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