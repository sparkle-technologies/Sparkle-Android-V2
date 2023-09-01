package com.cyberflow.sparkle.register.view

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.cyberflow.base.fragment.BaseVBFragment
import com.cyberflow.sparkle.databinding.FragmentRegisterNicknameBinding
import com.cyberflow.sparkle.login.viewmodel.LoginRegisterViewModel
import com.cyberflow.sparkle.login.widget.ShadowImgButton
import com.cyberflow.sparkle.login.widget.ShadowTxtButton

class NickNameFragment :
    BaseVBFragment<LoginRegisterViewModel, FragmentRegisterNicknameBinding>() {
    override fun initData() {

    }

    private var actVm: LoginRegisterViewModel? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        actVm = ViewModelProvider(requireActivity()).get(LoginRegisterViewModel::class.java)
    }

    var error = true
    override fun initView(savedInstanceState: Bundle?) {
        mViewBind.btnHead.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                mViewBind.outlinedTextField.also {
                    it.error = if(error) "Opps！ Something’s wrong. Please change to another nickname. " else null
                }
                error = !error
            }
        })

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

    // TODO 这里逻辑不确定  待产品确认
    private fun submitRegister(){
        val txt = mViewBind.etNiceName.text.toString().trim()

        if(txt.length in 1..30){

        }else{

        }
    }
}