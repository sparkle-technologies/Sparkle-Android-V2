package com.cyberflow.sparkle

import android.os.Bundle
import android.widget.Toast
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.databinding.ActivityTmpBinding
import com.cyberflow.sparkle.login.widget.ShadowTxtButton

class TmpActivity : BaseDBAct<BaseViewModel, ActivityTmpBinding>() {


    override fun initView(savedInstanceState: Bundle?) {

        mDataBinding.btn.setOnClickListener {
            mDataBinding.outlinedTextField.also {
                it.error = "Opps！ Something’s wrong. Please change to another nickname. "
            }
        }

        mDataBinding.btnClear.setOnClickListener {
            mDataBinding.outlinedTextField.also {
                it.error = null
            }
        }

        mDataBinding.loginBtnShadow.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked(d: Boolean) {
                Toast.makeText(this@TmpActivity, "click me", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun initData() {

    }
}