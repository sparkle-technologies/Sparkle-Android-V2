package com.cyberflow.sparkle.main.view

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.model.IMConversationCache
import com.cyberflow.base.model.IMFriendInfo
import com.cyberflow.base.model.IMFriendRequest
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.ConstantGlobal
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.FragmentMainFriendsBinding
import com.cyberflow.sparkle.databinding.ItemFriendsFeedBinding
import com.cyberflow.sparkle.databinding.ItemFriendsFeedEmptyBinding
import com.cyberflow.sparkle.databinding.ItemOfficialCoraBinding
import com.cyberflow.sparkle.databinding.MainFriendsFeedBinding
import com.cyberflow.sparkle.databinding.MainOfficialBinding
import com.cyberflow.sparkle.im.view.ChatActivity
import com.cyberflow.sparkle.im.view.IMContactListAct
import com.cyberflow.sparkle.im.view.IMScanAct
import com.cyberflow.sparkle.im.view.IMSearchFriendAct
import com.cyberflow.sparkle.main.adapter.QuestionsAdapter
import com.cyberflow.sparkle.main.viewmodel.MainViewModel
import com.cyberflow.sparkle.mainv2.view.MainActivityV2
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hyphenate.chat.EMConversation
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo
import com.youth.banner.Banner
import kotlinx.coroutines.launch

class MainFriendsFragment : BaseDBFragment<BaseViewModel, FragmentMainFriendsBinding>() {


    private val TAG = "MainFriendsFragment"

    private var actVm: MainViewModel? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        CacheUtil.setCoraInfo(null)   // every time app launched, clear cora info cache, so it well be updated at ChatActivity
        actVm = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun initView(savedInstanceState: Bundle?) {
        initAddFriend()
        initListView()
    }

