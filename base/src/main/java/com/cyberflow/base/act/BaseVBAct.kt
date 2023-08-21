package com.cyberflow.base.act

import android.view.View
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
}