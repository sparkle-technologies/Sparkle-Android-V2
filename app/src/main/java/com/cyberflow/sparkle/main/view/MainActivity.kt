package com.cyberflow.sparkle.main.view

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.databinding.ActivityMainBinding
import com.drake.brv.layoutmanager.HoverGridLayoutManager
import com.drake.brv.listener.OnHoverAttachListener
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.setup
import com.google.android.material.snackbar.Snackbar
import com.cyberflow.sparkle.R
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.net.utils.TipUtils.toast

class MainActivity : BaseDBAct<BaseViewModel, ActivityMainBinding>() {

    private fun clickTopMenu(left: Boolean, right: Boolean) {
        mDataBinding.bgMenuLeft.background = if (left) ResourcesCompat.getDrawable(resources, com.cyberflow.base.resources.R.drawable.main_bg_top_menu_selected, null) else null
        mDataBinding.bgMenuRight.background = if (right) ResourcesCompat.getDrawable(resources, com.cyberflow.base.resources.R.drawable.main_bg_top_menu_selected, null) else null
        mDataBinding.ivMenuLeft.setImageResource((if (left) com.cyberflow.base.resources.R.drawable.svg_ic_horoscope_select else com.cyberflow.base.resources.R.drawable.svg_ic_horoscope_unselect))
        mDataBinding.ivMenuRight.setImageResource((if (right) com.cyberflow.base.resources.R.drawable.svg_ic_contact_select else com.cyberflow.base.resources.R.drawable.svg_ic_contact_unselect))
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.bgMenuLeft.setOnClickListener { clickTopMenu(true, false) }
        mDataBinding.bgMenuRight.setOnClickListener { clickTopMenu(false, true) }
        mDataBinding.ivHead.setOnClickListener {
            Snackbar.make(mDataBinding.ivHead, "click me", Snackbar.LENGTH_SHORT).show()
        }
        mDataBinding.btnAddFriends.setOnClickListener {
            Snackbar.make(mDataBinding.ivHead, "add friends", Snackbar.LENGTH_SHORT).show()
        }

        clickTopMenu(true, false)

        initListView()
    }

    private fun initListView() {
        val layoutManager = HoverGridLayoutManager(this, 6)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (mDataBinding.rv.bindingAdapter.isHover(position)) {
                    6
                } else {
                    if (position == 0) return 6
                    if (position == 1) return 3
                    if (position == 2) return 3
                    else return 2
                }
            }
        }
        mDataBinding.rv.layoutManager = layoutManager

        mDataBinding.rv.divider {
            orientation = DividerOrientation.VERTICAL
            setDivider(10, true)
            onEnabled {
                itemViewType == R.layout.item_official || itemViewType == R.layout.item_contact
            }
        }.divider {
            orientation = DividerOrientation.HORIZONTAL
            setDivider(20, true)
            onEnabled {
                itemViewType == R.layout.item_contact
            }
        }.setup {
            addType<OfficialModel>(R.layout.item_official)
            addType<ContactModel>(R.layout.item_contact)
            addType<HoverHeaderModel>(R.layout.item_hover_header)

            // 点击事件
            onClick(R.id.item) {
                when (itemViewType) {
                    R.layout.item_hover_header -> toast("悬停条目")
                    else -> toast("普通条目")
                }
            }

            // 可选项, 粘性监听器
            onHoverAttachListener = object : OnHoverAttachListener {
                override fun attachHover(v: View) {
                    ViewCompat.setElevation(v, 10F) // 悬停时显示阴影
                }

                override fun detachHover(v: View) {
                    ViewCompat.setElevation(v, 0F) // 非悬停时隐藏阴影
                }
            }
        }.models = getData()

    }

    private fun getData(): List<Any> {
        return listOf(
            HoverHeaderModel(title = "Official", itemHover = false),
            OfficialModel(),
            OfficialModel(),
            HoverHeaderModel(title = "Contacts", itemHover = true),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel(),
            ContactModel()
        )
    }

    override fun initData() {

    }
}