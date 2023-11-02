package com.cyberflow.base.act

import android.view.View
import androidx.databinding.ViewDataBinding
import com.cyberflow.base.ext.inflateBindingWithGeneric
import com.cyberflow.base.viewmodel.BaseViewModel

abstract class BaseDBAct<VM : BaseViewModel, DB : ViewDataBinding> : BaseVMAct<VM>() {

    override fun layoutID(): Int = 0

    lateinit var mDataBinding: DB

    override fun initDataBind(): View? {
        mDataBinding = inflateBindingWithGeneric(layoutInflater)
//        mDataBinding.setVariable(BR._all, viewModel)  这样不太好  需要根据ID来双向绑定
        return mDataBinding.root
    }
}