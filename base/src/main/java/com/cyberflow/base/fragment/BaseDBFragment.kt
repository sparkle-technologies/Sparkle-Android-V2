package com.cyberflow.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.cyberflow.base.ext.inflateBindingWithGeneric
import com.cyberflow.base.viewmodel.BaseViewModel

abstract class BaseDBFragment<VM : BaseViewModel, DB : ViewDataBinding> : BaseVMFragment<VM>() {

    override fun layoutID(): Int = 0

    private var _binding: DB? = null
    val mDatabind: DB get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBindingWithGeneric(inflater, container, false)
        return mDatabind.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}