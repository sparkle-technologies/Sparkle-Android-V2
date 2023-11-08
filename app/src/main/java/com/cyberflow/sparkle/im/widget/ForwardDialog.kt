package com.cyberflow.sparkle.im.widget

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cyberflow.base.resources.R
import com.cyberflow.sparkle.DBComponent.loadAvatar
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
import com.cyberflow.sparkle.databinding.ItemImDialogFowardImgRectangleBinding
import com.cyberflow.sparkle.databinding.ItemImDialogFowardImgSquareBinding
import com.cyberflow.sparkle.databinding.ItemImDialogFowardTxtBinding
import com.cyberflow.sparkle.databinding.ItemImDialogFowardVideoRectangleBinding
import com.cyberflow.sparkle.databinding.ItemImDialogFowardVideoSquareBinding
import com.cyberflow.sparkle.im.viewmodel.Contact
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hyphenate.chat.EMImageMessageBody
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMTextMessageBody
import com.hyphenate.chat.EMVideoMessageBody
import com.hyphenate.easeui.utils.EaseImageUtils
import com.hyphenate.easeui.utils.EaseSmileUtils

class ForwardDialog {

    private var mContext: Context? = null
    private var mCallback: Callback? = null
    private var mDialog: Dialog? = null

    private var who: Contact? = null
    private var msg: EMMessage? = null

    interface Callback {
        fun onSelected(ok: Boolean)
    }

    constructor(context: Context, model: Contact, callback: Callback) {
        if (context == null || callback == null) {
            return
        }

        mContext = context
        mCallback = callback
        who = model
        Log.e("ForwardDialog", " who:  $model " )
        msg = IMDataManager.instance.getForwardMsg()

        initView()
        initData()
    }

    private fun initData() {
        val data = arrayListOf<Any>()
        if (msg!=null) {
            tvName?.text = who?.name
            if (ivHead != null && who != null) {
                loadAvatar(ivHead!!, who!!.avatar, who!!.gender)
            }

            msg?.apply {
                when (type) {
                    EMMessage.Type.TXT -> {
                        data.add(ForwardTxt(this))
                    }

                    EMMessage.Type.IMAGE -> {
                        (body as? EMImageMessageBody)?.also {
                            if (it.width > it.height) {
                                data.add(ForwardImgRectangle(this))
                            } else {
                                data.add(ForwardImgSquare(this))
                            }
                        }
                    }

                    EMMessage.Type.VIDEO -> {
                        (body as? EMVideoMessageBody)?.also {
                            if (it.thumbnailWidth > it.thumbnailHeight) {
                                data.add(ForwardVideoRectangle(this))
                            } else {
                                data.add(ForwardVideoSquare(this))
                            }
                        }
                    }

                    else -> {}
                }
            }

        }
        rv?.models = data
    }

    private var rv: RecyclerView? = null
    private var ivHead: ImageView? = null
    private var tvName: TextView? = null
    private fun initView() {
        mDialog = Dialog(mContext!!, R.style.forward_dialog)
        mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog?.setContentView(com.cyberflow.sparkle.R.layout.dialog_foward)
//        mDialog?.setCancelable(false)
//        mDialog?.setCanceledOnTouchOutside(true)
        val window = mDialog?.window
        if (window != null) {
            val lp = window.attributes
            lp.gravity = Gravity.CENTER_VERTICAL
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = lp
//            window.setWindowAnimations(R.style.BottomDialog_Animation)
//            window.setFlags(
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//            )
        }

        mDialog?.apply {
            findViewById<ShadowTxtButton>(com.cyberflow.sparkle.R.id.btn_cancel).setClickListener(
                object : ShadowTxtButton.ShadowClickListener {
                    override fun clicked(disable: Boolean) {
                        mCallback?.onSelected(false)
                    }
                })

            findViewById<ShadowTxtButton>(com.cyberflow.sparkle.R.id.btn_send).setClickListener(
                object : ShadowTxtButton.ShadowClickListener {
                    override fun clicked(disable: Boolean) {
                        mCallback?.onSelected(true)
                    }
                })

            ivHead = findViewById<ImageView>(com.cyberflow.sparkle.R.id.iv_head)
            tvName = findViewById<TextView>(com.cyberflow.sparkle.R.id.tv_name)

            rv = findViewById<RecyclerView>(com.cyberflow.sparkle.R.id.rv)
            rv?.linear()?.setup {
                addType<ForwardTxt>(com.cyberflow.sparkle.R.layout.item_im_dialog_foward_txt)
                addType<ForwardImgSquare>(com.cyberflow.sparkle.R.layout.item_im_dialog_foward_img_square)
                addType<ForwardImgRectangle>(com.cyberflow.sparkle.R.layout.item_im_dialog_foward_img_rectangle)
                addType<ForwardVideoSquare>(com.cyberflow.sparkle.R.layout.item_im_dialog_foward_video_square)
                addType<ForwardVideoRectangle>(com.cyberflow.sparkle.R.layout.item_im_dialog_foward_video_rectangle)
                onBind {
                    when (itemViewType) {
                        com.cyberflow.sparkle.R.layout.item_im_dialog_foward_txt -> {
                            val model = getModel<ForwardTxt>(layoutPosition)
                            getBinding<ItemImDialogFowardTxtBinding>().apply {
                                (model.msg.body as? EMTextMessageBody)?.apply {
                                    val span = EaseSmileUtils.getSmiledText(mContext, message)
                                    tvMsgContent.setText(span, TextView.BufferType.SPANNABLE)
                                }
                            }
                        }

                        com.cyberflow.sparkle.R.layout.item_im_dialog_foward_img_square -> {
                            val model = getModel<ForwardImgSquare>(layoutPosition)
                            getBinding<ItemImDialogFowardImgSquareBinding>().apply {
                                EaseImageUtils.showImageImg(context, ivImg, model.msg)
                            }
                        }

                        com.cyberflow.sparkle.R.layout.item_im_dialog_foward_img_rectangle -> {
                            val model = getModel<ForwardImgRectangle>(layoutPosition)
                            getBinding<ItemImDialogFowardImgRectangleBinding>().apply {
                                EaseImageUtils.showImageImg(context, ivImg, model.msg)
                            }
                        }

                        com.cyberflow.sparkle.R.layout.item_im_dialog_foward_video_square -> {
                            val model = getModel<ForwardVideoSquare>(layoutPosition)
                            getBinding<ItemImDialogFowardVideoSquareBinding>().apply {
                                EaseImageUtils.showVideoImg(context, ivVideoShortCut, model.msg)
                            }
                        }

                        com.cyberflow.sparkle.R.layout.item_im_dialog_foward_video_rectangle -> {
                            val model = getModel<ForwardVideoRectangle>(layoutPosition)
                            getBinding<ItemImDialogFowardVideoRectangleBinding>().apply {
                                EaseImageUtils.showVideoImg(context, ivVideoShortCut, model.msg)
                            }
                        }
                    }
                }
            }
        }
    }

    fun show() {
        Log.e("TAG", "show: mDialog=$mDialog")
        mDialog?.show()
    }

    fun onDestroy() {
        if (mDialog != null) {
            mDialog!!.dismiss()
            mDialog = null
        }
    }
}

data class ForwardTxt(val msg: EMMessage)
data class ForwardImgSquare(val msg: EMMessage)
data class ForwardImgRectangle(val msg: EMMessage)
data class ForwardVideoSquare(val msg: EMMessage)
data class ForwardVideoRectangle(val msg: EMMessage)