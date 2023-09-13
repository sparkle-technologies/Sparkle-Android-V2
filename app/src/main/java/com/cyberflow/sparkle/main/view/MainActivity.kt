package com.cyberflow.sparkle.main.view

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.dp2px
import com.cyberflow.sparkle.databinding.ActivityMainBinding
import com.cyberflow.sparkle.main.viewmodel.MainViewModel
import com.cyberflow.sparkle.register.view.PageAdapter
import com.google.android.material.snackbar.Snackbar

class MainActivity : BaseDBAct<MainViewModel, ActivityMainBinding>() {

    private var dis = 0f
    private var fromLeft2Right : ObjectAnimator? = null
    private var fromRight2Left : ObjectAnimator? = null
    private var lastLeft = false
    private var lastRight = false

    private fun clickTopMenu(left: Boolean, right: Boolean) {
        if(left && right) return // avoid same click
        if(lastRight == right && lastLeft == left) return
        lastLeft = left
        lastRight = right

        if(left){
            fromRight2Left?.start()
        }

        if(right){
            fromLeft2Right?.start()
        }

        mDataBinding.ivMenuLeft.setImageResource((if (left) com.cyberflow.base.resources.R.drawable.svg_ic_horoscope_select else com.cyberflow.base.resources.R.drawable.svg_ic_horoscope_unselect))
        mDataBinding.ivMenuRight.setImageResource((if (right) com.cyberflow.base.resources.R.drawable.svg_ic_contact_select else com.cyberflow.base.resources.R.drawable.svg_ic_contact_unselect))
        if(left) goPrevious()
        if(right)  goNext()
    }



    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.viewMenuLeft.setOnClickListener { clickTopMenu(true, false) }
        mDataBinding.viewMenuRight.setOnClickListener { clickTopMenu(false, true) }
        mDataBinding.ivHead.setOnClickListener {
            Snackbar.make(mDataBinding.ivHead, "click me", Snackbar.LENGTH_SHORT).show()
        }
        mDataBinding.btnAddFriends.setOnClickListener {
            Snackbar.make(mDataBinding.ivHead, "add friends", Snackbar.LENGTH_SHORT).show()
        }

        var adapter = PageAdapter(supportFragmentManager, lifecycle)
        adapter.addFragment(MainLeftFragment())
        adapter.addFragment(MainRightFragment())

        mDataBinding.pager.apply {
            offscreenPageLimit = 1
            isUserInputEnabled = false
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            this.adapter = adapter
        }

        dis = dp2px(82f).toFloat()
        fromLeft2Right = ObjectAnimator.ofFloat(mDataBinding.bgMenuLeft, "translationX", 0f, dis)
        fromRight2Left = ObjectAnimator.ofFloat(mDataBinding.bgMenuLeft, "translationX", dis, 0f)

        clickTopMenu(true, false)
    }

    private fun goPrevious(){
        Log.e(TAG, "goPrevious: ", )
        mDataBinding.pager.apply {
            if(currentItem > 0){
                setCurrentItem(currentItem - 1, true)
            }
        }
    }

    private fun goNext(){
        Log.e(TAG, "goNext: ", )
        mDataBinding.pager.apply {
            adapter?.also { a->
                if(currentItem < a.itemCount - 1){
                    setCurrentItem(currentItem + 1, true)
                }
            }
        }
    }

    override fun initData() {

    }
}