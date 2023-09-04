package com.cyberflow.sparkle.register.view

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.cyberflow.base.fragment.BaseVBFragment
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.databinding.FragmentSelectGenderBinding
import com.cyberflow.sparkle.login.viewmodel.LoginRegisterViewModel
import com.cyberflow.sparkle.login.widget.ShadowTxtButton

class SelectGenderFragment : BaseVBFragment<BaseViewModel, FragmentSelectGenderBinding>() {

    override fun initData() {

    }

    private var actVm: LoginRegisterViewModel? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        actVm = ViewModelProvider(requireActivity()).get(LoginRegisterViewModel::class.java)
    }

    override fun initView(savedInstanceState: Bundle?) {

        mViewBind.ivMan.setOnClickListener { click(true, false) }
        mViewBind.ivManBg.setOnClickListener { click(true, false) }

        mViewBind.ivWomen.setOnClickListener { click(false, true) }
        mViewBind.ivWomenBg.setOnClickListener { click(false, true) }

        mViewBind.btnRegisterNext.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked(disable: Boolean) {
                actVm?.registerBean?.gender = gender
                actVm?.clickNext()
            }
        })

        click(false, false)
    }

    private var gender = 0

    private fun click(clickMan: Boolean, clickWomen: Boolean) {
        if(clickMan && !clickWomen) gender = 1
        if(!clickMan && clickWomen) gender = 2
        if(gender == 1 || gender == 2){
            mViewBind.btnRegisterNext.disableBg(false)
        }

        mViewBind.ivManShadow.setImageResource(if (clickMan) com.cyberflow.base.resources.R.drawable.button_start_shadow else com.cyberflow.base.resources.R.drawable.register_bg_gender_white)
        mViewBind.ivManBg.setImageResource(if (clickMan) com.cyberflow.base.resources.R.drawable.register_bg_gender_man else com.cyberflow.base.resources.R.drawable.register_bg_gender_white)

        mViewBind.ivWomenShadow.setImageResource(if (clickWomen) com.cyberflow.base.resources.R.drawable.button_start_shadow else com.cyberflow.base.resources.R.drawable.register_bg_gender_white)
        mViewBind.ivWomenBg.setImageResource(if (clickWomen) com.cyberflow.base.resources.R.drawable.register_bg_gender_women else com.cyberflow.base.resources.R.drawable.register_bg_gender_white)
    }
}