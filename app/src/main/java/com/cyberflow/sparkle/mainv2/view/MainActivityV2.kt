package com.cyberflow.sparkle.mainv2.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.CacheUtil
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
            setCurrentItem(2, false)
            mDataBinding.bottomNarBar.setCurrentPage(2)
        }
    }

    override fun initData() {
        loadCacheOrRemote()
    }


    /**
     *  first:  load cache conversation & friend request data from db  -> show cache data at UI, the first time
     *  second: load remote data from server
     *              1. friend request remote data    -> save to local db
     *              2. friend list remote data       -> save to local db
     *              3. IM conversations remote data  -> save to local db
     *           merge 2 and 3, show remote updated data at UI, the second time
     */
    private fun loadCacheOrRemote() {
        initDataObserver()
        loadDBCache()
        loadFriendRequest()
        loadFriendList()
        loadIMConversations()
        loadIMQuestions()
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

        // IM questions for AIO
        viewModel.imQuestionsObservable?.observe(this) {
            CacheUtil.setAIOQuestions(it)
            friends.showQuestions()
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

    private fun loadIMQuestions() {
        viewModel.loadIMQuestions()
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
        notify.freshData()
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

    private var conversactionUnreadCount = 0
    private var friendRequestCount = 0

    fun setConversactionUnRead(unreadTotalCount: Int) {
        conversactionUnreadCount = unreadTotalCount
        mDataBinding.bottomNarBar.setNum(conversactionUnreadCount + friendRequestCount)
    }

    fun setRequestCount(friendCout: Int) {
        friendRequestCount = friendCout
        mDataBinding.bottomNarBar.setNum(conversactionUnreadCount + friendRequestCount)
    }

    fun setSiteUnRead(unreadTotalCount: Int){
        mDataBinding.bottomNarBar.setSiteMsgNum(unreadTotalCount)
    }
}