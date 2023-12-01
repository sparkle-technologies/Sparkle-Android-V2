package com.cyberflow.sparkle.im.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.model.IMUserInfo
import com.cyberflow.base.util.KeyboardUtil
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.sparkle.DBComponent
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.common.db.entity.InviteMessageStatus
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
import com.cyberflow.sparkle.chat.viewmodel.parseResource
import com.cyberflow.sparkle.databinding.ActivityImContactListBinding
import com.cyberflow.sparkle.databinding.ItemImContactBinding
import com.cyberflow.sparkle.databinding.ItemImContactBodyBinding
import com.cyberflow.sparkle.databinding.ItemImRequestBinding
import com.cyberflow.sparkle.databinding.ItemImRequestBodyBinding
import com.cyberflow.sparkle.databinding.ItemImRequestHeaderBinding
import com.cyberflow.sparkle.databinding.ItemImRequestHeaderEmptyBinding
import com.cyberflow.sparkle.im.DBManager
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
import com.cyberflow.sparkle.profile.view.ProfileAct
import com.cyberflow.sparkle.widget.NotificationDialog
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.cyberflow.sparkle.widget.ToastDialogHolder
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.utils.withMain
import com.drake.spannable.replaceSpanFirst
import com.drake.spannable.span.ColorSpan
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent
import com.vanniktech.ui.hideKeyboard
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

                                    DBComponent.loadAvatar(ivHead, model.url, model.gender)
                                    tvStatus.text = if (model.status == 1) getString(com.cyberflow.base.resources.R.string.accept) else getString(com.cyberflow.base.resources.R.string.refused)
                                    tvFriendName.text = model.name
                                    tvMsg.text = model.msg
                                    line.visibility = if (layoutPosition == modelCount - 1) View.INVISIBLE else View.VISIBLE

//                                    cardview.setOnClickListener {
//                                        goProfile(model)
//                                    }
                                    item.setOnClickListener {
                                        goProfile(model)
                                    }
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
                                        DBComponent.loadAvatar(ivHead, model.avatar, model.gender)
