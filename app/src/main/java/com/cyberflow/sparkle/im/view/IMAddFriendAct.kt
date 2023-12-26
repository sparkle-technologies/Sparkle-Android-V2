package com.cyberflow.sparkle.im.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.model.BindingResult
import com.cyberflow.base.model.IMSearchData
import com.cyberflow.base.net.Api
import com.cyberflow.sparkle.DBComponent
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.chat.DemoHelper
import com.cyberflow.sparkle.databinding.ActivityImAddFriendBinding
import com.cyberflow.sparkle.im.viewmodel.IMViewModel
import com.cyberflow.sparkle.mainv2.view.MainActivityV2
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.drake.net.Post
import com.drake.net.utils.scopeDialog
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
    }

    private fun submit() {
        val msg = mDataBinding.et.text.toString()
        hideKeyboard()
        scopeDialog {
            Log.e(TAG, "submit: currentUsername=${DemoHelper.getInstance().model.currentUsername}" )
            try{
                //        imAccount="c32c8b8b_04ee_439c_a65b_0d46304bdaf7"
                val data = Post<BindingResult>(Api.RELATIONSHIP_FRIEND_REQUEST) {
                    json("open_uid" to imAccount.replace("_", "-"), "req_msg" to msg)
                }.await()

                viewModel.IM_addFriend(imAccount.replace("-", "_"), msg) // todo our server do not ready for that, so wait

                data?.let {
                    toastSuccess(getString(R.string.request_sent))
                    MainActivityV2.go(this@IMAddFriendAct)
                    finish()
                }
            }catch (e: Exception){
                toastError(getString(R.string.oops_request_failed))
            }
        }
    }
}
