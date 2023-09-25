package com.cyberflow.sparkle.main.view

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.dp2px
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ActivityMainBinding
import com.cyberflow.sparkle.login.widget.ShadowImgButton
import com.cyberflow.sparkle.main.viewmodel.MainViewModel
import com.cyberflow.sparkle.main.widget.DoubleClickListener
import com.cyberflow.sparkle.register.view.PageAdapter
import com.cyberflow.sparkle.setting.view.SettingsActivity
import com.google.android.material.snackbar.Snackbar

class MainActivity : BaseDBAct<MainViewModel, ActivityMainBinding>() {

    companion object {
        fun go(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var dis = 0f
    private var fromLeft2Right: ObjectAnimator? = null
    private var fromRight2Left: ObjectAnimator? = null
    private var lastLeft = false
    private var lastRight = false

    // the principle is : try to make logic clear, more code is acceptable
    private fun clickTopMenu(left: Boolean, right: Boolean, justTopAnima: Boolean = false) {
        if (left && right) return // avoid same click
        if (lastRight == right && lastLeft == left) return
        lastLeft = left
        lastRight = right

        if (left) {
            fromRight2Left?.start()
        }

        if (right) {
            fromLeft2Right?.start()
        }

        mDataBinding.ivMenuLeft.setImageResource((if (left) com.cyberflow.base.resources.R.drawable.svg_ic_horoscope_select else com.cyberflow.base.resources.R.drawable.svg_ic_horoscope_unselect))
        mDataBinding.ivMenuRight.setImageResource((if (right) com.cyberflow.base.resources.R.drawable.svg_ic_contact_select else com.cyberflow.base.resources.R.drawable.svg_ic_contact_unselect))
        if (justTopAnima) return
        if (left) goPrevious()
        if (right) goNext()
    }

    private val left: MainLeftFragment by lazy { MainLeftFragment() }
    private val right: MainRightFragment by lazy { MainRightFragment() }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.viewMenuLeft.setOnClickListener { clickTopMenu(true, false) }

        mDataBinding.viewMenuRight.setOnClickListener(object : DoubleClickListener() {
            override fun onDoubleClick() {
                right.refresh()
            }

            override fun onSingleClick() {
                clickTopMenu(false, true)
            }
        })

        mDataBinding.ivHead.setOnClickListener {
            Snackbar.make(mDataBinding.ivHead, "go setting", Snackbar.LENGTH_SHORT).show()
            SettingsActivity.go(this)
        }

        mDataBinding.btnAddFriends.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                mDataBinding.layDialogAdd.apply {
                    visibility = if (this.visibility == View.VISIBLE) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }
            }
        })

        mDataBinding.layDialogAdd.apply {
            findViewById<View>(R.id.lay_add_friends).setOnClickListener {
                Snackbar.make(mDataBinding.ivHead, "add friends", Snackbar.LENGTH_SHORT).show()
            }
            findViewById<View>(R.id.lay_contacts).setOnClickListener {
                Snackbar.make(mDataBinding.ivHead, "contacts", Snackbar.LENGTH_SHORT).show()
            }
        }

        var adapter = PageAdapter(supportFragmentManager, lifecycle)
        adapter.addFragment(left)
        adapter.addFragment(right)

        mDataBinding.pager.apply {
            offscreenPageLimit = 1
//            isUserInputEnabled = false
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            this.adapter = adapter
            registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (position == 0) clickTopMenu(true, false, true)
                    else clickTopMenu(false, true, true)
                }
            })
            setPageTransformer(MarginPageTransformer(100))
        }

        dis = dp2px(82f).toFloat()
        fromLeft2Right = ObjectAnimator.ofFloat(mDataBinding.bgMenuLeft, "translationX", 0f, dis)
        fromRight2Left = ObjectAnimator.ofFloat(mDataBinding.bgMenuLeft, "translationX", dis, 0f)

        clickTopMenu(true, false)
    }

    private fun goPrevious() {
        mDataBinding.pager.apply {
            if (currentItem > 0) {
                setCurrentItem(currentItem - 1, true)
            }
        }
    }

    private fun goNext() {
        mDataBinding.pager.apply {
            adapter?.also { a ->
                if (currentItem < a.itemCount - 1) {
                    setCurrentItem(currentItem + 1, true)
                }
            }
        }
    }

    override fun initData() {
        Glide.with(this)
            .load(R.drawable.avatar)
            .skipMemoryCache(true)
            .into(mDataBinding.ivHead)
    }
}