package com.cyberflow.sparkle.im.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.PageConst
import com.cyberflow.base.util.ToastUtil
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.util.click
import com.cyberflow.sparkle.DBComponent.loadImage
import com.cyberflow.sparkle.chat.DemoHelper
import com.cyberflow.sparkle.chat.R
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.chat.databinding.ActivityImChatBinding
import com.cyberflow.sparkle.chat.ui.fragment.ChatFragment
import com.cyberflow.sparkle.chat.ui.goPreview
import com.cyberflow.sparkle.chat.viewmodel.ChatViewModel
import com.cyberflow.sparkle.chat.viewmodel.MessageViewModel
import com.cyberflow.sparkle.chat.viewmodel.parseResource
import com.cyberflow.sparkle.widget.NotificationDialog
import com.cyberflow.sparkle.widget.ToastDialogHolder
import com.google.android.material.snackbar.Snackbar
import com.hyphenate.chat.EMClient
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.easeui.model.EaseEvent
import com.therouter.TheRouter

class ChatActivity : BaseDBAct<ChatViewModel, ActivityImChatBinding>(),
    ChatFragment.OnFragmentInfoListener {

    var conversationId: String = ""
    var avatar: String = ""
    var nickName: String = ""
    var chatType: Int = 0

    private val msgViewModel: MessageViewModel by viewModels()

    companion object {
        fun launch(context: Context, conversationId: String, avatar: String, nickName:String,  chatType: Int) {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, conversationId)
            intent.putExtra(EaseConstant.EXTRA_CONVERSATION_AVATAR, avatar)
            intent.putExtra(EaseConstant.EXTRA_CONVERSATION_NICK_NAME, nickName)
            intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, chatType)
            context.startActivity(intent)
        }
    }

    private fun goMain(){
        TheRouter.build(PageConst.App.PAGE_MAIN).navigation()
    }

    override fun initView(savedInstanceState: Bundle?) {
        intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_ID)?.apply {
            conversationId = this.replace("-", "_")
        }
        intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_AVATAR)?.apply {
            avatar = this
        }
        intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_NICK_NAME)?.apply {
            nickName = this
        }
        chatType = intent.getIntExtra(EaseConstant.EXTRA_CHAT_TYPE, 0)

        if (conversationId.isNullOrEmpty() || chatType == 0) {
            ToastUtil.show(this, "Invalid params")
            finish()
            return
        }

        mDataBinding.layBack.click {
            onBackPressed()
        }

        mDataBinding.ivAvatar.click {
            Log.e(TAG, "ivAvatar:  go chat detail or profile detail? ")
            goPreview(conversationId.replace("_", "-"))
        }

        mDataBinding.ivBtnRight.click {
            Log.e(TAG, "ivBtnRight:  waiting for designer to decide what to do")
        }
        initChatFragment()
    }

    var fragment: ChatFragment? = null

    private fun initChatFragment() {
        chatType?.also {
            Bundle().apply {
                fragment = ChatFragment()
                putString(EaseConstant.EXTRA_CONVERSATION_ID, conversationId)
                putInt(EaseConstant.EXTRA_CHAT_TYPE, it)
                putString(DemoConstant.HISTORY_MSG_ID, "")
                putBoolean(EaseConstant.EXTRA_IS_ROAM, DemoHelper.getInstance().model.isMsgRoaming)
                fragment?.arguments = this
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fl_fragment, fragment!!, "chat").commit()

                fragment?.setOnFragmentInfoListener(this@ChatActivity)
            }
        }
    }

    override fun initData() {
        val conversation = EMClient.getInstance().chatManager().getConversation(conversationId)
        viewModel.deleteObservable.observe(this) {
            parseResource(it, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    finish()
                    val event =
                        EaseEvent.create(DemoConstant.CONVERSATION_DELETE, EaseEvent.TYPE.MESSAGE)
                    msgViewModel.setMessageChange(event)
                }
            })
        }
        msgViewModel.messageChange.with(DemoConstant.MESSAGE_FORWARD, EaseEvent::class.java).observe(this) {
            it?.apply {
                if (isMessageChange) {
                    message?.also { m->
                        showSnackBar(m)
                    }
                }
            }
        }
        msgViewModel.messageChange.with(DemoConstant.CONTACT_CHANGE, EaseEvent::class.java)
            .observe(this) {
                if (it == null) {
                    return@observe
                }
                if (conversation == null) {
                    finish()
                }
            }

        LiveDataBus.get().with(ToastDialogHolder.CHAT_ACTIVITY_NOTIFY, NotificationDialog.ToastBody::class.java).observe(this){
            ToastDialogHolder.getDialog()?.show(this@ChatActivity, it.type, it.content)
        }

        setDefaultTitle()
    }

    private fun showSnackBar(event: String) {
        Snackbar.make(mDataBinding.layTop, event, Snackbar.LENGTH_SHORT).show()
    }

    override fun onChatError(code: Int, errorMsg: String?) {
        if (errorMsg != null) {
            ToastUtil.show(this, errorMsg, false)
        }
    }

    override fun onOtherTyping(action: String?) {
        action?.also {
            if (it == "TypingBegin") {
                mDataBinding.tvTitle.text = getString(R.string.alert_during_typing)
            }
            if (it == "TypingEnd") {
                setDefaultTitle()
            }
        }
    }

    private fun setDefaultTitle() {
        Log.e(TAG, "setDefaultTitle: avatar=${avatar}  nickName=${nickName}")
        mDataBinding.tvTitle.text = nickName
        loadImage(mDataBinding.ivAvatar, avatar)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (null != this.currentFocus) {
            val mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            return mInputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
        }
        return super.onTouchEvent(event)
    }

    override fun onBackPressed() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm != null && window.attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (currentFocus != null) {
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                goMain()
            } else {
                goMain()
            }
        } else {
            goMain()
        }
    }
}