    private fun initAddFriend() {
        mDatabind.root.setOnClickListener {
            hideAddDialog()
        }

        mDatabind.btnAddFriends.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                mDatabind.layDialogAdd.apply {
                    visibility = if (this.visibility == View.VISIBLE) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }
            }
        })

        mDatabind.layDialogAdd.apply {
            tvNumSecLayer = findViewById<TextView>(R.id.tv_num)
            findViewById<View>(R.id.lay_add_friends).setOnClickListener {
                IMSearchFriendAct.go(requireActivity())
                mDatabind.layDialogAdd.visibility = View.GONE
            }
            findViewById<View>(R.id.lay_contacts).setOnClickListener {
                IMContactListAct.go(requireActivity())
                mDatabind.layDialogAdd.visibility = View.GONE
            }
            findViewById<View>(R.id.lay_scan).setOnClickListener {
                IMScanAct.go(requireActivity())
                mDatabind.layDialogAdd.visibility = View.GONE
            }
        }
    }


    private fun hideAddDialog(){
        mDatabind.layDialogAdd.apply {
            visibility = if (this.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    private fun initListView() {
        mDatabind.rv.setup {
//            addType<HeaderModel>(R.layout.item_hover_header)
//            addType<OfficialModel>(R.layout.main_official)
            addType<OfficialCoraModel>(R.layout.item_official_cora)
            addType<FriendsModel>(R.layout.main_friends_feed)
            addType<FriendsEmptyModel>(R.layout.item_friends_feed_empty)
            onCreate {
                when (itemViewType) {
                    R.layout.item_official_cora -> {
                        banner = getBinding<ItemOfficialCoraBinding>().banner  as? Banner<HoroscopeReq, HoroscopeAdapter>
                        tvCoraUnread = getBinding<ItemOfficialCoraBinding>().coraUnread
                        Log.e(TAG, "initListView: showQuestions", )
                        showQuestions()
                    }
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
                                when (itemViewType) {
                                    R.layout.item_friends_feed -> {
                                        val model = getModel<FriendMessageInfo>()
                                        getBinding<ItemFriendsFeedBinding>().apply {
                                            (bgBottomColor.background as? GradientDrawable)?.also {
                                                it.setColor(Color.parseColor(model.bgColor))
                                                bgBottomColor.background = it
                                            }
                                            val gotMsg = model.num > 0
                                            contactLine.isVisible = gotMsg
                                            bgContactMsg.isVisible = gotMsg
                                            tvUnread.isVisible = gotMsg

                                            if(gotMsg){
                                                var txt = "${model.num}"
                                                if(model.num > 99){
                                                    txt = "···"
                                                }
                                                tvUnread.text = txt
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
                                    nickName = model.nickname
                                )
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
                    R.layout.item_official_cora -> {
                        getBinding<ItemOfficialCoraBinding>().root.setOnClickListener {
                            chatWithCora(null)
                        }
                    }

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
        mDatabind.page?.onError {
            findViewById<ShadowTxtButton>(R.id.btn).setClickListener(object :
                ShadowTxtButton.ShadowClickListener {
                override fun clicked(disable: Boolean) {
                    freshIMData()
                }
            })
        }
//        refresh()
    }

    // called by activity
    fun pullToRefreshUI() {
        mDatabind.page.autoRefresh()
    }

    // no friends contact list
    fun showEmpty() {
        showConversationList(null)
        mDatabind.page.finishRefresh()
    }

    private fun freshIMData() {
        actVm?.loadContactAndRequestListData()
    }

    override fun initData() {

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initHeadData()
    }


    var allData = arrayListOf<Any>()
    var headData = arrayListOf<Any>()

    private fun initHeadData() {
//        headData.add(HeaderModel(title = "Official"))
//        headData.add(OfficialModel(arrayListOf("Cora-Official", "King-Official")))
        headData.add(OfficialCoraModel( "Cora"))
        allData.clear()
        allData.addAll(headData)
        mDatabind.rv.models = allData
    }

    override fun onResume() {
        super.onResume()
        showFriendRequestNum()
        if(banner?.isVisible == true){
            banner?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        banner?.stop()
    }

    var totalUnread = 0
    var tvNumSecLayer :TextView? = null  // in layout/include_main_top_right_add

    private fun showFriendRequestNum() {
//        Log.e(TAG, "showFriendRequestNum: isVisible=$isVisible" )
        if(isVisible){
            if(totalUnread > 0){
                tvNumSecLayer?.isVisible = true
                mDatabind.tvNum.isVisible = true

                if(totalUnread > 99){
                    tvNumSecLayer?.text = "···"
                    mDatabind.tvNum.text = "···"
                }else{
                    tvNumSecLayer?.text = "$totalUnread"
                    mDatabind.tvNum.text = "$totalUnread"
                }
            }else{
                mDatabind.tvNum?.isVisible = false
                tvNumSecLayer?.isVisible = false
            }
        }
    }

    fun freshFriendRequest(friendReqList: List<IMFriendRequest>?) {
        totalUnread = friendReqList?.size ?: 0
        showFriendRequestNum()
    }

    fun mergeContactAndConversation() {
        Log.e(TAG, "mergeContactAndConversation: " )
        actVm?.apply {
            if(contactList == null || contactList == null){
                return
            }
            if(conversationList.isNullOrEmpty() && contactList.isNullOrEmpty()){
                showEmpty()
            }else{
                merge(contactList, conversationList)
            }
        }
    }

    private val unRead = HashMap<String, Int>()

    private fun merge(contactList: List<IMFriendInfo>?, imConversationList: List<EaseConversationInfo>?) {
        Log.e(TAG, "merge: allContactList.size=${contactList?.size}  data.size=${imConversationList?.size}" )
        /*contactList?.forEach {
            Log.e(TAG, "merge:IMFriendInfo: $it", )
        }
        imConversationList?.forEach {
            Log.e(TAG, "merge:EaseConversationInfo: $it", )
        }*/
        if(contactList.isNullOrEmpty()){
            showConversationList(null)
            return
        }
        lifecycleScope.launch {
            contactList?.associate {
                it.open_uid.replace("-", "_") to it
            }?.also { allContactMap ->
                val allData = arrayListOf<IMFriendInfo>()
                val contactOpenUidList = contactList.map { it.open_uid }.map { it.replace("-", "_") }
                val conversaction = imConversationList?.filter {
                    contactOpenUidList.contains((it.info as? EMConversation)?.conversationId())
                }?.mapNotNull {
                    val username = (it.info as? EMConversation)?.conversationId() ?: ""
                    val count = (it.info as? EMConversation)?.unreadMsgCount
//                        Log.e(TAG, "fetchUserInfoFromLocalDB: username=$username  count=$count" )
                    unRead[username] = count ?: 0
                    allContactMap[username]
                }.orEmpty()

                val mark = conversaction.map { it.open_uid.replace("-", "_") }.toSet()
//                Log.e(TAG, "merge:mark= $mark", )
                val contactData = contactOpenUidList.filter {
                    !mark.contains(it)
                }.mapNotNull {
                    allContactMap[it]
                }.sortedBy { it.nick }.orEmpty()


                allData.addAll(conversaction)
                allData.addAll(contactData)

                actVm?.saveConversationData2DB(unRead, allData)

                if (allData.isNotEmpty()) {
                    showConversationList(allData)
                } else {
                    showConversationList(null)
                }
            }
        }
    }

    var coraUnreadCount = 0

    fun showConversationListFromCache(data: List<IMConversationCache>?) {
        var modelData = arrayListOf<Any>()
//        modelData.add(HeaderModel(title = "Friends Feed"))
        if (!data.isNullOrEmpty()) {
            var friendMessageList = arrayListOf<Any>()
            data?.also { list ->

                var totalUnreadCount = 0
                list.forEach { item ->

                    var avatar = item.avatar
                    var imageUrl = item.feed_avatar
                    var nickname = item.nick
                    var openUid = item.open_uid.replace("-", "_")
                    var bgColor =  item.bgColor
                    var num: Int = item.num
                    totalUnreadCount += num

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

                (requireActivity() as? MainActivityV2)?.setUnRead(totalUnreadCount)
                showQuestions()

                if (friendMessageList.size <= 6) {
                    friendMessageList.add(FriendsAddModel())
                }
            }
            modelData.add(FriendsModel(friendMessageList))
            allData.clear()
            allData.addAll(headData)
            allData.addAll(modelData)
            if (allData.isNotEmpty()) {
                mDatabind.rv.models = allData
            }
        }
    }

    private fun showConversationList(data: List<IMFriendInfo>?) {
        var modelData = arrayListOf<Any>()
//        modelData.add(HeaderModel(title = "Friends Feed"))
        if (data.isNullOrEmpty()) {
            modelData.add(FriendsEmptyModel())
        } else {
            var friendMessageList = arrayListOf<Any>()
            data?.also { list ->

                var totalUnreadCount = 0
                list.forEach { item ->

                    var avatar = item.avatar
                    var imageUrl = item.feed_avatar
                    var nickname = item.nick
                    var openUid = item.open_uid.replace("-", "_")
                    var bgColor = item.feed_card_color
                    var num: Int = unRead[openUid] ?: 0
                    totalUnreadCount += num

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

                (requireActivity() as? MainActivityV2)?.setUnRead(totalUnreadCount)

                if (friendMessageList.size <= 6) {
                    friendMessageList.add(FriendsAddModel())
                }
            }
            modelData.add(FriendsModel(friendMessageList))
        }
        allData.clear()
        allData.addAll(headData)
        allData.addAll(modelData)
        if (allData.isNotEmpty()) {
            mDatabind.rv.models = allData
        }
        mDatabind.page.finishRefresh()
    }

    private var banner : Banner<HoroscopeReq, HoroscopeAdapter>? = null
    private var tvCoraUnread : TextView? = null

    fun showQuestions() {
        val questions = CacheUtil.getAIOQuestions()
        Log.e(TAG, "showQuestions: question.list=${questions?.questions?.size}  coraUnreadCount=$coraUnreadCount   banner=$banner   coraUnread=$tvCoraUnread" )
        if(coraUnreadCount > 0) {
            // hide banner, show cora unread msg
            banner?.isVisible = false
            tvCoraUnread?.isVisible = true
            if(coraUnreadCount == 1) {
                tvCoraUnread?.text = "You have $coraUnreadCount new message"
            }else{
                tvCoraUnread?.text = "You have $coraUnreadCount new messages"
            }
            tvCoraUnread?.setOnClickListener {
                chatWithCora(null)
            }
        }else{
            if(questions?.questions.isNullOrEmpty() || banner == null) {
                return
            }
            banner?.isVisible = true
            tvCoraUnread?.isVisible = false
            banner?.also {
                it.addBannerLifecycleObserver(this)
                    .setAdapter(QuestionsAdapter(questions?.questions.orEmpty()))
                    .setOnBannerListener { data, position ->
                        Log.e(TAG, "onBannerClick: data=$data   position=$position")
                        chatWithCora(data?.toString())
                    }
            }
        }
    }

    // go chat with cora
    // if question is null,
    //      just open chat page,
    //      otherwise send a question to cora, no more hi cora greeting button
    private fun chatWithCora(question: String?) {
        var url = ""
        var name = "Cora"
        CacheUtil.getCoraInfo()?.apply {
            url = user?.avatar.orEmpty()
            name = user?.nick ?: "Cora"
        }
        ChatActivity.launch(requireActivity(), conversationId = ConstantGlobal.getCoraOpenUid(), avatar = url, nickName = name, question = question)
    }
}