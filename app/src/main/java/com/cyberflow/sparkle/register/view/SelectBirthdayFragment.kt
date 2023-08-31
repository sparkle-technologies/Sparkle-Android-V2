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

    private fun selectDate() {
        Log.e("TAG", "selectDate: ")
        //todo select data widget
        mViewBind.etBirthDate.setText("2000-01-01")
    }

    private fun selectTime() {
        Log.e("TAG", "selectTime: ")
        mViewBind.etBirthTime.setText("00:00")
    }

}