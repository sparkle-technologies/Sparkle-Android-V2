package com.cyberflow.sparkle.register.view

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.cyberflow.base.fragment.BaseVBFragment
import com.cyberflow.sparkle.databinding.FragmentSelectBirthdayBinding
import com.cyberflow.sparkle.login.viewmodel.LoginRegisterViewModel
import com.cyberflow.sparkle.login.widget.ShadowImgButton
import com.cyberflow.sparkle.login.widget.ShadowTxtButton
import com.cyberflow.sparkle.register.widget.daytimepicker.CustomDatePicker
import com.cyberflow.sparkle.register.widget.daytimepicker.DateFormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SelectBirthdayFragment :
    BaseVBFragment<LoginRegisterViewModel, FragmentSelectBirthdayBinding>() {
    override fun initData() {

    }

    private var actVm: LoginRegisterViewModel? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        actVm = ViewModelProvider(requireActivity()).get(LoginRegisterViewModel::class.java)
    }

    override fun initView(savedInstanceState: Bundle?) {

        mViewBind.btnHead.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                // yyyy-MM-dd
                mViewBind.etBirthDate.setText("")
                mViewBind.etBirthTime.setText("")
            }
        })

        mViewBind.anchorDate.setOnClickListener { selectDate() }
        mViewBind.anchorTime.setOnClickListener { selectTime() }

        mViewBind.btnRegisterNext.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked() {
                actVm?.clickNext()
            }
        })
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
            mDatePicker?.show(DateFormatUtils.long2Str(System.currentTimeMillis(), false))
        }
    }

    private fun selectTime() {
        Log.e("TAG", "selectTime: ")
        initTimePicker()
        mDatePicker?.show("2023-11-11 ${selectTime.ifEmpty { "00:00:00" }}")
    }

    private fun initDatePicker() {
        val beginTimestamp: Long = DateFormatUtils.str2Long("1970-02-01", false)
        val endTimestamp = System.currentTimeMillis()

        // 通过时间戳初始化日期，毫秒级别
        mDatePicker = CustomDatePicker(
            context, { timestamp ->
                selectDate = DateFormatUtils.long2Str(timestamp, false)
                mViewBind.etBirthDate.setText(selectDate)
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