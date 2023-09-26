package com.cyberflow.sparkle.main.view

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.chat.ui.ChatActivity
import com.cyberflow.sparkle.databinding.FragmentMainRightBinding
import com.cyberflow.sparkle.databinding.ItemFriendsFeedEmptyBinding
import com.cyberflow.sparkle.databinding.MainFriendsFeedBinding
import com.cyberflow.sparkle.databinding.MainOfficialBinding
import com.cyberflow.sparkle.login.widget.ShadowTxtButton
import com.cyberflow.sparkle.main.viewmodel.MainViewModel
import com.cyberflow.sparkle.main.viewmodel.parseResource
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.google.android.material.snackbar.Snackbar
import com.hyphenate.chat.EMConversation
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo
import kotlin.random.Random

class MainRightFragment : BaseDBFragment<BaseViewModel, FragmentMainRightBinding>() {

    override fun initData() {
        initIMListener()
    }

    private var actVm: MainViewModel? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        actVm = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun initView(savedInstanceState: Bundle?) {
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
                                Snackbar.make(
                                    this.itemView,
                                    "click official",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                when (this.layoutPosition) {
                                    0 -> {
                                        mDatabind.rv.models = getData(true)
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
                                ChatActivity.launch(model.nickname, 1)
                            }
                            onClick(R.id.bg_new_friend) {
                                // todo
                                Snackbar.make(itemView, "new friend  ->  go add friend", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }

                    R.layout.item_friends_feed_empty -> {
                        getBinding<ItemFriendsFeedEmptyBinding>().btnAddFriend.setClickListener(
                            object : ShadowTxtButton.ShadowClickListener {
                                override fun clicked(disable: Boolean) {
                                    // todo
                                    Snackbar.make(itemView, "click empty  -> add friend", Snackbar.LENGTH_SHORT).show()
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
            actVm?.refreshIMData()
        }
        refresh()
    }


    fun refresh() {
        mDatabind.page.autoRefresh()
    }

    /************************* IM *******************************/
    private fun initIMListener() {
        // from server
        actVm?.conversationInfoObservable?.observe(this) { response ->
            mDatabind.page.finishRefresh()
            Log.e("MainRightFragment", "conversation from server " )
            parseResource(response, object : OnResourceParseCallback<List<EaseConversationInfo>>(true) {
                    override fun onSuccess(data: List<EaseConversationInfo>?) {
                        showConversationList(data)
                    }
                })
        }

        // from db cache
        actVm?.conversationCacheObservable?.observe(this) {
            mDatabind.page.finishRefresh()
            Log.e("MainRightFragment", "conversation from db cache " )
            showConversationList(it)
        }
    }

    val imgUrl1 = "https://i.ibb.co/LPGHdr2/1.jpg"
    val imgUrl2 = "https://i.ibb.co/zHLQWSX/2.jpg"
    val imgUrl3 = "https://i.ibb.co/jbDJHqv/3.jpg"
    val imgUrl4 = "https://i.ibb.co/dpL36nY/4.jpg"
    val imgUrl5 = "https://i.ibb.co/YDDB33P/5.jpg"
    val imgUrl6 = "https://i.ibb.co/m6LvNLk/6.jpg"
    val imgList = arrayListOf(imgUrl1, imgUrl2, imgUrl3, imgUrl4, imgUrl5, imgUrl6)
    var count = 5

    private fun showConversationList(data: List<EaseConversationInfo>?) {
        var modelData = arrayListOf<Any>()

        modelData.add(HeaderModel(title = "Official"))
        modelData.add(OfficialModel(arrayListOf("Cora-Official", "King-Official")))
        modelData.add(HeaderModel(title = "Friends Feed"))

        if (data.isNullOrEmpty()) {
            modelData.add(FriendsEmptyModel())
        }else{
            var friendMessageList = arrayListOf<Any>()
            data?.also { list ->
                list.forEach { item ->
                    var imageUrl: String = imgList[count++ % 3]
                    var nickname: String = ""
                    var bgColor: String = "#ff0000"   // todo waiting for our end developer to implement
                    var num: String = ""

                    (item.info as? EMConversation)?.also {
                        val username = it.conversationId()
                        EaseIM.getInstance().userProvider?.also { provider ->
                            provider.getUser(username)?.also { user ->
                                val nickname = user.nickname
                                val avatar = user.avatar
                                val bgColor = ""   // todo waiting for our end developer to implement
                            }
                        }
                        val unread = it.unreadMsgCount  //  >99  ->  99+
                        nickname = username
                        num = if (unread > 99) "99+" else "$unread"
                    }
                    Log.e("TAG", "showConversationList: nickname=$nickname   num=$num")

                    friendMessageList.add(FriendMessageInfo(imageUrl, nickname, bgColor, num))
                }
                if (friendMessageList.size <= 6) {
                    friendMessageList.add(FriendsAddModel())
                }
            }
            modelData.add(FriendsModel(friendMessageList))
        }
        mDatabind.rv.models = modelData
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