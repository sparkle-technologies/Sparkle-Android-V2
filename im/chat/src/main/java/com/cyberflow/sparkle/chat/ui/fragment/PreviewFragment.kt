package com.cyberflow.sparkle.chat.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cyberflow.base.fragment.BaseVBFragment
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.chat.common.model.EmojiconExampleGroupData
import com.cyberflow.sparkle.chat.databinding.FragmentPreivewBinding
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.easeui.input.KeyboardAwareLinearLayout
import com.hyphenate.easeui.interfaces.MessageListItemClickListener
import com.hyphenate.easeui.modules.chat.EaseChatExtendMenu
import com.hyphenate.easeui.modules.chat.EaseChatInputMenu
import com.hyphenate.easeui.modules.chat.EaseChatMessageListLayout
import com.hyphenate.easeui.modules.chat.interfaces.ChatInputMenuListener
import com.hyphenate.easeui.modules.chat.interfaces.IChatExtendMenu

class PreviewFragment : BaseVBFragment<BaseViewModel, FragmentPreivewBinding>(),
    EaseChatMessageListLayout.OnMessageTouchListener,
    MessageListItemClickListener,
    EaseChatMessageListLayout.OnChatErrorListener,
    KeyboardAwareLinearLayout.OnKeyboardShownListener,
    KeyboardAwareLinearLayout.OnKeyboardHiddenListener,
    EaseChatInputMenu.OnConversationInputPanelStateChangeListener, ChatInputMenuListener {

    companion object {

        const val TAG = "PreviewFragment"

        fun go(act: AppCompatActivity, conversationId: String, chatType: Int) {
            val intent = Intent(act, PreviewFragment::class.java)
            intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, conversationId)
            intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, chatType)
            act.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

        /*mViewBind.btnDelete.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {

            }
        })
        mViewBind.btnShare.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {

            }
        })
        mViewBind.btnDownload.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {

            }
        })*/

        /* intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_ID)?.apply {
           conversationId = this
       }
       chatType = intent.getIntExtra(EaseConstant.EXTRA_CHAT_TYPE, 0)*/

        initInputView()
    }

    override fun initData() {

    }

    private fun initInputView() {
        mViewBind.rootLinearLayout.addOnKeyboardShownListener(this);
        mViewBind.inputMenu.init(mViewBind.rootLinearLayout)
        mViewBind.inputMenu.setOnConversationInputPanelStateChangeListener(this)
        mViewBind.inputMenu.setChatInputMenuListener(this)


        mViewBind.messageListLayout.setOnMessageTouchListener(this)
        mViewBind.messageListLayout.setMessageListItemClickListener(this)
        mViewBind.messageListLayout.setOnChatErrorListener(this)

        resetChatExtendMenu()
    }

    private fun resetChatExtendMenu() {
        val chatExtendMenu: IChatExtendMenu = mViewBind.inputMenu.chatExtendMenu
        chatExtendMenu.clear()
        // seven menu in total, include camera, library, send token, send nft, daily horoscope, horoscope, compatibility
        for (i in EaseChatExtendMenu.itemdrawables.indices) {
            chatExtendMenu.registerMenuItem(
                EaseChatExtendMenu.itemStrings[i],
                EaseChatExtendMenu.itemdrawables[i],
                EaseChatExtendMenu.itemIds[i]
            )
        }
        //添加扩展表情
        mViewBind.inputMenu.emojiconMenu.addEmojiconGroup(EmojiconExampleGroupData.getData())
    }

    /********************************************************************/

    override fun onInputPanelExpanded() {
//        mViewBind.inputMenu.hideSoftKeyboard()
//        mViewBind.inputMenu.showExtendMenu(false)
    }

    override fun onInputPanelCollapsed() {

    }

    /********************************************************************/

    override fun onTyping(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun onSendMessage(content: String?) {
    }

    override fun onExpressionClicked(emojicon: Any?) {
    }

    override fun onPressToSpeakBtnTouch(v: View?, event: MotionEvent?): Boolean = false

    override fun onChatExtendMenuItemClick(itemId: Int, view: View?) {
    }

    /********************************************************************/


    override fun onKeyboardHidden() {
        Log.e(TAG, "onKeyboardHidden: ")
        mViewBind.inputMenu.onKeyboardHidden()
    }

    override fun onKeyboardShown() {
        Log.e(TAG, "onKeyboardShown: ")
        mViewBind.inputMenu.onKeyboardShown()
    }



    /*override fun onBackPressed() {
        var consumed = false
        mViewBind.rootLinearLayout.apply {
            if (currentInput != null) {
                hideAttachedInput(true)
                mViewBind.inputMenu.closeConversationInputPanel()
                consumed = true
            }
        }
        if(!consumed)
            super.onBackPressed()
    }*/

    /********************************************************************/

    /*override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (null != this.currentFocus) {
            val mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            return mInputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
        }
        return super.onTouchEvent(event)
    }*/

    /********************************************************************/


    override fun onBubbleClick(message: EMMessage?): Boolean {
        return false
    }

    override fun onResendClick(message: EMMessage?): Boolean {
        return true
    }

    override fun onBubbleLongClick(v: View?, message: EMMessage?): Boolean {
        return false
    }

    override fun onUserAvatarClick(username: String?) {

    }

    override fun onUserAvatarLongClick(username: String?) {

    }

    override fun onMessageCreate(message: EMMessage?) {

    }

    override fun onMessageSuccess(message: EMMessage?) {
    }

    override fun onMessageError(message: EMMessage?, code: Int, error: String?) {
    }

    override fun onMessageInProgress(message: EMMessage?, progress: Int) {
    }

    override fun onTouchItemOutside(v: View?, position: Int) {
        mViewBind.inputMenu.onOutSideClicked()
    }

    override fun onViewDragging() {
        mViewBind.inputMenu.hideSoftKeyboard()
        mViewBind.inputMenu.showExtendMenu(false)
    }

    override fun onChatError(code: Int, errorMsg: String?) {

    }
}