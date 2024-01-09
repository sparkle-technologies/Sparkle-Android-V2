package com.cyberflow.sparkle.main.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ItemHoroscopeSelectMonthBinding
import com.cyberflow.sparkle.main.widget.calendar.DateBean
import com.cyberflow.sparkle.widget.BaseDialog
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.gyf.immersionbar.ImmersionBar
import java.util.Calendar

class SelectYearDialog: BaseDialog {
    override fun onStart() {
        super.onStart()
        dialog!!.setCanceledOnTouchOutside(true)

        val dm: DisplayMetrics = mContext!!.resources.displayMetrics
        val width = (dm.heightPixels.toFloat() * 450 / 812).toInt()
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


    override fun setLayoutId() = R.layout.dialog_select_month

    private var mContext: Context? = null
    private var mCallback: Callback? = null
    private var birthDate: DateBean? = null
    private var currentDate: DateBean? = null

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

    }


    private var root: View? = null
    private var tvYear: TextView? = null
    private var btnPrevious: ShadowImgButton? = null
    private var btnNext: ShadowImgButton? = null
    private var rv: RecyclerView? = null

    override fun initView() {
        mRootView?.apply {
            root = findViewById(R.id.root)
            tvYear = findViewById(R.id.tv_year)
            btnPrevious = findViewById(R.id.btn_previous)
            btnNext = findViewById(R.id.btn_next)
            rv = findViewById(R.id.rv)

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

                        if(model.year < (birthDate?.year ?: 1900) || model.year > 2100){
                            tvData.setTextColor(ResourcesCompat.getColor(context.resources, com.cyberflow.base.resources.R.color.color_7D7D80, null))
                        }else{
                            tvData.setTextColor(Color.BLACK)
                        }
                        tvData.text = "${model.year}"
                    }
                }
                R.id.item.onClick {
                    val model = getModel<DateBean>(layoutPosition)
                    if(model.year >= (birthDate?.year ?: 1900) && model.year <= 2100){
                        setChecked(layoutPosition, true)
                        itemView.postDelayed({
//                        mCallback?.onSelected(getModel<DateBean>(layoutPosition))
                        }, 200)
                    }
                }
            }
        }
    }


    private var selectYear = 0
    private var currentYear = 0
    private val COUNT = 12

    override fun initData() {
        val calendar = Calendar.getInstance()
        currentYear = calendar[Calendar.YEAR]
        selectYear = (currentYear + 7) - COUNT
        action(true)
    }

    private fun action(next: Boolean) {

        Log.e("TAG", "action: selectYear=$selectYear   birthDate=$birthDate" )  // 2018
        if (next) {
            if(selectYear >= 2100 ){
                btnNext?.disableBg(true)
                return
            }
            selectYear += COUNT
        } else {
            if(selectYear - COUNT <= (birthDate?.year ?: 1900)){
                btnPrevious?.disableBg(true)
                return
            }
            selectYear -= COUNT
        }

        btnPrevious?.disableBg((selectYear - COUNT + 1) <= (birthDate?.year ?: 1900))
        btnNext?.disableBg(selectYear >= 2100)

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
//        Log.e("TAG", "getYearData:  year=$year", )  // 2030
        val data = arrayListOf<DateBean>()
        repeat(COUNT){
//            Log.e("TAG", "getYearData:  ${year - COUNT + it + 1}   it=$it", )
            // 2019  it=0    ...    2020   it=1
            data.add(DateBean(year - COUNT + it + 1, 1))
        }
        return data
    }

    fun show(activity: FragmentActivity) {
        activity?.apply {
            if (dialog == null) {
                show(supportFragmentManager, "MonthSelectDialog")
            }
        }
    }

}