package com.cyberflow.base.act

import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.viewbinding.ViewBinding
import com.cyberflow.base.ext.inflateBindingWithGeneric
import com.cyberflow.base.viewmodel.BaseViewModel

abstract class BaseVBAct<VM : BaseViewModel, VB : ViewBinding> : BaseVMAct<VM>() {

    override fun layoutID(): Int = 0

    lateinit var mViewBind: VB

    override fun initDataBind(): View? {
        mViewBind = inflateBindingWithGeneric(layoutInflater)
        return mViewBind.root
    }


    override fun onBackPressed() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm != null && window.attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (currentFocus != null) {
                imm.hideSoftInputFromWindow(
                    currentFocus!!.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
                super.onBackPressed()
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}