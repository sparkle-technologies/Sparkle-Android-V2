package com.cyberflow.sparkle.mainv2.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.PageConst
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.chat.common.utils.ChatPresenter
import com.cyberflow.sparkle.databinding.ActivityMainVersionTwoBinding
import com.cyberflow.sparkle.im.DBManager
import com.cyberflow.sparkle.main.view.MainFeedsFragment
import com.cyberflow.sparkle.main.view.MainFriendsFragment
import com.cyberflow.sparkle.main.view.MainHoroscopeFragment
import com.cyberflow.sparkle.main.view.MainNotifyFragment
import com.cyberflow.sparkle.main.view.MainProfileFragment
import com.cyberflow.sparkle.main.viewmodel.MainViewModel
import com.cyberflow.sparkle.main.viewmodel.parseResource
import com.cyberflow.sparkle.register.view.PageAdapter
import com.cyberflow.sparkle.widget.NotificationDialog
import com.cyberflow.sparkle.widget.ToastDialogHolder
import com.drake.net.utils.withMain
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo
import com.therouter.router.Route
import kotlinx.coroutines.launch

@Route(path = PageConst.App.PAGE_MAIN)
class MainActivityV2 : BaseDBAct<MainViewModel, ActivityMainVersionTwoBinding>() {

    companion object {
        fun go(context: Context) {
            val intent = Intent(context, MainActivityV2::class.java)
            context.startActivity(intent)
        }
    }

    private val friends: MainFriendsFragment by lazy { MainFriendsFragment() }
    private val feeds: MainFeedsFragment by lazy { MainFeedsFragment() }
    private val horoscope: MainHoroscopeFragment by lazy { MainHoroscopeFragment() }
    private val notify: MainNotifyFragment by lazy { MainNotifyFragment() }
    private val profile: MainProfileFragment by lazy { MainProfileFragment() }

    override fun initView(savedInstanceState: Bundle?) {
        var adapter = PageAdapter(supportFragmentManager, lifecycle)
        adapter.addFragment(friends)
        adapter.addFragment(feeds)
        adapter.addFragment(horoscope)
        adapter.addFragment(notify)
        adapter.addFragment(profile)

        mDataBinding.pager.apply {
            offscreenPageLimit = 5
            isUserInputEnabled = false
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            this.adapter = adapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {

                }
            })
//            setPageTransformer(MarginPageTransformer(100))
            mDataBinding.bottomNarBar.setViewPager(this)
        }
    }

    override fun initData() {
        LiveDataBus.get().with(ToastDialogHolder.MAIN_ACTIVITY_NOTIFY, NotificationDialog.ToastBody::class.java).observe(this){
            ToastDialogHolder.getDialog()?.show(this@MainActivityV2, it.type, it.content)
        }
        loadCacheOrRemote()
    }


    /********************* IM ***********************/

    /**
     *  优先本地数据库   好友请求 + 回话列表   UI快速显示缓存数据
     *
     *  其次是 异步请求
     *      1.好友请求列表  显示红点  存数据库
     *      2.好友列表   IM会话列表
     *              好友列表 存数据库
     *              混合后 触发更新   存数据库
     */
    private fun loadCacheOrRemote() {
        initDataObserver()
        loadDBCache()
        loadFriendRequest()
        loadFriendList()
        loadIMConversations()
    }

    private fun initDataObserver() {

        ChatPresenter.getInstance().init()  // chat global observer, like msg received , it should be called after login

        LiveDataBus.get().apply {
            with(DemoConstant.NOTIFY_CHANGE, EaseEvent::class.java).observe(this@MainActivityV2, this@MainActivityV2::receiveNewMsgEvent)
            with(DemoConstant.MESSAGE_CHANGE_CHANGE, EaseEvent::class.java).observe(this@MainActivityV2, this@MainActivityV2::receiveNewMsgEvent)
            with(DemoConstant.MESSAGE_FORWARD, EaseEvent::class.java).observe(this@MainActivityV2, this@MainActivityV2::receiveNewMsgEvent)
            with(DemoConstant.CONVERSATION_READ, EaseEvent::class.java).observe(this@MainActivityV2, this@MainActivityV2::receiveNewMsgEvent)
            with(DemoConstant.CONTACT_CHANGE, EaseEvent::class.java).observe(this@MainActivityV2, this@MainActivityV2::receiveFreshEvent)
            with(DemoConstant.CONTACT_ADD, EaseEvent::class.java).observe(this@MainActivityV2, this@MainActivityV2::receiveFreshEvent)
            with(DemoConstant.CONTACT_UPDATE, EaseEvent::class.java).observe(this@MainActivityV2, this@MainActivityV2::receiveFreshEvent)
            with(DemoConstant.FRESH_MAIN_IM_DATA, EaseEvent::class.java).observe(this@MainActivityV2, this@MainActivityV2::receiveFreshEvent)
        }

        viewModel.inviteMsgObservable.observe(this) {
            viewModel.saveInviteData2DB(it.friend_req_list)
            friends.freshFriendRequest(it.friend_req_list)
        }

        viewModel.contactObservable?.observe(this) {
            viewModel.saveContactData2DB(it.friend_list)
            viewModel.contactList = it.friend_list.orEmpty()
            showMergedData()
        }

        // IM conversation data from server
        viewModel.conversationInfoObservable?.observe(this) { response->
            parseResource(response, object : OnResourceParseCallback<List<EaseConversationInfo>>(true) {
                override fun onSuccess(data: List<EaseConversationInfo>?) {
                    viewModel.conversationList = data
                    showMergedData()
                }
            })
        }

        //  IM conversation data from db cache
        viewModel.conversationCacheObservable?.observe(this) {
            viewModel.conversationList = it.orEmpty()
            showMergedData()
        }
    }

    private fun showMergedData() {
        friends.mergeContactAndConversation()
    }

    private fun loadDBCache() {
        lifecycleScope.launch {
            val friendRequestCache = DBManager.instance.db?.imFriendRequestDao()?.getAll()
            withMain {
                friends.freshFriendRequest(friendRequestCache)
            }
        }

        lifecycleScope.launch {
            val conversationCache = DBManager.instance.db?.imConversationCacheDao()?.getAll()
            withMain {
                friends.showConversationListFromCache(conversationCache)
            }
        }
    }

    private fun loadFriendRequest() {
        viewModel.loadFriendRequestMessages()
    }

    private fun loadFriendList() {
        viewModel.loadContactList()
    }

    private fun loadIMConversations() {
        viewModel.freshConversationData()
    }

    private fun receiveNewMsgEvent(event: EaseEvent?) {
        viewModel.freshConversationData()
    }

    private fun receiveFreshEvent(event: EaseEvent?) {
        viewModel.loadContactAndRequestListData()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val home = Intent(Intent.ACTION_MAIN)
            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            home.addCategory(Intent.CATEGORY_HOME)
            startActivity(home)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    fun setUnRead(unreadTotalCount: Int) {
         mDataBinding.bottomNarBar.setNum(unreadTotalCount)
    }
}