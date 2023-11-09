package com.cyberflow.sparkle.mainv2.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.model.IMUserInfo
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.common.db.entity.InviteMessageStatus
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.chat.common.utils.ChatPresenter
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
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
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent
import kotlinx.coroutines.launch

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
        loadIMConversations()
    }

    /********************* IM ***********************/

    private fun freshConversationData(event: EaseEvent?) {
        Log.e(TAG, "freshConversationData: event:${event?.event}  refresh:${event?.refresh}  message:${event?.message}",)
        event?.also {
            viewModel.freshConversationData()
        }
    }

    private fun freshContactData(event: EaseEvent?) {
        event?.also {
            viewModel.freshContactData()
        }
    }

    override fun onDestroy() {
        DBManager.instance.closeDB()
        super.onDestroy()
    }

    private fun loadIMConversations() {
        DBManager.instance.initDB(this)

        ChatPresenter.getInstance()
            .init()  // chat global observer, like msg received , it should be called after login

        LiveDataBus.get().apply {
            with(DemoConstant.NOTIFY_CHANGE, EaseEvent::class.java).observe(
                this@MainActivityV2,
                this@MainActivityV2::freshConversationData
            )
            with(
                DemoConstant.MESSAGE_CHANGE_CHANGE,
                EaseEvent::class.java
            ).observe(this@MainActivityV2, this@MainActivityV2::freshConversationData)
            with(DemoConstant.MESSAGE_FORWARD, EaseEvent::class.java).observe(
                this@MainActivityV2,
                this@MainActivityV2::freshConversationData
            )
            with(DemoConstant.CONVERSATION_READ, EaseEvent::class.java).observe(
                this@MainActivityV2,
                this@MainActivityV2::freshConversationData
            )

            with(DemoConstant.CONTACT_CHANGE, EaseEvent::class.java).observe(
                this@MainActivityV2,
                this@MainActivityV2::freshContactData
            )
            with(DemoConstant.CONTACT_ADD, EaseEvent::class.java).observe(
                this@MainActivityV2,
                this@MainActivityV2::freshContactData
            )
            with(DemoConstant.CONTACT_UPDATE, EaseEvent::class.java).observe(
                this@MainActivityV2,
                this@MainActivityV2::freshContactData
            )
        }

        viewModel.inviteMsgObservable.observe(this) { list ->
            list?.also {
                IMDataManager.instance.setInviteData(list)
                val res = it.filter { msg ->
                    val statusParam = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS)
                    val status = InviteMessageStatus.valueOf(statusParam)
                    status == InviteMessageStatus.BEINVITEED
                }
                val count = res.size

                val open_uid_list = res.map {
                    it.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM).replace("_", "-")
                }

                if (open_uid_list.isNotEmpty()) {
                    viewModel.getIMNewFriendInfoList(open_uid_list)
                }
            }
        }

        viewModel.contactObservable?.observe(this) { response ->
            parseResource(response, object : OnResourceParseCallback<List<EaseUser>>() {
                override fun onSuccess(data: List<EaseUser>?) {
                    IMDataManager.instance.setContactData(data)
                    Log.e("MainActivityV2", "contact from server size: ${data?.size}")
                    contactsBatchRequestInfo(data)
                }
            })
        }

        viewModel.imNewFriendListData?.observe(this) { infoList ->
            saveToLocalDB(infoList.user_info_list, true)
        }

        viewModel.imContactListData?.observe(this) { infoList ->
            saveToLocalDB(infoList.user_info_list, true)
        }

        viewModel.homeUnReadObservable.observe(this) {
            if (it.isNotEmpty()) {
                Log.e("MainActivityV2", "UnRead Count: $it")   // todo :  PM not decide yet
            }
        }

        viewModel.freshContactData()
    }

    private fun saveToLocalDB(
        userInfoList: List<IMUserInfo>?,
        needFetchConversation: Boolean = false
    ) {
        Log.e(TAG, "saveToLocalDB: isContactData=$needFetchConversation")
        if (userInfoList.isNullOrEmpty()) {
            return
        }
        lifecycleScope.launch {
            DBManager.instance.db?.imUserInfoDao()?.insert(*userInfoList.toTypedArray())
            if (needFetchConversation) {
                withMain {
                    freshConversationData(EaseEvent())
                }
            }
        }
    }

    // get data from server then save into local db
    private fun contactsBatchRequestInfo(data: List<EaseUser>?) {
        Log.e(TAG, "batchRequest:  size: ${data?.size}")
        val open_uid_list = data?.map {
            it.username.replace("_", "-")
        }?.filter { it.length > 20 }?.toList()
        if (open_uid_list.isNullOrEmpty()) {
            Log.e(TAG, "contactsBatchRequestInfo: open_uid_list isNullOrEmpty")
            friends.showEmpty()  // no friends
            return
        }
        viewModel.getIMContactInfoList(open_uid_list)
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
}