//                                        cardview.setOnClickListener {
//                                            ProfileAct.go(this@IMContactListAct, model.openUid, ProfileAct.CHAT)
//                                        }
                                        item.setOnClickListener {
                                            ProfileAct.go(this@IMContactListAct, model.openUid, ProfileAct.CHAT)
//                                            ChatActivity.launch(this@IMContactListAct, model.openUid, model.avatar, model.name, 1)  // go chat activity
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
                getBinding<ItemImRequestBinding>().apply {
                    val model = getModel<FriendRequest>()
                    line.visibility = if (layoutPosition == modelCount - 1) View.INVISIBLE else View.VISIBLE
                    cardview.setOnClickListener {
                        goProfile(model)
                    }
                    layDelete.setOnClickListener {
                        viewModel.deleteMessage(model.emMessage?.msgId)
                    }
                    btnAccept.setClickListener(object : ShadowTxtButton.ShadowClickListener{
                        override fun clicked(disable: Boolean) {
                            val model = getModel<FriendRequest>(layoutPosition)
                            viewModel.acceptFriend(model.emMessage)
                        }
                    })
                }
            }
        }
    }

    private fun goProfile(model: FriendRequest){
        val friendStatus = when(model.status){
            STATUS_NORMAL-> ProfileAct.ACCEPT_FRIEND
            STATUS_ADDED-> ProfileAct.CHAT
            STATUS_REJECTED-> ProfileAct.ADD_FRIEND
            else -> ProfileAct.ADD_FRIEND
        }
        IMDataManager.instance.setEmMessage(model.emMessage)
        hideKeyboard(mDataBinding.edtSearchContact)
        ProfileAct.go(this@IMContactListAct, model.openUid, friendStatus)
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

    private var isFirst = true
    private fun freshData() {
        if(isFirst){
            IMDataManager.instance.apply {
                isFirst = false
                val invite = getInviteData()
                val contact = getContactData()
                if(invite.isNullOrEmpty() && contact.isNullOrEmpty()){
                    freshData()
                }else{
                    handleInviteMsg(invite)
                    showContactListData(contact)
                }
            }
        }else{
            viewModel.loadFriendRequestMessages()    // load system message
            viewModel.loadContactList(true)   // load contact data from local db
        }
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
            if(it){
                ToastDialogHolder.getDialog()?.show(this@IMContactListAct, NotificationDialog.TYPE_SUCCESS, getString(R.string.friend_successfully_removed))
            }else{
                ToastDialogHolder.getDialog()?.show(this@IMContactListAct, NotificationDialog.TYPE_ERROR, getString(R.string.oops_failed_to_remove_friend))
            }
            freshData()
        }
        viewModel.acceptFriendObservable.observe(this){
            if(!it.isNullOrEmpty()){
//                ChatActivity.launch(this@IMContactListAct, it, 1)
            }
            freshData()
        }

        LiveDataBus.get().apply {
            with(DemoConstant.CONTACT_CHANGE, EaseEvent::class.java).observe(this@IMContactListAct, this@IMContactListAct::loadLocalDBData)
            with(DemoConstant.CONTACT_ADD, EaseEvent::class.java).observe(this@IMContactListAct, this@IMContactListAct::loadLocalDBData)
            with(DemoConstant.CONTACT_UPDATE, EaseEvent::class.java).observe(this@IMContactListAct, this@IMContactListAct::loadLocalDBData)
        }

        viewModel.inviteMsgObservable.observe(this) { inviteList->
            handleInviteMsg(inviteList)
        }

        viewModel.contactObservable.observe(this) { response ->
            parseResource(response, object : OnResourceParseCallback<List<EaseUser>>() {
                override fun onSuccess(data: List<EaseUser>?) {
                    showContactListData(data)
                }
            })
        }

        viewModel.imNewFriendListData.observe(this){
            saveToLocalDB(it.user_info_list, false)
        }
    }

    private fun handleInviteMsg(inviteList: List<EMMessage>) {
        requestData.clear()
        allRequestData.clear()

        if(inviteList.isEmpty()){
            requestData.add(FriendRequestEmpty())
            freshNormalUI()

            freshMoreUI()
            return
        }

        var list = arrayListOf<FriendRequest>()
        var newUser = arrayListOf<String>()
        inviteList.forEach { msg ->
            try {
                val name = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
                var reason = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_REASON)

                val statusParam = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS)
                val status = InviteMessageStatus.valueOf(statusParam)

//                    Log.e(TAG, "getContactData: name: $name reason: $reason status: $status")  // name: lover2 reason: 加个好友呗 status: BEINVITEED

                if(map.contains(name)){
                    map[name]?.also {
                        val friend = FriendRequest(name = it.nick, msg = reason, status = STATUS_NORMAL, gender=it.gender,  url= it.avatar, openUid= it.open_uid, emMessage = msg)
                        val cacheFriend = FriendRequest(name = it.nick, msg = reason, status = STATUS_NORMAL, gender=it.gender, url= it.avatar, openUid= it.open_uid, emMessage = msg)

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
                }else{
                    newUser.add(name)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

      /*  list.forEach {
            Log.e(TAG, "freshData: $it" )
        }
        newUser.forEach {
            Log.e(TAG, "freshData: $it" )
        }*/

        if(newUser.isNotEmpty()){
            viewModel.getIMNewFriendInfoList(newUser)
            return
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

    private fun saveToLocalDB(userInfoList: List<IMUserInfo>?, isContactData: Boolean = false) {
        Log.e(TAG, "saveToLocalDB: ", )
        if(userInfoList.isNullOrEmpty()){
            return
        }
        lifecycleScope.launch {
            DBManager.instance.db?.imUserInfoDao()?.insert(*userInfoList.toTypedArray())
            if(isContactData){
                withMain {
                    freshData()
                }
            }
        }
    }

    private val allRequestData = arrayListOf<FriendRequest>()

    private val map = HashMap<String, IMUserInfo>()

    private val allContactData = arrayListOf<Contact>()
    // show contact list
    private fun showContactListData(data: List<EaseUser>?) {
        contactData.clear()
        allContactData.clear()
        val list = arrayListOf<Any>()

//        data?.forEach {
//            Log.e(TAG, "  userName=${it.username}  initialLetter=${it.initialLetter}" )
//        }

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

//            Log.e(TAG, "showContactListData: ${it.nick}  ${it}" )

            Contact(name = it.nick, avatar = it.avatar, gender = it.gender, openUid = it.open_uid).apply {
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

}
