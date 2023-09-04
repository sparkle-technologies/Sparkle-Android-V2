package com.cyberflow.sparkle.register.view

import android.app.Activity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.cyberflow.base.fragment.BaseVBFragment
import com.cyberflow.base.model.GENDER_MAN
import com.cyberflow.base.model.GENDER_WOMEN
import com.cyberflow.sparkle.databinding.FragmentRegisterNicknameBinding
import com.cyberflow.sparkle.login.viewmodel.LoginRegisterViewModel
import com.cyberflow.sparkle.login.widget.ShadowImgButton
import com.cyberflow.sparkle.login.widget.ShadowTxtButton

class NickNameFragment :
    BaseVBFragment<LoginRegisterViewModel, FragmentRegisterNicknameBinding>() {
    override fun initData() {
        actVm?.registerBean?.gender?.apply {
            if(this == GENDER_MAN){
                mViewBind.btnHead.updateSrc(com.cyberflow.base.resources.R.drawable.register_ic_man)
            }
            if(this == GENDER_WOMEN){
                mViewBind.btnHead.updateSrc(com.cyberflow.base.resources.R.drawable.register_ic_women)
            }
        }
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
        mViewBind.etNiceName.addTextChangedListener {
            if(it.isNullOrEmpty()){
                mViewBind.btnRegisterNext.disableBg(true)
            }else{
                mViewBind.btnRegisterNext.disableBg(false)
            }
        }

        mViewBind.btnRegisterPrevious.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked(disable: Boolean) {
                actVm?.clickPrevious()
            }
        })
        mViewBind.btnRegisterNext.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked(disable: Boolean) {
//                actVm?.clickNext()
                val txt = mViewBind.etNiceName.text.toString().trim()
                val pass  = txt.isNotEmpty()
                mViewBind.outlinedTextField.also {
                    it.error = if(pass) null else "Opps！ Something’s wrong. Please change to another nickname. "
                }
                if(pass){
                    submitRegister()
                }
            }
        })
    }

    // 只能输入表情符和号字母空格，汉字
    // const reg = /^(\uD83C[\uDF00-\uDFFF]|\uD83D[\uDC00-\uDE4F]|[a-zA-Z\s]|[\u4e00-\u9fa5])+$/
    // const reg = /^([\uD83C|\uD83D|\uD83E][\uDC00-\uDFFF]|[0-9a-zA-Z\s]|[\u4e00-\u9fa5])+$/
    private fun submitRegister(){
        val txt = mViewBind.etNiceName.text.toString().trim()
        actVm?.apply {
            registerBean?.nick = txt
            register()
        }
    }
}