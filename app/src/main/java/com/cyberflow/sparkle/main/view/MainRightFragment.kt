package com.cyberflow.sparkle.main.view

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
import com.cyberflow.sparkle.databinding.FragmentMainRightBinding
import com.cyberflow.sparkle.databinding.ItemFriendsFeedBinding
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
import com.hyphenate.chat.EMConversation
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
        Log.e(TAG, "initView: ")
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
//                                Snackbar.make(this.itemView, "click official", Snackbar.LENGTH_SHORT).show()
                                when (this.layoutPosition) {
                                    0 -> {
//                                        mDatabind.rv.models = getData(true)
                                    }

                                    1 -> {
//                                        mDatabind.rv.models = getData(false)
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
                            onBind {
                                when(itemViewType){
                                    R.layout.item_friends_feed ->{
                                        val model = getModel<FriendMessageInfo>()
                                        getBinding<ItemFriendsFeedBinding>().bgBottomColor.apply {
                                            (background as? GradientDrawable)?.also {
                                                it.setColor(Color.parseColor(model.bgColor))
                                                background = it
                                            }
                                        }
                                    }
                                }
                            }
                            onClick(R.id.lay_go_chat) {
                                val model = getModel<FriendMessageInfo>(layoutPosition)
                                ChatActivity.launch(
                                    context,
                                    conversationId = model.open_uid,
                                    avatar = model.avatar,
                                    nickName = model.nickname,
                                    chatType = 1
                                )
                            }
                            onClick(R.id.bg_new_friend) {
                                IMSearchFriendAct.go(requireActivity(), actVm?.getContactList().orEmpty())
                            }
                        }
                    }

                    R.layout.item_friends_feed_empty -> {
                        getBinding<ItemFriendsFeedEmptyBinding>().btnAddFriend.setClickListener(
                            object : ShadowTxtButton.ShadowClickListener {
                                override fun clicked(disable: Boolean) {
                                    IMSearchFriendAct.go(requireActivity(), actVm?.getContactList().orEmpty())
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
        Log.e(TAG, "pullToRefreshUI: ")
        mDatabind.page.autoRefresh()
    }

    // no friends contact list
    fun showEmpty() {
        Log.e(TAG, "showEmpty: ")
        showConversationList(null)
    }

    private fun freshIMData() {
        Log.e(TAG, "freshIMData: ", )
        actVm?.freshContactData()
    }

    override fun initData() {
        Log.e(TAG, "initData: ")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initIMListener()
        initHeadData()
    }

    /************************* IM *******************************/
    private fun initIMListener() {
        Log.e(TAG, "initIMListener: ")
        // from server
        actVm?.conversationInfoObservable?.observe(requireActivity()) { response ->
            mDatabind.page.finishRefresh()
            Log.e("MainRightFragment", "conversation from server ")
            parseResource(response, object : OnResourceParseCallback<List<EaseConversationInfo>>(true) {
                    override fun onSuccess(data: List<EaseConversationInfo>?) {
                        fetchUserInfoFromLocalDB(data)
                    }
                })
        }

        // from db cache
        actVm?.conversationCacheObservable?.observe(requireActivity()) {
            mDatabind.page.finishRefresh()
            Log.e("MainRightFragment", "conversation from db cache ")
            fetchUserInfoFromLocalDB(it)
        }
    }


    var allData = arrayListOf<Any>()
    var headData = arrayListOf<Any>()

    private fun initHeadData() {
        Log.e(TAG, "initHeadData: ")
        headData.add(HeaderModel(title = "Official"))
        headData.add(OfficialModel(arrayListOf("Cora-Official", "King-Official")))
        allData.clear()
        allData.addAll(headData)
        mDatabind.rv.models = allData
    }

    private val unRead = HashMap<String, Int>()

    private fun fetchUserInfoFromLocalDB(data: List<EaseConversationInfo>?) {
        Log.e(TAG, "fetchUserInfoFromLocalDB: ")
        lifecycleScope.launch {
            val allContactList = DBManager.instance.db?.imUserInfoDao()?.getAll()
            if (data.isNullOrEmpty() && allContactList.isNullOrEmpty()) {
                withMain { showConversationList(null) }
                return@launch
            } else {

                allContactList?.associate {
                    it.open_uid.replace("-", "_") to it
                }?.also { allContactMap ->
                    val allData = arrayListOf<IMUserInfo>()
                    val contactOpenUidList = IMDataManager.instance.getContactData().map { it.username }
                    val conversaction = data?.filter {
                        contactOpenUidList.contains((it.info as? EMConversation)?.conversationId())
                    }?.mapNotNull {
                        val username = (it.info as? EMConversation)?.conversationId() ?: ""
                        val count = (it.info as? EMConversation)?.unreadMsgCount
                        unRead[username] = count ?: 0
                        allContactMap[username]
                    }.orEmpty()

                    val mark = conversaction.map { it.open_uid.replace("-", "_") }.toSet()

                    Log.e(TAG, "mark: ${mark.toString()}" )

                    allContactMap.forEach {
                        Log.e(TAG, "allContactMap.forEach: key=${it.key}   value=${it.value}", )
                    }

                    val contactData = contactOpenUidList.filter {
                        !mark.contains(it)
                    }.mapNotNull {
                        allContactMap[it]
                    }.sortedBy { it.nick }.orEmpty()

                    conversaction.forEach {
                        Log.e(TAG, "conversaction: ${it.nick}" )
                    }

                    contactData.forEach {
                        Log.e(TAG, "contactData: ${it.nick}" )
                    }

                    allData.addAll(conversaction)
                    allData.addAll(contactData)

                    if (allData.isNotEmpty()) {
                        showConversationList(allData)
                    } else {
                        showConversationList(null)
                    }
                }
            }
        }
    }

    private fun showConversationList(data: List<IMUserInfo>?) {
        Log.e(TAG, "showConversationList: ")
        var modelData = arrayListOf<Any>()
        modelData.add(HeaderModel(title = "Friends Feed"))
        if (data.isNullOrEmpty()) {
            modelData.add(FriendsEmptyModel())
        } else {
            var friendMessageList = arrayListOf<Any>()
            data?.also { list ->
                list.forEach { item ->

                    var avatar = item.avatar
                    var imageUrl = item.feed_avatar
                    var nickname = item.nick
                    var openUid = item.open_uid.replace("-", "_")
                    var bgColor = item.feed_card_color
                    var num: Int = unRead[item.open_uid] ?: 0

                    friendMessageList.add(
                        FriendMessageInfo(
                            avatar = avatar,
                            imageUrl = imageUrl,
                            nickname = nickname,
                            open_uid = openUid,
                            bgColor = bgColor,
                            num = num
                        )
                    )
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
            if (empty) FriendsEmptyModel() else FriendsModel(
                arrayListOf(
                    FriendMessageInfo(nickname = "Cora-$r"),
                    FriendMessageInfo(nickname = "King-$r"),
                    FriendMessageInfo(nickname = "Cora-$r"),

                    FriendMessageInfo(nickname = "King-$r"),
                    FriendMessageInfo(nickname = "Cora-$r"),
                    FriendMessageInfo(nickname = "King-$r"),

                    FriendMessageInfo(nickname = "King-$r"),
                    FriendMessageInfo(nickname = "Cora-$r"),
                    FriendMessageInfo(nickname = "King-$r")
                )
            )
        )
    }
}