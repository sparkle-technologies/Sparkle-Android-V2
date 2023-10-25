package com.cyberflow.sparkle.im.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.KeyboardUtil
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.sparkle.DBComponent.loadImage
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.common.db.entity.InviteMessageStatus
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.chat.viewmodel.parseResource
import com.cyberflow.sparkle.databinding.ActivityImContactListBinding
import com.cyberflow.sparkle.databinding.ItemImContactBinding
import com.cyberflow.sparkle.databinding.ItemImContactBodyBinding
import com.cyberflow.sparkle.databinding.ItemImRequestBinding
import com.cyberflow.sparkle.databinding.ItemImRequestBodyBinding
import com.cyberflow.sparkle.databinding.ItemImRequestHeaderBinding
import com.cyberflow.sparkle.databinding.ItemImRequestHeaderEmptyBinding
import com.cyberflow.sparkle.im.DBManager
import com.cyberflow.sparkle.im.db.IMUserInfo
import com.cyberflow.sparkle.im.viewmodel.Contact
import com.cyberflow.sparkle.im.viewmodel.ContactLetter
import com.cyberflow.sparkle.im.viewmodel.ContactList
import com.cyberflow.sparkle.im.viewmodel.ContactListHeader
import com.cyberflow.sparkle.im.viewmodel.FriendRequest
import com.cyberflow.sparkle.im.viewmodel.FriendRequestEmpty
import com.cyberflow.sparkle.im.viewmodel.FriendRequestHeader
import com.cyberflow.sparkle.im.viewmodel.FriendRequestList
import com.cyberflow.sparkle.im.viewmodel.IMViewModel
import com.cyberflow.sparkle.im.viewmodel.STATUS_ADDED
import com.cyberflow.sparkle.im.viewmodel.STATUS_NORMAL
import com.cyberflow.sparkle.im.viewmodel.STATUS_REJECTED
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.utils.withMain
import com.drake.spannable.replaceSpanFirst
import com.drake.spannable.span.ColorSpan
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent
import kotlinx.coroutines.launch


class IMContactListAct : BaseDBAct<IMViewModel, ActivityImContactListBinding>() {

    companion object {
        fun go(context: Context) {
            val intent = Intent(context, IMContactListAct::class.java)
            context.startActivity(intent)
        }
    }

