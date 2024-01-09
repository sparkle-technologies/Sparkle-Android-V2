package com.cyberflow.sparkle.widget

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.gyf.immersionbar.ImmersionBar

abstract class BaseDialog : DialogFragment() {

//    private val TAG = "BaseDialog"

    protected var mActivity: Activity? = null
    protected var mRootView: View? = null
    protected var mWindow: Window? = null
    protected var isNormal = true
    var mWidthAndHeight: Array<Int>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as Activity?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(isNormal){
            setStyle(STYLE_NORMAL, R.style.NormalDialog)
        }else{
            setStyle(STYLE_NORMAL, R.style.MyDialog)
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        mWindow = dialog!!.window
        mWidthAndHeight = ImmerUtils.getWidthAndHeight(mWindow)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mRootView = inflater.inflate(setLayoutId(), container, false)
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isImmersionBarEnabled()) {
            initImmersionBar()
        }
        initView()
        initData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mActivity?.let { ImmerUtils.hideSoftKeyBoard(it) }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mWidthAndHeight = ImmerUtils.getWidthAndHeight(mWindow)
    }

    open fun isImmersionBarEnabled(): Boolean {
        return true
    }

    open fun initImmersionBar() {
        ImmersionBar.with(this).init()
    }

    abstract fun setLayoutId(): Int
    abstract fun initView()
    abstract fun initData()

}