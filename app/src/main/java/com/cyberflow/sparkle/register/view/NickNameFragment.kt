package com.cyberflow.sparkle.register.view

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.cyberflow.base.fragment.BaseVBFragment
import com.cyberflow.base.model.GENDER_MAN
import com.cyberflow.base.model.GENDER_WOMEN
import com.cyberflow.base.model.LoginResponseData
import com.cyberflow.base.net.Api
import com.cyberflow.base.net.GsonConverter
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.sparkle.databinding.FragmentRegisterNicknameBinding
import com.cyberflow.sparkle.login.view.LoginAct
import com.cyberflow.sparkle.login.viewmodel.LoginRegisterViewModel
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.drake.net.Post
import com.drake.net.utils.scopeDialog

class NickNameFragment :
    BaseVBFragment<LoginRegisterViewModel, FragmentRegisterNicknameBinding>() {
    override fun initData() {
        actVm?.registerBean?.gender?.apply {
            if (this == GENDER_MAN) {
                mViewBind.btnHead.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_man_divider)
            }
            if (this == GENDER_WOMEN) {
                mViewBind.btnHead.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_women_divider)
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
        /*mViewBind.btnHead.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                mViewBind.outlinedTextField.also {
                    it.error = if(error) "Opps！ Something’s wrong. Please change to another nickname. " else null
                }
                error = !error
            }
        })*/

        mViewBind.etNiceName.addTextChangedListener {
            if (it.isNullOrEmpty()) {
                mViewBind.btnRegisterNext.disableBg(true)
            } else {
                mViewBind.btnRegisterNext.disableBg(false)
            }
        }

        mViewBind.btnRegisterPrevious.setClickListener(object :
            ShadowTxtButton.ShadowClickListener {
            override fun clicked(disable: Boolean) {
                actVm?.clickPrevious()
            }
        })
        mViewBind.btnRegisterNext.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked(disable: Boolean) {
//                actVm?.clickNext()
                val txt = mViewBind.etNiceName.text.toString().trim()
                val pass = txt.isNotEmpty()
                mViewBind.outlinedTextField.also {
                    it.error =
                        if (pass) null else "Opps！ Something’s wrong. Please change to another nickname. "
                }
                if (pass) {
                    submitRegister()
                }
            }
        })
    }


    private fun submitRegister() {
        val txt = mViewBind.etNiceName.text.toString().trim()
        actVm?.apply {
            registerBean?.nick = txt
            scopeDialog {
                val data = Post<LoginResponseData>(Api.COMPLETE_INFO) {
                    json(GsonConverter.gson.toJson(registerBean))
                }.await()
                data?.let {
                    CacheUtil.setUserInfo(it)

                    val token = CacheUtil.getUserInfo()?.token.orEmpty()
                    Log.e("NickNameFragment", "got new token from login :  $token")
                    LoginAct.imLogin(requireActivity())
                    requireActivity().finish()
                }
            }
        }
    }
}