    override fun onBackPressed() {
        if(mDataBinding.tvTitle.text == getString(com.cyberflow.base.resources.R.string.contacts)){
            super.onBackPressed()
        }else{
            switchToSeeMoreOrNormal(false)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.llBack.setOnClickListener {
            onBackPressed()
        }
        mDataBinding.ivClear.setOnClickListener {
            mDataBinding.edtSearchContact.setText("")
        }
        mDataBinding.edtSearchContact.apply {
            setOnEditorActionListener { textView, i, keyEvent ->
                if(i == EditorInfo.IME_ACTION_SEARCH){

                    true
                }
                false
            }
            addTextChangedListener {
                if (it.isNullOrEmpty()) {
                    mDataBinding.ivClear.visibility = View.INVISIBLE
                    freshNormalUI()
                    inputTxt = ""
                } else {
                    mDataBinding.ivClear.visibility = View.VISIBLE
                    freshSearchUI()
                }
            }
            setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) return@setOnFocusChangeListener

                post {
                    KeyboardUtil.show(this)
                }
            }
        }
        mDataBinding.rv.linear().setup {
            addType<FriendRequestEmpty>(R.layout.item_im_request_header_empty)
            addType<FriendRequestHeader>(R.layout.item_im_request_header)
            addType<FriendRequestList>(R.layout.item_im_request_body)
            addType<ContactListHeader>(R.layout.item_im_contact_header)
            addType<ContactList>(R.layout.item_im_contact_body)
            onCreate {
                when (itemViewType) {
                    R.layout.item_im_request_body->{
                        getBinding<ItemImRequestBodyBinding>().rv.linear().setup {
                            addType<FriendRequest>(R.layout.item_im_request)
                            onBind {
                                getBinding<ItemImRequestBinding>().apply {
                                    val model = getModel<FriendRequest>()
                                    loadImage(ivHead, model.url)
                                    tvFriendName.text = model.name
                                    tvMsg.text = model.msg
                                    line.visibility = if (layoutPosition == modelCount - 1) View.INVISIBLE else View.VISIBLE
                                    btnAccept.setClickListener(object : ShadowTxtButton.ShadowClickListener{
                                        override fun clicked(disable: Boolean) {
                                            viewModel.acceptFriend(model.emMessage)
                                        }
                                    })
                                    layDelete.setOnClickListener {
                                        viewModel.deleteMessage(model.emMessage?.msgId)
                                    }
                                }
                            }
                        }
                    }
                    R.layout.item_im_contact_body->{
                        getBinding<ItemImContactBodyBinding>().rv.linear().setup {
                            addType<Contact>(R.layout.item_im_contact)
                            addType<ContactLetter>(R.layout.item_im_contact_letter)
                            onBind {
                                if(itemViewType == R.layout.item_im_contact){
                                    getBinding<ItemImContactBinding>().apply {
                                        val model = getModel<Contact>()
                                        tvContactName.text = if(isSearchModel()) getSpan(model.name) else model.name
                                        val condition = model.last || layoutPosition == modelCount - 1
                                        line.visibility = if ( condition ) View.INVISIBLE else View.VISIBLE
                                        loadImage(ivHead, model.avatar)
                                        item.setOnClickListener {
//                                        ChatActivity.launch(this@IMContactListAct, model.name, 1)  // go chat activity
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            onBind {
                when (itemViewType) {
                    R.layout.item_im_request_header_empty->{
                        getBinding<ItemImRequestHeaderEmptyBinding>().item.setOnClickListener {
                            switchToSeeMoreOrNormal(true)
                        }
                    }
                    R.layout.item_im_request_header ->{
                        val model = getModel<FriendRequestHeader>()
                        getBinding<ItemImRequestHeaderBinding>().apply {
                            tvCount.setNum(model.count)
                            laySeeMore.setOnClickListener {
                                switchToSeeMoreOrNormal(true)
                            }
                        }
                    }
                    R.layout.item_im_request_body->{
                        val model = getModel<FriendRequestList>()
                        getBinding<ItemImRequestBodyBinding>().rv.models = model.list
                    }
                    R.layout.item_im_contact_body->{
                        val model = getModel<ContactList>()
                        getBinding<ItemImContactBodyBinding>().rv.models = model.list
                    }
                }
            }
        }

        mDataBinding.rvCache.linear().setup {
            addType<FriendRequest>(R.layout.item_im_request)
            onBind {
                findView<View>(R.id.line).visibility = if (layoutPosition == modelCount - 1) View.INVISIBLE else View.VISIBLE
                findView<LinearLayout>(R.id.lay_delete).setOnClickListener {
                    val model = getModel<FriendRequest>(layoutPosition)
                    viewModel.deleteMessage(model.emMessage?.msgId)
                }
                findView<ShadowTxtButton>(R.id.btn_accept).setClickListener(object : ShadowTxtButton.ShadowClickListener{
                    override fun clicked(disable: Boolean) {
                        val model = getModel<FriendRequest>(layoutPosition)
                        viewModel.acceptFriend(model.emMessage)
                    }
                })
            }
        }
    }

    private var inputTxt = ""
    private fun getSpan(txt: String): CharSequence {
        if(inputTxt.isNullOrEmpty()) return ""

        return txt.replaceSpanFirst(inputTxt, ignoreCase = true){
            ColorSpan("#8B82DB")
        }
    }

    private fun isSearchModel() : Boolean{
        return inputTxt.isNotEmpty()
    }

    private fun switchToSeeMoreOrNormal(more: Boolean) {
        Log.e(TAG, "switchToSeeMoreOrNormal:  more: $more" )
        mDataBinding.layMain.visibility = if (more) View.GONE else View.VISIBLE
        mDataBinding.state.visibility = if (more) View.VISIBLE else View.GONE
        mDataBinding.tvTitle.text = if (more) getString(com.cyberflow.base.resources.R.string.friend_request) else getString(com.cyberflow.base.resources.R.string.contacts)
    }

    override fun initData() {
        initListener()
        loadLocalDBData(null)
    }

    // for event listener
    private fun loadLocalDBData(easeEvent: EaseEvent?) {
        lifecycleScope.launch {
            DBManager.instance.db?.imUserInfoDao()?.getAll()?.forEach {
                it.open_uid = it.open_uid.replace("-", "_")
                map[it.open_uid] = it
            }
            withMain {
                freshData()
            }
        }
    }

    private fun initListener(){
        viewModel.deleteMsgObservable.observe(this){
            freshData()
        }
        viewModel.acceptFriendObservable.observe(this){
            if(!it.isNullOrEmpty()){
//                ChatActivity.launch(this@IMContactListAct, it, 1)
            }
            freshData()
        }

        LiveDataBus.get().apply {
            with(DemoConstant.NOTIFY_CHANGE, EaseEvent::class.java).observe(this@IMContactListAct, this@IMContactListAct::loadLocalDBData)
            with(DemoConstant.MESSAGE_CHANGE_CHANGE, EaseEvent::class.java).observe(this@IMContactListAct, this@IMContactListAct::loadLocalDBData)
            with(DemoConstant.CONTACT_CHANGE, EaseEvent::class.java).observe(this@IMContactListAct, this@IMContactListAct::loadLocalDBData)
        }

        viewModel.inviteMsgObservable.observe(this) {inviteList->
            requestData.clear()
            allRequestData.clear()

            if(inviteList.isEmpty()){
                requestData.add(FriendRequestEmpty())
                freshNormalUI()

                freshMoreUI()
                return@observe
            }

            var list = arrayListOf<FriendRequest>()
            inviteList.forEach { msg ->
                try {
                    val name = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
                    var reason = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_REASON)

                    val statusParam = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS)
                    val status = InviteMessageStatus.valueOf(statusParam)

//                    Log.e(TAG, "getContactData: name: $name reason: $reason status: $status")  // name: lover2 reason: 加个好友呗 status: BEINVITEED

                    if(map.contains(name)){
                        map[name]?.also {
                            val friend = FriendRequest(name = it.nick, msg = reason, status = STATUS_NORMAL, url= it.avatar, emMessage = msg)
                            val cacheFriend = FriendRequest(name = it.nick, msg = reason, status = STATUS_NORMAL,url= it.avatar, emMessage = msg)

                            if (status == InviteMessageStatus.BEINVITEED) {  // only show friend request
                                list.add(friend)
                            } else {
                                if (status == InviteMessageStatus.AGREED) {
                                    cacheFriend.status = STATUS_ADDED
                                }
                                if (status == InviteMessageStatus.BEREFUSED || status == InviteMessageStatus.REFUSED) {
                                    cacheFriend.status = STATUS_REJECTED
                                }
                            }
                            allRequestData.add(cacheFriend)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            list.forEach {
                Log.e(TAG, "freshData: $it" )
            }

            if(list.isNotEmpty()){
                requestData.add(FriendRequestHeader(list.size))
                requestData.add(FriendRequestList("", list.take(2)))  // show no more than two friend request
            }else{
                requestData.add(FriendRequestEmpty())
            }

            freshNormalUI()
            freshMoreUI()
        }

        viewModel.contactObservable.observe(this) { response ->
            parseResource(response, object : OnResourceParseCallback<List<EaseUser>>() {
                override fun onSuccess(data: List<EaseUser>?) {
                    showContactListData(data)
                }
            })
        }
    }

    private val allRequestData = arrayListOf<FriendRequest>()
    private fun freshData() {
        viewModel.loadFriendRequestMessages()  // load system message
        viewModel.loadContactList(true)   // load contact data from local db
    }


    private val map = HashMap<String, IMUserInfo>()

    private val allContactData = arrayListOf<Contact>()
    // show contact list
    private fun showContactListData(data: List<EaseUser>?) {
        contactData.clear()
        allContactData.clear()
        val list = arrayListOf<Any>()

        data?.forEach {
            Log.e(TAG, "  userName=${it.username}  initialLetter=${it.initialLetter}" )
        }

        val markArray = BooleanArray(array.size)  // handle letter missing problem
        val letter = EaseUser.GetInitialLetter()
        data?.filter {
            map.contains( it.username )
        }?.mapNotNull {
            map[it.username]
        }?.sortedBy {
            it.nick
        }?.forEach {
            val initialLetter = letter.getLetter(it.nick)
            if (!markArray[array.indexOf(initialLetter)]) {
                // if last name of a letter, change it to true
                (list.lastOrNull() as? Contact)?.also {c->
                    c.last = true
                }

                list.add(ContactLetter(initialLetter))
                markArray[array.indexOf(initialLetter)] = true
            }

            Log.e(TAG, "showContactListData: ${it.nick}  ${it}" )

            Contact(name = it.nick, avatar = it.avatar).apply {
                list.add(this)
                allContactData.add(this)
            }
        }

        if(list.isNotEmpty()){
            contactData.add(ContactListHeader())
            contactData.add(ContactList(list))
            freshNormalUI()
        }
    }

    private val requestData = arrayListOf<Any>()
    private val contactData = arrayListOf<Any>()

    private fun freshNormalUI(){
        val allData = arrayListOf<Any>()
        allData.addAll(requestData)
        allData.addAll(contactData)
        mDataBinding.rv.models = allData
    }

    private fun freshMoreUI(){
//        if(allRequestData.isNullOrEmpty()) allRequestData.addAll(getCacheData())

        if(allRequestData.isNullOrEmpty()){
            mDataBinding.state.showEmpty()
        }else{
            mDataBinding.state.showContent()
            mDataBinding.rvCache.models = allRequestData
        }
    }

    private fun freshSearchUI() {
        val name = mDataBinding.edtSearchContact.text.toString()
        inputTxt = name
        mDataBinding.rv.models = arrayListOf<Any>(ContactList(allContactData.filter {
            it.name.contains(name, true)
        }))
    }

    private val array = arrayOf(
        "A",
        "B",
        "C",
        "D",
        "E",
        "F",
        "G",
        "H",
        "I",
        "J",
        "K",
        "L",
        "M",
        "N",
        "O",
        "P",
        "Q",
        "R",
        "S",
        "T",
        "U",
        "V",
        "W",
        "X",
        "Y",
        "Z",
        "#"
    )

    private fun getData() : List<Any>{
        val data = arrayListOf<Any>()

        data.add(FriendRequestEmpty())
        data.add(FriendRequestHeader())
        data.add(FriendRequestList("", arrayListOf(FriendRequest(),FriendRequest() )))

        data.add(ContactListHeader())
        data.add(ContactList(arrayListOf(ContactLetter("A"), Contact(), Contact(), Contact(), Contact(), Contact(), Contact(last = true), ContactLetter("B"),Contact(), Contact(),  Contact(), Contact(), Contact(last = true), ContactLetter("Z"), Contact(), Contact(),  Contact(), Contact(), Contact(last = true))))
        return data
    }

    private fun getCacheData(): List<FriendRequest>{
        val data = arrayListOf<FriendRequest>()
        data.add(FriendRequest(name = "Arc Chan", msg = "Hi,glad to see you, my name is xxxxxx, how are you and you im fine good to see you again", status = STATUS_NORMAL))
        data.add(FriendRequest(name = "Arc Chan", msg = "Hi,glad to see you", status = STATUS_NORMAL))
        data.add(FriendRequest(name = "Arc Chan", msg = "Hi,glad to see you", status = STATUS_ADDED))
        data.add(FriendRequest(name = "Arc Chan", msg = "Hi,glad to see you", status = STATUS_NORMAL))
        data.add(FriendRequest(name = "Arc Chan", msg = "Hi,glad to see you", status = STATUS_NORMAL))
        data.add(FriendRequest(name = "Arc Chan", msg = "Hi,glad to see you", status = STATUS_ADDED))
        data.add(FriendRequest(name = "Arc Chan", msg = "Hi,glad to see you", status = STATUS_ADDED))
        data.add(FriendRequest(name = "Arc Chan", msg = "Hi,glad to see you", status = STATUS_REJECTED))
        data.add(FriendRequest(name = "Arc Chan", msg = "Hi,glad to see you", status = STATUS_NORMAL))
        return data
    }
}
