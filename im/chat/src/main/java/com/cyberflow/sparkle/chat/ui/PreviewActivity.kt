package com.cyberflow.sparkle.chat.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cyberflow.base.act.BaseVBAct
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.chat.DemoHelper
import com.cyberflow.sparkle.chat.R
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.databinding.ActivityPreivewBinding
import com.cyberflow.sparkle.chat.ui.fragment.PreviewFragment
import com.hyphenate.easeui.constants.EaseConstant

class PreviewActivity : BaseVBAct<BaseViewModel, ActivityPreivewBinding>() {

    companion object {
        fun go(act: AppCompatActivity, conversationId: String, chatType: Int) {
            val intent = Intent(act, PreviewActivity::class.java)
            intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, conversationId)
            intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, chatType)
            act.startActivity(intent)
        }
    }


    private var fragment: PreviewFragment? = null

    private var conversationId: String = ""
    private var chatType: Int = 0

    override fun initView(savedInstanceState: Bundle?) {
        intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_ID)?.apply {
            conversationId = this
        }
        chatType = intent.getIntExtra(EaseConstant.EXTRA_CHAT_TYPE, 0)

        initChatFragment()
    }

    private fun initChatFragment() {
        chatType?.also {
            Bundle().apply {
                fragment = PreviewFragment()
                putString(EaseConstant.EXTRA_CONVERSATION_ID, conversationId)
                putInt(EaseConstant.EXTRA_CHAT_TYPE, it)
                putString(DemoConstant.HISTORY_MSG_ID, "")
                putBoolean(EaseConstant.EXTRA_IS_ROAM, DemoHelper.getInstance().model.isMsgRoaming)
                fragment?.arguments = this
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fl_fragment, fragment!!, "preview").commit()
            }
        }
    }

    override fun initData() {

    }

}