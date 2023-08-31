package com.cyberflow.sparkle.register.view

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.cyberflow.base.fragment.BaseVBFragment
import com.cyberflow.sparkle.databinding.FragmentRegisterBirthPlaceBinding
import com.cyberflow.sparkle.login.viewmodel.LoginRegisterViewModel
import com.cyberflow.sparkle.login.widget.ShadowImgButton
import com.cyberflow.sparkle.login.widget.ShadowTxtButton

class BirthPlaceFragment :
    BaseVBFragment<LoginRegisterViewModel, FragmentRegisterBirthPlaceBinding>() {
    override fun initData() {

    }

    private var actVm: LoginRegisterViewModel? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        actVm = ViewModelProvider(requireActivity()).get(LoginRegisterViewModel::class.java)
    }

    override fun initView(savedInstanceState: Bundle?) {

        mViewBind.tvLater.setOnClickListener {
            actVm?.clickNext()
        }

        mViewBind.btnHead.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {

            }
        })

        mViewBind.anchor.setOnClickListener {
            selectPlace()
        }

        mViewBind.btnRegisterPrevious.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked() {
                actVm?.clickPrevious()
            }
        })
        mViewBind.btnRegisterNext.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked() {
                actVm?.clickNext()
            }
        })
    }

    private fun selectPlace(){

    }
}