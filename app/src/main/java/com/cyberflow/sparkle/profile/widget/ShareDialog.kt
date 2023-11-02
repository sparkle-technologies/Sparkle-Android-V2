package com.cyberflow.sparkle.profile.widget

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.cyberflow.base.model.IMUserInfo
import com.cyberflow.base.resources.R
import com.cyberflow.sparkle.DBComponent
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
import com.cyberflow.sparkle.databinding.ItemShareActionHorizontalBinding
import com.cyberflow.sparkle.databinding.ItemShareRecentHorizontalBinding
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.drake.brv.utils.models
import com.drake.brv.utils.setup

class ShareDialog {

    private var mContext: Context? = null
    private var mCallback: Callback? = null

    private var mDialog: Dialog? = null


    interface Callback {
        fun onSelected(openUid: String, type: Int)
    }

    companion object {
        const val TYPE_SHARE = 0
        const val TYPE_MORE = 1
        const val TYPE_COPY_LINK = 2
        const val TYPE_DOWNLOAD = 3
    }

    private fun getActionIconAndName(type: Int): Pair<Int, String>? {
        return when(type){
            TYPE_MORE -> Pair(R.drawable.share_ic_more, "More")
            TYPE_COPY_LINK -> Pair(R.drawable.share_ic_copy_link, "Copy link")
            TYPE_DOWNLOAD -> Pair(R.drawable.share_ic_download, "Download")
            else -> null
        }
    }

    constructor(context: Context, callback: Callback) {
        if (context == null || callback == null) {
            return
        }

        mContext = context
        mCallback = callback

        initView()
        initData()
    }

    private fun initData() {
        val conversationCache = IMDataManager.instance.getConversationData()
        val recentListData = arrayListOf<Any>()
        if (conversationCache.isNotEmpty()) {
            rv?.isVisible = true
            line?.isVisible = true
            recentListData.addAll(conversationCache.take(3))
            recentListData.add(TYPE_MORE)
            rv?.models = recentListData
        }

        val actionListData = arrayListOf(TYPE_COPY_LINK, TYPE_DOWNLOAD )
        rvAction?.models = actionListData
    }


    var rv: RecyclerView? = null
    var rvAction: RecyclerView? = null
    var line: View? = null

    private fun initView() {
        mDialog = Dialog(mContext!!, R.style.share_dialog)
        mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog?.setContentView(com.cyberflow.sparkle.R.layout.dialog_share)
//        mDialog?.setCancelable(false)
        mDialog?.setCanceledOnTouchOutside(false)
        val window = mDialog?.window
        if (window != null) {
            val lp = window.attributes
            lp.gravity = Gravity.BOTTOM
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = lp
            window.setWindowAnimations(R.style.BottomDialog_Animation)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
        }

        mDialog?.apply {

            rv = findViewById<RecyclerView>(com.cyberflow.sparkle.R.id.rv)
            rvAction = findViewById<RecyclerView>(com.cyberflow.sparkle.R.id.rvAction)
            line = findViewById<RecyclerView>(com.cyberflow.sparkle.R.id.line_horizon)

            rv?.setup {
                addType<IMUserInfo>(com.cyberflow.sparkle.R.layout.item_share_recent_horizontal)
                addType<Int>(com.cyberflow.sparkle.R.layout.item_share_action_horizontal)
                onBind {
                    when (itemViewType) {
                        com.cyberflow.sparkle.R.layout.item_share_recent_horizontal->{
                            val model = getModel<IMUserInfo>()
                            getBinding<ItemShareRecentHorizontalBinding>().apply {
                                DBComponent.loadAvatar(ivHead, model.avatar, model.gender)
                                tvName.text = model.nick
                                item.setOnClickListener {
                                    mCallback?.onSelected(model.open_uid, TYPE_SHARE)
                                }
                            }
                        }
                        com.cyberflow.sparkle.R.layout.item_share_action_horizontal->{
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

    private fun handleAction(binding: ItemShareActionHorizontalBinding, model: Int) {
        binding.apply {
            getActionIconAndName(model)?.also {
                btnAction.updateSrc(it.first)
                tvName.text = it.second
            }
            btnAction.setClickListener(object : ShadowImgButton.ShadowClickListener{
                override fun clicked() {
                    mCallback?.onSelected("", model)
                }
            })
        }
    }

    fun show() {
        mDialog!!.show()
    }

    fun onDestroy() {
        if (mDialog != null) {
            mDialog!!.dismiss()
            mDialog = null
        }
    }
}