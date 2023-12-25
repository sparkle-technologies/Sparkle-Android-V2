package com.cyberflow.sparkle.main.widget

import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ItemHoroscopeSelectMonthBinding
import com.cyberflow.sparkle.main.widget.calendar.DateBean
import com.cyberflow.sparkle.main.widget.calendar.getMonthEngStr
import com.cyberflow.sparkle.widget.NotificationDialog
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Calendar

class SelectMonthDialog {

    private var mContext: Context? = null
    private var mCallback: Callback? = null
    private var birthDate: DateBean? = null
    private var currentDate: DateBean? = null
    private var mDialog: BottomSheetDialog? = null

    interface Callback {
        fun onSelected(select: DateBean?)
    }

    constructor(context: Context, birth: DateBean?, current: DateBean?, callback: Callback) {
        if (context == null || callback == null) {
            return
        }

        mContext = context
        birthDate = birth
        currentDate = current
        mCallback = callback

        initView()
        initData()
    }

    private fun initView() {
        mDialog = BottomSheetDialog(mContext!!, com.cyberflow.base.resources.R.style.forward_dialog)
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
//                Log.e("TAG", "onChecked:  position=$position  checked=$checked" )
                val model = getModel<DateBean>(position)
                model.checked = checked
                model.notifyChange()
            }
            onBind {
                val model = getModel<DateBean>()
                getBinding<ItemHoroscopeSelectMonthBinding>().apply {
//                    Log.e("TAG", "onBind:  layoutPosition=$layoutPosition  model=${model.toString()}  currentYear=$currentYear  currentMonth=$currentMonth" )
                    if (model.year == currentYear && model.month == currentMonth) {
                        ivSelected.setImageResource(com.cyberflow.base.resources.R.drawable.main_bg_horoscope_month_selected_yellow)
                    }else{
                        ivSelected.setImageDrawable(null)
                    }

                    var min = 1900 * 12 + 1
                    birthDate?.let {
                        min = it.year * 12 +  it.month
                    }
                    val count = model.year * 12 + model.month
                    val max = 2100 * 12 + 1

                    if( count < min ||  count > max){
                        tvData.setTextColor(ResourcesCompat.getColor(context.resources, com.cyberflow.base.resources.R.color.color_7D7D80, null))
                    }else{
                        tvData.setTextColor(Color.BLACK)
                    }

                    tvData.text = getMonthEngStr(model.month)?.take(3)
                }
            }
            R.id.item.onClick {
                Log.e("TAG", "onClick:  position=$layoutPosition  checked=true" )
                val model = getModel<DateBean>(layoutPosition)
                var min = 1900 * 12 + 1
                birthDate?.let {
                    min = it.year * 12 +  it.month
                }
                val count = model.year * 12 + model.month
                var max = 2100 * 12 + 1
                currentDate?.let {
                    max = it.year * 12 + it.month + 1
                }
                if(count in min..max){
                    setChecked(layoutPosition, true)
                    itemView.postDelayed({
                        mCallback?.onSelected(getModel<DateBean>(layoutPosition))
                    }, 200)
                }
                if(count > max){
                    LiveDataBus.get().with(NotificationDialog.EVENT_SUCCESS).postValue(context.getString(R.string.future_horoscope_hint))
                }
            }
        }
    }

    private var tvYear: TextView? = null
    private var btnPrevious: ShadowImgButton? = null
    private var btnNext: ShadowImgButton? = null
    private var rv: RecyclerView? = null

    private var selectYear = 0
    private var currentYear = 0
    private var currentMonth = 0

    private fun initData() {
        val calendar = Calendar.getInstance()
        currentYear = calendar[Calendar.YEAR]
        currentMonth = calendar[Calendar.MONTH] + 1
        selectYear = currentYear - 1
        action(true)
    }

    private fun action(next: Boolean) {
        if (next) {
            if(selectYear >= 2100){
                btnNext?.disableBg(true)
                return
            }
            selectYear++
        } else {
            if(selectYear <= (birthDate?.year ?: 1900)){
                btnPrevious?.disableBg(true)
                return
            }
            selectYear--
        }

        btnPrevious?.disableBg(selectYear <= (birthDate?.year ?: 1900))
        btnNext?.disableBg(selectYear >= 2100)

        tvYear?.text = "$selectYear"
        getMonthData(selectYear).apply {
            rv?.models = this
            this.forEachIndexed { index, dateBean ->
                if (dateBean.year == currentYear && dateBean.month == currentMonth) {
                    rv?.bindingAdapter?.setChecked(index, true)
                }
            }
        }
    }

    private fun getMonthData(year: Int): List<DateBean> {
        val data = arrayListOf<DateBean>()
        for (i in 1 until  13) {
            val bean = DateBean(year, i)
            data.add(bean)
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