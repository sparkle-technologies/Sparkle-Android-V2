package com.cyberflow.sparkle.register.view

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.cyberflow.base.fragment.BaseVBFragment
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.FragmentSelectGenderBinding
import com.cyberflow.sparkle.login.viewmodel.LoginRegisterViewModel
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.google.android.material.snackbar.Snackbar

class SelectGenderFragment : BaseVBFragment<BaseViewModel, FragmentSelectGenderBinding>() {

    override fun initData() {

    }

    private var actVm: LoginRegisterViewModel? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        actVm = ViewModelProvider(requireActivity()).get(LoginRegisterViewModel::class.java)
    }

    override fun initView(savedInstanceState: Bundle?) {

        mViewBind.ivManBg.setOnClickListener { click(true, false) }

        mViewBind.ivWomenBg.setOnClickListener { click(false, true) }

        mViewBind.btnRegisterNext.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked(disable: Boolean) {
                if(disable){
                    Snackbar.make(mViewBind.btnRegisterNext, "please select gender", Snackbar.LENGTH_SHORT).show()
                    return
                }
                actVm?.registerBean?.gender = gender
                actVm?.clickNext()
            }
        })
    }

    private var gender = 0

    private fun click(clickMan: Boolean, clickWomen: Boolean) {
        if(clickMan && !clickWomen) gender = 1
        if(!clickMan && clickWomen) gender = 2
        if(gender == 1 || gender == 2){
            mViewBind.btnRegisterNext.disableBg(false)
        }

        mViewBind.ivManBg.setImageResource(if (clickMan) R.drawable.register_ic_gender_man_select else R.drawable.register_ic_gender_man_unselect)
        mViewBind.ivWomenBg.setImageResource(if (clickWomen) R.drawable.register_ic_gender_women_select else R.drawable.register_ic_gender_women_unselect)
    }
}