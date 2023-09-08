package com.cyberflow.sparkle.main.view

import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : BaseDBAct<BaseViewModel, ActivityMainBinding>() {

    private fun clickTopMenu(left: Boolean, right: Boolean){

        mDataBinding.bgMenuLeft.background =if(left)  ResourcesCompat.getDrawable(resources, com.cyberflow.base.resources.R.drawable.main_bg_top_menu_selected, null) else null
        mDataBinding.bgMenuRight.background =if(right)  ResourcesCompat.getDrawable(resources, com.cyberflow.base.resources.R.drawable.main_bg_top_menu_selected, null) else null
        mDataBinding.ivMenuLeft.setImageResource((if(left) com.cyberflow.base.resources.R.drawable.svg_ic_horoscope_select else com.cyberflow.base.resources.R.drawable.svg_ic_horoscope_unselect))
        mDataBinding.ivMenuRight.setImageResource((if(right) com.cyberflow.base.resources.R.drawable.svg_ic_contact_select else com.cyberflow.base.resources.R.drawable.svg_ic_contact_unselect))

    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.bgMenuLeft.setOnClickListener {  clickTopMenu(true, false) }
        mDataBinding.bgMenuRight.setOnClickListener {  clickTopMenu(false, true) }
        mDataBinding.ivHead.setOnClickListener { Snackbar.make(mDataBinding.ivHead, "click me", Snackbar.LENGTH_SHORT).show() }
        mDataBinding.btnAddFriends.setOnClickListener { Snackbar.make(mDataBinding.ivHead, "add friends", Snackbar.LENGTH_SHORT).show() }

        clickTopMenu(true, false)


    }

    override fun initData() {

    }
}