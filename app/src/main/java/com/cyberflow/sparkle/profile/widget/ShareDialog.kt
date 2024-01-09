package com.cyberflow.sparkle.profile.widget

import android.content.Context
import android.content.res.Configuration
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.cyberflow.base.model.IMConversationCache
import com.cyberflow.base.resources.R
import com.cyberflow.sparkle.DBComponent
import com.cyberflow.sparkle.databinding.ItemShareActionHorizontalBinding
import com.cyberflow.sparkle.databinding.ItemShareRecentHorizontalBinding
import com.cyberflow.sparkle.im.DBManager
import com.cyberflow.sparkle.widget.BaseDialog
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.utils.withMain
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.launch


class ShareDialog : BaseDialog {

//    private val TAG = "ShareDialogFragment"

    override fun setLayoutId(): Int {
        return com.cyberflow.sparkle.R.layout.dialog_share
    }

    override fun onStart() {
        super.onStart()
        dialog!!.setCanceledOnTouchOutside(false)
        mWindow?.apply {
            setGravity(Gravity.BOTTOM)
            setWindowAnimations(R.style.BottomDialog_Animation)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, mWidthAndHeight!![1] / 2)
            setFlags(   // 弹出小窗口的同时，点击Activity中未被覆盖的按钮等
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mWindow?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, mWidthAndHeight!![1] / 2)
    }

    override fun initImmersionBar() {
        super.initImmersionBar()
        ImmersionBar.with(this).navigationBarColor(R.color.white_2nd).init()
    }

    private var mCallback: Callback? = null

    interface Callback {
        fun onSelected(user: IMConversationCache?, type: Int)
    }

    companion object {
        const val TYPE_SHARE = 0
        const val TYPE_MORE = 1
        const val TYPE_COPY_LINK = 2
        const val TYPE_DOWNLOAD = 3
    }

    private var mContext: Context? = null

    private fun getActionIconAndName(type: Int): Pair<Int, String>? {
        return when (type) {
            TYPE_MORE -> Pair(
                R.drawable.share_ic_more, mContext!!.getString(R.string.more)
            )

            TYPE_COPY_LINK -> Pair(
                R.drawable.share_ic_copy_link,
                mContext!!.getString(com.cyberflow.sparkle.R.string.copy_link)
            )

            TYPE_DOWNLOAD -> Pair(
                R.drawable.share_ic_download,
                mContext!!.getString(com.cyberflow.sparkle.R.string.download)
            )

            else -> null
        }
    }

    constructor(context: Context, callback: Callback) {
        if (context == null || callback == null) {
            return
        }

        mContext = context
        mCallback = callback
    }

    var rv: RecyclerView? = null
    var rvAction: RecyclerView? = null
    var line: View? = null

    override fun initView() {
        mRootView?.apply {
            rv = findViewById<RecyclerView>(com.cyberflow.sparkle.R.id.rv)
            rvAction = findViewById<RecyclerView>(com.cyberflow.sparkle.R.id.rvAction)
            line = findViewById<RecyclerView>(com.cyberflow.sparkle.R.id.line_horizon)

            rv?.setup {
                addType<IMConversationCache>(com.cyberflow.sparkle.R.layout.item_share_recent_horizontal)
                addType<Int>(com.cyberflow.sparkle.R.layout.item_share_action_horizontal)
                onBind {
                    when (itemViewType) {
                        com.cyberflow.sparkle.R.layout.item_share_recent_horizontal -> {
                            val model = getModel<IMConversationCache>()
                            getBinding<ItemShareRecentHorizontalBinding>().apply {
                                DBComponent.loadAvatar(ivHead, model.avatar, model.gender)
                                tvName.text = model.nick
                                layImg.setOnClickListener {
                                    mCallback?.onSelected(model, ShareDialog.TYPE_SHARE)
                                }
                            }
                        }

                        com.cyberflow.sparkle.R.layout.item_share_action_horizontal -> {
                            val model = getModel<Int>()
                            getBinding<ItemShareActionHorizontalBinding>().apply {
                                handleAction(this, model)
                            }
                        }
                    }
                }
            }

            rvAction?.setup {
                addType<Int>(com.cyberflow.sparkle.R.layout.item_share_action_horizontal)
                onBind {
                    val model = getModel<Int>()
                    getBinding<ItemShareActionHorizontalBinding>().apply {
                        handleAction(this, model)
                    }
                }
            }
        }
    }

    override fun initData() {
        (mContext as? AppCompatActivity)?.apply {
            lifecycleScope.launch {
                val conversationCache =
                    DBManager.instance.db?.imConversationCacheDao()?.getAll().orEmpty()
                withMain {
                    val recentListData = arrayListOf<Any>()
                    if (conversationCache.isNotEmpty()) {
                        rv?.isVisible = true
                        line?.isVisible = true
                        recentListData.addAll(conversationCache.take(3))
                        recentListData.add(TYPE_MORE)
                        rv?.models = recentListData
                    }

                    val actionListData = arrayListOf(
                        TYPE_COPY_LINK,
                        TYPE_DOWNLOAD
                    )
                    rvAction?.models = actionListData
                }
            }
        }
    }

    private fun handleAction(binding: ItemShareActionHorizontalBinding, model: Int) {
        binding.apply {
            getActionIconAndName(model)?.also {
                btnAction.updateSrc(it.first)
                tvName.text = it.second
            }
            btnAction.setClickListener(object : ShadowImgButton.ShadowClickListener {
                override fun clicked() {
                    mCallback?.onSelected(null, model)
                }
            })
        }
    }

    fun show(activity: AppCompatActivity) {
        activity?.apply {
            if (dialog == null) {
                show(supportFragmentManager, "ShareDialogFragment")
            }
        }
    }

    fun hideOrShow() {
        activity?.apply {
            dialog?.also {
                if (it.isShowing) it.hide()
                else it.show()
            }
        }
    }

    fun justShow() {
        activity?.apply {
            dialog?.also {
                if (!it.isShowing) it.show()
            }
        }
    }

    fun justHide() {
        dialog?.apply {
            if (isShowing) hide()
        }
    }
}