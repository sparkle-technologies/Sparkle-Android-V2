package com.cyberflow.sparkle.main.view

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.databinding.FragmentMainRightBinding
import com.cyberflow.sparkle.databinding.ItemFriendsFeedEmptyBinding
import com.cyberflow.sparkle.databinding.MainFriendsFeedBinding
import com.cyberflow.sparkle.databinding.MainOfficialBinding
import com.cyberflow.sparkle.im.DBManager
import com.cyberflow.sparkle.im.db.IMUserInfo
import com.cyberflow.sparkle.im.view.ChatActivity
import com.cyberflow.sparkle.im.view.IMSearchFriendAct
import com.cyberflow.sparkle.main.viewmodel.MainViewModel
import com.cyberflow.sparkle.main.viewmodel.parseResource
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.utils.withMain
import com.google.android.material.snackbar.Snackbar
import com.hyphenate.chat.EMConversation
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainRightFragment : BaseDBFragment<BaseViewModel, FragmentMainRightBinding>() {


    private val TAG = "MainRightFragment"
    
    private var actVm: MainViewModel? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        actVm = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun initView(savedInstanceState: Bundle?) {
        Log.e(TAG, "initView: ", )
        initListView()
    }

    private fun initListView() {
        mDatabind.rv.setup {
            addType<HeaderModel>(R.layout.item_hover_header)
            addType<OfficialModel>(R.layout.main_official)
            addType<FriendsModel>(R.layout.main_friends_feed)
            addType<FriendsEmptyModel>(R.layout.item_friends_feed_empty)
            onCreate {
                when (itemViewType) {
                    R.layout.main_official -> {
                        getBinding<MainOfficialBinding>().rv.divider {
                            orientation = DividerOrientation.VERTICAL
                            setDivider(10, true)
                        }.setup {
                            addType<String>(R.layout.item_official)
                            onClick(R.id.root) {
                                Snackbar.make(this.itemView, "click official", Snackbar.LENGTH_SHORT).show()
                                when (this.layoutPosition) {
                                    0 -> {
//                                        mDatabind.rv.models = getData(true)
                                    }

                                    1 -> {
                                        mDatabind.rv.models = getData(false)
                                    }
                                }
                            }
                        }
                    }

                    R.layout.main_friends_feed -> {
                        getBinding<MainFriendsFeedBinding>().rv.divider {
                            orientation = DividerOrientation.HORIZONTAL
                            setDivider(14, true)
                        }.divider {
                            orientation = DividerOrientation.VERTICAL
                            setDivider(10, true)
                        }.setup {
                            addType<FriendMessageInfo>(R.layout.item_friends_feed)
                            addType<FriendsAddModel>(R.layout.item_friends_feed_add)
                            onClick(R.id.lay_go_chat){
                                val model = getModel<FriendMessageInfo>(layoutPosition)
                                ChatActivity.launch(context, conversationId = model.open_uid, avatar = model.avatar, nickName = model.nickname, chatType = 1)
                            }
                            onClick(R.id.bg_new_friend) {
                                IMSearchFriendAct.go(requireActivity())
                            }
                        }
                    }

                    R.layout.item_friends_feed_empty -> {
                        getBinding<ItemFriendsFeedEmptyBinding>().btnAddFriend.setClickListener(
                            object : ShadowTxtButton.ShadowClickListener {
                                override fun clicked(disable: Boolean) {
                                    IMSearchFriendAct.go(requireActivity())
                                }
                            })
                    }
                }
            }

            onBind {
                when (itemViewType) {
                    R.layout.main_official -> {
                        val model = getModel<OfficialModel>()
                        getBinding<MainOfficialBinding>().rv.models = model.names
                    }

                    R.layout.main_friends_feed -> {
                        val model = getModel<FriendsModel>()
                        getBinding<MainFriendsFeedBinding>().rv.models = model.names
                    }
                }
            }
        }

        mDatabind.page.setEnableLoadMore(false)
        mDatabind.page.onRefresh {
            freshIMData()
        }
//        refresh()
    }

    // called by activity
    fun pullToRefreshUI() {
        Log.e(TAG, "refresh: " )
        mDatabind.page.autoRefresh()
    }

    private fun freshIMData(){
        actVm?.refreshIMData()
    }

    override fun initData() {
        Log.e(TAG, "initData: ", )
        initIMListener()
        initHeadData()
        actVm?.loadContactList(true)
    }

    /************************* IM *******************************/
    private fun initIMListener() {
        
        Log.e(TAG, "initIMListener: " )
        
        // from server
        actVm?.conversationInfoObservable?.observe(this) { response ->
            mDatabind.page.finishRefresh()
            Log.e("MainRightFragment", "conversation from server " )
            parseResource(response, object : OnResourceParseCallback<List<EaseConversationInfo>>(true) {
                    override fun onSuccess(data: List<EaseConversationInfo>?) {
                        fetchUserInfoFromLocalDB(data)
                    }
                })
        }

        // from db cache
        actVm?.conversationCacheObservable?.observe(this) {
            mDatabind.page.finishRefresh()
            Log.e("MainRightFragment", "conversation from db cache " )
            fetchUserInfoFromLocalDB(it)
        }

        actVm?.contactObservable?.observe(this){ response ->
            parseResource(response, object : OnResourceParseCallback<List<EaseUser>>() {
                override fun onSuccess(data: List<EaseUser>?) {
                    Log.e("MainRightFragment", "contact from server " )
                    batchRequest(data)
                }
            })
        }

        // after fetch user info from our server
        actVm?.imUserListData?.observe(this){ infoList->
            saveToLocalDB(infoList.user_info_list)
            //?.map {
            //                it.toInfo()
            //            }
        }
    }

    private fun saveToLocalDB(userInfoList: List<IMUserInfo>?) {
        Log.e(TAG, "saveToLocalDB: ", )
        if(userInfoList.isNullOrEmpty()){
            return
        }
        lifecycleScope.launch {
            DBManager.instance.db?.imUserInfoDao()?.insert(*userInfoList.toTypedArray())
            withMain {
                freshIMData()
            }
        }
    }

    // get data from server then save into local db
    private fun batchRequest(data: List<EaseUser>?){
        Log.e(TAG, "batchRequest: ", )
        val open_uid_list = data?.map {
            it.username.replace("_", "-")
        }?.filter { it.length > 20 }?.toList()
        if(open_uid_list.isNullOrEmpty()){
           return
        }
        actVm?.getIMUserInfoList(open_uid_list)
    }

    var allData = arrayListOf<Any>()
    var headData = arrayListOf<Any>()

    private fun initHeadData(){
        Log.e(TAG, "initHeadData: ", )
        headData.add(HeaderModel(title = "Official"))
        headData.add(OfficialModel(arrayListOf("Cora-Official", "King-Official")))
        allData.clear()
        allData.addAll(headData)
        mDatabind.rv.models = allData
    }

    private val map = HashMap<String, IMUserInfo>()
    private val unRead = HashMap<String, Int>()

    private fun fetchUserInfoFromLocalDB(data: List<EaseConversationInfo>?){
        Log.e(TAG, "fetchUserInfoFromLocalDB: ", )
        
        if(data.isNullOrEmpty()){
            showConversationList(null)
        }else{
            lifecycleScope.launch {
                map.clear()
                DBManager.instance.db?.imUserInfoDao()?.getAll()?.forEach {
                    it.open_uid = it.open_uid.replace("-", "_")
                    map[it.open_uid] = it
                }

                data.filter {
                    map.contains((it.info as? EMConversation)?.conversationId())
                }.mapNotNull {
                    val username = (it.info as? EMConversation)?.conversationId() ?: ""
                    val count = (it.info as? EMConversation)?.unreadMsgCount
                    unRead[username] = count ?: 0
                    map[username]
                }.apply {
                    showConversationList(this)
                }
            }
        }
    }

    private fun showConversationList(data: List<IMUserInfo>?) {
        Log.e(TAG, "showConversationList: ", )
        var modelData = arrayListOf<Any>()
        modelData.add(HeaderModel(title = "Friends Feed"))
        if (data.isNullOrEmpty()) {
            modelData.add(FriendsEmptyModel())
        }else{
            var friendMessageList = arrayListOf<Any>()
            data?.also { list ->
                list.forEach { item ->

                    var avatar = item.avatar
                    var imageUrl  = item.feed_avatar
                    var nickname = item.nick
                    var openUid = item.open_uid
                    var bgColor  = item.feed_card_color
                    var num: Int = unRead[item.open_uid] ?: 0

                    friendMessageList.add(FriendMessageInfo(avatar=avatar, imageUrl=imageUrl, nickname=nickname,  open_uid = openUid, bgColor = bgColor, num = num))
                }
                if (friendMessageList.size <= 6) {
                    friendMessageList.add(FriendsAddModel())
                }
            }
            modelData.add(FriendsModel(friendMessageList))
        }
        allData.clear()
        allData.addAll(headData)
        allData.addAll(modelData)
        mDatabind.rv.models = allData
    }

    private fun getData(empty: Boolean = false): List<Any> {
        val r = Random.nextInt(10)
        return listOf(
            HeaderModel(title = "Official"),
            OfficialModel(arrayListOf("Cora-Official", "King-Official")),
            HeaderModel(title = "Friends Feed"),
            if (empty) FriendsEmptyModel() else
                FriendsModel(
                    arrayListOf(
                        FriendMessageInfo(nickname = "Cora-$r"),
                        FriendMessageInfo(nickname ="King-$r"),
                        FriendMessageInfo(nickname ="Cora-$r"),

                        FriendMessageInfo(nickname ="King-$r"),
                        FriendMessageInfo(nickname ="Cora-$r"),
                        FriendMessageInfo(nickname ="King-$r"),

                        FriendMessageInfo(nickname ="King-$r"),
                        FriendMessageInfo(nickname ="Cora-$r"),
                        FriendMessageInfo(nickname ="King-$r")
                    )
                )
        )
    }
}