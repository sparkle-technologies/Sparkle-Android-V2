package com.cyberflow.sparkle.im.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.model.IMSearchData
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.databinding.ActivityImAddFriendBinding
import com.cyberflow.sparkle.im.viewmodel.IMViewModel
import com.cyberflow.sparkle.login.widget.ShadowTxtButton
import com.cyberflow.sparkle.main.viewmodel.parseResource
import com.drake.net.utils.TipUtils
import com.google.android.material.snackbar.Snackbar
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

    private var searchName = ""

    override fun initData() {
        (intent.getSerializableExtra(TAG) as? IMSearchData)?.apply {
            searchName = name
            mDataBinding.tvName.text = name
            mDataBinding.tvAddress.text = address
        }

        viewModel.friendObservable.observe(this) { response ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    if (data == true) {
                        Snackbar.make(mDataBinding.ivHead, "request send successfully", Snackbar.LENGTH_SHORT).show()
                        finish()
                    } else {
                        TipUtils.toast("failed to send request")
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
        viewModel.addFriend(searchName, msg)
    }
}
