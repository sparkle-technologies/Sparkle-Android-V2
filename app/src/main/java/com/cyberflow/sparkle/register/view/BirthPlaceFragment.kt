package com.cyberflow.sparkle.register.view

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.cyberflow.base.fragment.BaseVBFragment
import com.cyberflow.base.model.BirthplaceInfo
import com.cyberflow.base.model.GENDER_MAN
import com.cyberflow.base.model.GENDER_WOMEN
import com.cyberflow.sparkle.databinding.FragmentRegisterBirthPlaceBinding
import com.cyberflow.sparkle.login.viewmodel.LoginRegisterViewModel
import com.cyberflow.sparkle.login.widget.ShadowImgButton
import com.cyberflow.sparkle.login.widget.ShadowTxtButton
import com.cyberflow.sparkle.register.widget.searchplace.SearchPlaceDialog

class BirthPlaceFragment : BaseVBFragment<LoginRegisterViewModel, FragmentRegisterBirthPlaceBinding>() {
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
            override fun clicked(disable: Boolean) {
                actVm?.clickPrevious()
            }
        })
        mViewBind.btnRegisterNext.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked(disable: Boolean) {
                actVm?.clickNext()
            }
        })
    }

    private fun selectPlace(){
        val searchPlaceDialog = SearchPlaceDialog(getString(com.cyberflow.base.resources.R.string.current))
        searchPlaceDialog.setCallBack(object : SearchPlaceDialog.ICallBack {
            override fun callback(placeStr: String?, latitude: String?, longitude: String?) {
                Log.e("TAG", "callback: placeStr=$placeStr latitude=$latitude longitude=$longitude" )
                placeStr?.also {
                    actVm?.registerBean?.apply {
                        if(birthplace_info == null ) birthplace_info = BirthplaceInfo()
                        birthplace_info?.location = placeStr
                        birthplace_info?.latitude = latitude ?: ""
                        birthplace_info?.longitude = longitude ?: ""
                    }
                    mViewBind.etBirthPlace.setText(placeStr)
                    mViewBind.btnRegisterNext.disableBg(false)
                }
            }
        })
        searchPlaceDialog.show(requireActivity().supportFragmentManager, "current")
    }
}