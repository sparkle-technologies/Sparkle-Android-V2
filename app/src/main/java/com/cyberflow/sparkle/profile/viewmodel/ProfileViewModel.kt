package com.cyberflow.sparkle.profile.viewmodel

import androidx.lifecycle.viewModelScope
import com.cyberflow.base.BaseApp
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.util.bus.SingleSourceLiveData
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.common.db.entity.InviteMessageStatus
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMTextMessageBody
import com.hyphenate.easeui.manager.EaseSystemMsgManager
import com.hyphenate.easeui.model.EaseEvent
import kotlinx.coroutines.launch

class ProfileViewModel : BaseViewModel() {

    val acceptFriendObservable = SingleSourceLiveData<String>()

    fun acceptFriend(msg : EMMessage?){
        if(msg==null) {
            acceptFriendObservable.postValue("")
            return
        }
        viewModelScope.launch {
            val name = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
            EMClient.getInstance().contactManager().acceptInvitation(name)
            val message = BaseApp.instance?.getString(com.cyberflow.sparkle.chat.R.string.demo_system_agree_invite, msg.getStringAttribute(
                DemoConstant.SYSTEM_MESSAGE_FROM))
            msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.AGREED.name)
            msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_REASON, message)
            val body = EMTextMessageBody(message)
            msg.body = body
            EaseSystemMsgManager.getInstance().updateMessage(msg)
            acceptFriendObservable.postValue(name)
            LiveDataBus.get().with(DemoConstant.NOTIFY_CHANGE).postValue(EaseEvent())
        }
    }

}