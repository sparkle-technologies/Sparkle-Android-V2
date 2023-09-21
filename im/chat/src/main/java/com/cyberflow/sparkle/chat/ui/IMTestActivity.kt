package com.cyberflow.sparkle.chat.ui

import android.os.Bundle
import android.util.Log
import com.cyberflow.base.BaseApp
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.PageConst
import com.cyberflow.base.util.click
import com.cyberflow.sparkle.chat.DemoHelper
import com.cyberflow.sparkle.chat.common.db.DemoDbHelper
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.chat.common.utils.PreferenceManager
import com.cyberflow.sparkle.chat.databinding.ActivityImTestBinding
import com.cyberflow.sparkle.chat.viewmodel.IMTestViewModel
import com.cyberflow.sparkle.chat.viewmodel.parseResource
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo
import com.therouter.router.Route


private const val NAME = "test"
private const val PWD = "test"

@Route(path = PageConst.IM.SERVICE_IM_TEST)
class IMTestActivity : BaseDBAct<IMTestViewModel, ActivityImTestBinding>() {

    override fun initView(savedInstanceState: Bundle?) {

        PreferenceManager.init(this)   // 不初始化会 crash

        viewModel.initObservable.observe(this) {
            parseResource(it, object : OnResourceParseCallback<Boolean>(true) {
                override fun onSuccess(data: Boolean?) {
                    Log.e(TAG, "onSuccess:  already login in go main activity")
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    Log.e(TAG, "onError: not login before, need go login page")

                }
            })
        }

        mDataBinding.btnInit.click {
//            if (DemoHelper.getInstance().autoLogin) {
//            }
            DemoHelper.getInstance().init(this@IMTestActivity)
            viewModel.checkLogin()
        }

        mDataBinding.btnCreate.click {
            Log.e(TAG, "initView: isSDKInit=${DemoHelper.getInstance().isSDKInit}")
            if (DemoHelper.getInstance().isSDKInit) {
                viewModel.create(NAME, PWD)
            }
        }
        mDataBinding.btnLogin.click {
            var name = mDataBinding.etFrom.text.toString()
            viewModel.login(name, name)
        }
        mDataBinding.btnLogout.click {
            if (DemoHelper.getInstance().isSDKInit) {
                viewModel.logout()
            }
        }

        viewModel.friendObservable.observe(this) {
            it?.data?.also { result ->
                Log.e(TAG, "add friend result:  $result")
                showHint("add friend : $result")
            }
        }
        mDataBinding.btnFriendRequest.click {
            if (DemoHelper.getInstance().isSDKInit) {

                viewModel.addFriend("lover", "hello, guys, we are family 000 ")
//                viewModel.addFriend("war", "i dont like war 000 , but we need talk")

                for (i in 1..5) {
                    viewModel.addFriend("lover${i}", "hello, guys, we are family $i ")
                    viewModel.addFriend("war${i}", "i dont like war${i}, but we need talk")
                }

            }
        }
        mDataBinding.btnAdd.click {
            if (DemoHelper.getInstance().isSDKInit) {
                viewModel.acceptFriend("lover")
            }
        }
        mDataBinding.btnRefuse.click {
            viewModel.refuseFriend("lover")
        }

        mDataBinding.btnSendMsg.click {

            val conversationId = mDataBinding.etTo.text.toString()
            // conversationId=lover3  chatType=1
            ChatActivity.launch(context = this, conversationId, 1)

            /*if(conversationList.isNotEmpty()){
                val info = conversationList[0].info
                if(info is EMConversation){
                    ChatActivity.launch(context = this, info.conversationId(), EaseCommonUtils.getChatType(info))
                }
            }*/
            /*val to = mDataBinding.etTo.text.toString()
            val content = mDataBinding.etContent.text.toString()
            EMMessage.createTextSendMessage(content, to).apply {
                setIsNeedGroupAck(false)
                EMClient.getInstance().chatManager().sendMessage(this)
            }*/
        }

        mDataBinding.btnUserList.click {
            if (DemoHelper.getInstance().isSDKInit) {
                viewModel.getContactList()
            }
        }

        mDataBinding.btnConversationList.click {
            if (DemoHelper.getInstance().isSDKInit) {
                viewModel.getConversationListFromDB()
//                viewModel.getConversationListFromServer(1, 10)
                viewModel.getConversationListFromServer()
            }
        }

        mDataBinding.btnContactList.click{
//            ARouter.getInstance().build(PageConst.Mine.PAGE_FRIENDS_HOME).navigation()
        }

        mDataBinding.btnSearchFriend.click{
//            ARouter.getInstance().build(PageConst.Mine.PAGE_FRIENDS_SEARCH).navigation()
        }

        mDataBinding.btnUpdateInfo.click{
            val username = mDataBinding.etFrom.text.toString()
            viewModel.updateInfo(username)
        }
    }

    override fun initData() {

        mDataBinding.vm = viewModel

        viewModel.createAccountObservable.observe(this) {
            it?.data?.also { result ->
                Log.e(TAG, "create result:  $result")
                showHint("create result:  $result")
            }
        }

        viewModel.loginObservable.observe(this) {
            parseResource(it, object : OnResourceParseCallback<EaseUser>(true) {
                override fun onSuccess(data: EaseUser?) {
                    Log.e(TAG, "login result:  $data")
                    showHint("login result:  ${data?.username}")
                    DemoHelper.getInstance().autoLogin = true
                    DemoDbHelper.getInstance(BaseApp.Companion.instance).initDb(data?.username)
                }
            })
        }

        viewModel.logoutObservable.observe(this) {
            it?.data?.also { result ->
                Log.e(TAG, "logout result:  $result")
                showHint("logout : $result")
            }
        }

        viewModel.contactListObservable.observe(this) {
            Log.e(TAG, "contact list ")
            it?.data?.also { result ->
                result.forEach { user ->
                    Log.e(TAG, "initData: ${user.username}   ${user.toString()}")
                }
            }
        }

        viewModel.conversationDBListObservable.observe(this) {
            it?.data?.also { result ->
                Log.e(TAG, "get conversation List DB result:  $result")
            }
        }

        viewModel.conversationListObservable.observe(this) {
            parseResource(it, object : OnResourceParseCallback<List<EaseConversationInfo>>() {
                override fun onSuccess(data: List<EaseConversationInfo>?) {
                    if (data != null) {
                        conversationList.addAll(data)
                        showHint("get conversation list size: ${data.size}")
                    }
                }
            })
        }
        viewModel.init()
    }

    var conversationList = arrayListOf<EaseConversationInfo>()

    private fun showHint(msg: String) {
        mDataBinding.tvHint.text = "${mDataBinding.tvHint.text} \n $msg"
    }

}