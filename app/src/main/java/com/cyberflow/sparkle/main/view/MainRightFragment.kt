package com.cyberflow.sparkle.main.view

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.FragmentMainRightBinding
import com.cyberflow.sparkle.main.viewmodel.MainViewModel
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.layoutmanager.HoverGridLayoutManager
import com.drake.brv.listener.OnHoverAttachListener
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.setup
import com.drake.net.utils.TipUtils

class MainRightFragment : BaseDBFragment<BaseViewModel, FragmentMainRightBinding>() {

    override fun initData() {

    }

    private var actVm: MainViewModel? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        actVm = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun initView(savedInstanceState: Bundle?) {

        initListView()
    }

    private fun initListView() {
        val layoutManager = HoverGridLayoutManager(requireContext(), 6)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (mDatabind.rv.bindingAdapter.isHover(position)) {
                    6
                } else {
                    if (position == 0) return 6
                    if (position == 1) return 3
                    if (position == 2) return 3
                    else return 2
                }
            }
        }
        mDatabind.rv.layoutManager = layoutManager

        mDatabind.rv.divider {
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
                    R.layout.item_hover_header -> TipUtils.toast("悬停条目")
                    else -> TipUtils.toast("普通条目")
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

}