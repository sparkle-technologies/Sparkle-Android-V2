package com.cyberflow.sparkle.im.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.model.IMSearchData
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.sparkle.DBComponent
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.chat.DemoHelper
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.databinding.ActivityImAddFriendBinding
import com.cyberflow.sparkle.im.viewmodel.IMViewModel
import com.cyberflow.sparkle.main.viewmodel.parseResource
import com.cyberflow.sparkle.mainv2.view.MainActivityV2
import com.cyberflow.sparkle.widget.NotificationDialog
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.cyberflow.sparkle.widget.ToastDialogHolder
import com.drake.net.utils.TipUtils
import com.vanniktech.ui.hideKeyboard

class IMAddFriendAct : BaseDBAct<IMViewModel, ActivityImAddFriendBinding>() {

    companion object {
        const val TAG = "IMAddFriendAct"
        fun go(context: Context, data: IMSearchData) {
            val intent = Intent(context, IMAddFriendAct::class.java)
            intent.putExtra(TAG, data)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.llBack.setOnClickListener {
            onBackPressed()
        }

        mDataBinding.btnSend.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked(disable: Boolean) {
                submit()
            }
        })
    }

    private var imAccount = ""

    override fun initData() {
        (intent.getSerializableExtra(TAG) as? IMSearchData)?.apply {
            imAccount = open_uid.replace("-", "_")

            DBComponent.loadAvatar(mDataBinding.ivHead, avatar, gender)

            mDataBinding.tvName.text = nick

           /* val address = if(wallet_address.isNullOrEmpty()) ca_wallet else wallet_address
            if(address.length > 5){
                mDataBinding.tvAddress.text = "${address.substring(0, 5)}...${address.substring(address.length - 5, address.length)}"
            }*/
        }

        viewModel.friendObservable.observe(this) { response ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    if (data == true) {
                        LiveDataBus.get().with(ToastDialogHolder.MAIN_ACTIVITY_NOTIFY).postValue(NotificationDialog.ToastBody(NotificationDialog.TYPE_SUCCESS, getString(R.string.request_sent)))
                        MainActivityV2.go(this@IMAddFriendAct)
                        finish()
                    } else {
                        ToastDialogHolder.getDialog()?.show(this@IMAddFriendAct, NotificationDialog.TYPE_ERROR, getString(R.string.oops_request_failed))
                    }
                }
            })
        }
    }

    private fun submit() {
        val msg = mDataBinding.et.text.toString()
        if (msg.isEmpty()) {
            TipUtils.toast("please Enter authentication messaged")
            return
        }
        hideKeyboard()

        Log.e(TAG, "submit: currentUsername=${DemoHelper.getInstance().model.currentUsername}" )
//        imAccount="c32c8b8b_04ee_439c_a65b_0d46304bdaf7"
        viewModel.addFriend(imAccount, msg)
    }
}
