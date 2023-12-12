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
import com.cyberflow.base.model.IMFriendInfo
import com.cyberflow.base.model.IMFriendRequest
import com.cyberflow.base.util.KeyboardUtil
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.sparkle.DBComponent
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
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
import com.cyberflow.sparkle.im.viewmodel.STATUS_NORMAL
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
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent
import com.vanniktech.ui.hideKeyboard
import kotlinx.coroutines.launch


/**
 *
 */
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
                                            viewModel.acceptFriend(model.openUid)
                                        }
                                    })
                                    layDelete.setOnClickListener {
                                        viewModel.deleteMessage(model.openUid)
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
                                            ProfileAct.go(this@IMContactListAct, model.openUid)
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
                        viewModel.deleteMessage(model.openUid)
                    }
                    btnAccept.setClickListener(object : ShadowTxtButton.ShadowClickListener{
                        override fun clicked(disable: Boolean) {
                            val model = getModel<FriendRequest>(layoutPosition)
                            viewModel.acceptFriend(model.openUid)
                        }
                    })
                }
            }
        }
    }

    private fun goProfile(model: FriendRequest){
        IMDataManager.instance.setOpenUidProfile(model.openUid)
        hideKeyboard(mDataBinding.edtSearchContact)
        ProfileAct.go(this@IMContactListAct, model.openUid)
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
        loadLocalDBData()
    }

    private var isFirst = true

    private fun freshData() {
        if(isFirst){
            IMDataManager.instance.apply {
                isFirst = false
                lifecycleScope.launch {
                    val contact = DBManager.instance.db?.imFriendInfoDao()?.getAll()
                    val invite = DBManager.instance.db?.imFriendRequestDao()?.getAll()
                    if(invite.isNullOrEmpty() && contact.isNullOrEmpty()){
                        freshData()
                    }else{
                        handleInviteMsg(invite.orEmpty())
                        showContactListData(contact)
                    }

                }
            }
        }else{
            viewModel.loadFriendRequestMessages()    // load system message
            viewModel.loadContactList()   // load contact data from local db
        }
    }


    // for event listener
    private fun receiveNewFriendRequestEvent(easeEvent: EaseEvent?) {
        lifecycleScope.launch {
            withMain {
                LiveDataBus.get().with(DemoConstant.FRESH_MAIN_IM_DATA).postValue(EaseEvent())
                freshData()
            }
        }
    }

    private fun loadLocalDBData() {
        lifecycleScope.launch {
            withMain {
                freshData()
            }
        }
    }

    private fun initListener(){
        viewModel.deleteMsgObservable.observe(this){
            ToastDialogHolder.getDialog()?.show(this@IMContactListAct, NotificationDialog.TYPE_SUCCESS, getString(R.string.friend_successfully_removed))
            LiveDataBus.get().with(DemoConstant.NOTIFY_CHANGE).postValue(EaseEvent())
            freshData()
        }
        viewModel.acceptFriendObservable.observe(this){
//            viewModel.IM_acceptFriend(it)
            freshData()
        }

        LiveDataBus.get().apply {
            with(DemoConstant.CONTACT_CHANGE, EaseEvent::class.java).observe(this@IMContactListAct, this@IMContactListAct::receiveNewFriendRequestEvent)
            with(DemoConstant.CONTACT_ADD, EaseEvent::class.java).observe(this@IMContactListAct, this@IMContactListAct::receiveNewFriendRequestEvent)
            with(DemoConstant.CONTACT_UPDATE, EaseEvent::class.java).observe(this@IMContactListAct, this@IMContactListAct::receiveNewFriendRequestEvent)
        }

        viewModel.inviteMsgObservable.observe(this) { inviteList->
            viewModel.saveInviteData2DB(inviteList.friend_req_list)
            handleInviteMsg(inviteList.friend_req_list.orEmpty())
        }

        viewModel.contactObservable.observe(this) {
            viewModel.saveContactData2DB(it.friend_list)
            showContactListData(it.friend_list)
        }
    }

    private fun handleInviteMsg(inviteList: List<IMFriendRequest>) {
        requestData.clear()
        allRequestData.clear()

        if(inviteList.isEmpty()){
            requestData.add(FriendRequestEmpty())
            freshNormalUI()

            freshMoreUI()
            return
        }

        val list = inviteList.map {
            FriendRequest(name = it.nick, msg = it.req_msg, status = STATUS_NORMAL, gender=0,  url= it.avatar, openUid= it.from_open_uid)
        }

        allRequestData.addAll(list)

        if(list.isNotEmpty()){
            requestData.add(FriendRequestHeader(list.size))
            requestData.add(FriendRequestList("", list.take(2)))  // show no more than two friend request
        }else{
            requestData.add(FriendRequestEmpty())
        }

        freshNormalUI()
        freshMoreUI()
    }


    private val allRequestData = arrayListOf<FriendRequest>()


    private val allContactData = arrayListOf<Contact>()
    // show contact list
    private fun showContactListData(data: List<IMFriendInfo>?) {
        contactData.clear()
        allContactData.clear()
        val list = arrayListOf<Any>()

        data?.forEach {
            Log.e(TAG, "  userName=${it.nick}  " )
        }

        val markArray = BooleanArray(array.size)  // handle letter missing problem
        val letter = EaseUser.GetInitialLetter()

        data?.sortedBy {
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

            Contact(name = it.nick, avatar = it.avatar, gender = 0, openUid = it.open_uid).apply {
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
