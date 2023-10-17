package com.cyberflow.sparkle.register.view

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.cyberflow.base.fragment.BaseVBFragment
import com.cyberflow.base.model.GENDER_MAN
import com.cyberflow.base.model.GENDER_WOMEN
import com.cyberflow.sparkle.databinding.FragmentSelectBirthdayBinding
import com.cyberflow.sparkle.login.viewmodel.LoginRegisterViewModel
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.cyberflow.sparkle.register.widget.daytimepicker.CustomDatePicker
import com.cyberflow.sparkle.register.widget.daytimepicker.DateFormatUtils
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SelectBirthdayFragment :
    BaseVBFragment<LoginRegisterViewModel, FragmentSelectBirthdayBinding>() {
    override fun initData() {
        actVm?.registerBean?.gender?.apply {
            if(this == GENDER_MAN){
                mViewBind.btnHead.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_man_divider)
            }
            if(this == GENDER_WOMEN){
                mViewBind.btnHead.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_women_divider)
            }
        }
    }

    private var actVm: LoginRegisterViewModel? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        actVm = ViewModelProvider(requireActivity()).get(LoginRegisterViewModel::class.java)
    }

    override fun initView(savedInstanceState: Bundle?) {
       /* mViewBind.btnHead.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                // yyyy-MM-dd
                mViewBind.etBirthDate.setText("")
                mViewBind.etBirthTime.setText("")
            }
        })*/

        mViewBind.anchorDate.setOnClickListener { selectDate() }
        mViewBind.anchorTime.setOnClickListener { selectTime() }

        mViewBind.btnRegisterPrevious.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked(disable: Boolean) {
                actVm?.clickPrevious()
            }
        })

        mViewBind.btnRegisterNext.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked(disable: Boolean) {
                 if(selectDate.isNullOrEmpty()){
                    Snackbar.make(mViewBind.btnRegisterNext, "please select birth date", Snackbar.LENGTH_SHORT).show()
                    return
                }
                if(selectTime.isNullOrEmpty()){
                    Snackbar.make(mViewBind.btnRegisterNext, "please select birth time", Snackbar.LENGTH_SHORT).show()
                    return
               }
                if(disable) return
                actVm?.apply {
                    registerBean?.birthdate = selectDate
                    registerBean?.birth_time = selectTime
                    clickNext()
                }
            }
        })
    }

    private fun updateBtnNextStatus(){

        mViewBind.btnRegisterNext.disableBg(selectDate.isNullOrEmpty() || selectTime.isNullOrEmpty())

    }

    private var mDatePicker: CustomDatePicker? = null
    private var selectDate: String = ""
    private var selectTime: String = ""


    private fun selectDate() {
        Log.e("TAG", "selectDate: ")
        initDatePicker()
        // yyyy-MM-dd
        if (selectDate.isNotEmpty()) {
            mDatePicker?.show(selectDate)
        } else {
            // DateFormatUtils.long2Str(System.currentTimeMillis(), false)
            mDatePicker?.show("2000-01-01")
        }
    }

    private fun selectTime() {
        Log.e("TAG", "selectTime: ")
        initTimePicker()
        mDatePicker?.show("2023-11-11 ${selectTime.ifEmpty { "12:00:00" }}")
    }

    private fun initDatePicker() {
        val beginTimestamp: Long = DateFormatUtils.str2Long("1970-02-01", false)
        val endTimestamp = System.currentTimeMillis()

        // 通过时间戳初始化日期，毫秒级别
        mDatePicker = CustomDatePicker(
            context, { timestamp ->
                selectDate = DateFormatUtils.long2Str(timestamp, false)
                mViewBind.etBirthDate.setText(selectDate)
                updateBtnNextStatus()
            },
            beginTimestamp,
            endTimestamp
        ).apply {
            setTitle("Set Date");
            setCancelable(true)
            setCanShowPreciseTime(false)
            setScrollLoop(false)
            setCanShowAnim(false)
        }
    }

    private fun initTimePicker() {
        val beginTime = "2023-11-11 00:00"
        val endTime = "2023-11-11 23:59"

        // 通过时间戳初始化日期，毫秒级别
        mDatePicker =
            CustomDatePicker(
                context,
                { timestamp ->
                    val date = Date(timestamp)
                    selectTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)
                    mViewBind.etBirthTime.setText(
                        SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                        ).format(date)
                    )
                    updateBtnNextStatus()
                },
                beginTime,
                endTime
            ).apply {
                setTitle("Set Time")
                setCancelable(true)
                setCanShowPreciseDate(false)
                setCanShowPreciseTime(true)
                setScrollLoop(false)
                setCanShowAnim(false)
            }
    }
}