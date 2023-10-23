package com.cyberflow.sparkle.im.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.KeyboardUtil
import com.cyberflow.base.util.PageConst
import com.cyberflow.base.util.ToastUtil
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.chat.common.manager.PushAndMessageHelper
import com.cyberflow.sparkle.chat.viewmodel.parseResource
import com.cyberflow.sparkle.databinding.ActivityImForwardListBinding
import com.cyberflow.sparkle.databinding.ItemImContactBinding
import com.cyberflow.sparkle.databinding.ItemImForwardContactBinding
import com.cyberflow.sparkle.databinding.ItemImForwardContactSearchBinding
import com.cyberflow.sparkle.databinding.ItemImForwardRecentBinding
import com.cyberflow.sparkle.im.viewmodel.Contact
import com.cyberflow.sparkle.im.viewmodel.ContactLetter
import com.cyberflow.sparkle.im.viewmodel.ContactList
import com.cyberflow.sparkle.im.viewmodel.IMViewModel
import com.cyberflow.sparkle.im.viewmodel.RecentContactList
import com.cyberflow.sparkle.im.viewmodel.SearchContactList
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.spannable.replaceSpanFirst
import com.drake.spannable.span.ColorSpan
import com.hyphenate.chat.EMConversation
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo
import com.therouter.TheRouter
import com.therouter.router.Route


@Route(path = PageConst.IM.PAGE_IM_FORWARD)
class IMForwardListAct : BaseDBAct<IMViewModel, ActivityImForwardListBinding>() {


    private var mForwardMsgId = ""
    private fun forwardMsg(model: Contact) {
        Log.e(TAG, "forwardMsg: ${model.name} mForwardMsgId=$mForwardMsgId" )

        ToastUtil.show(this, "Message Sent!")
        PushAndMessageHelper.sendForwardMessage(model.name, mForwardMsgId)
//        finish()
    }

    override fun initView(savedInstanceState: Bundle?) {
        TheRouter.inject(this)
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
            addType<RecentContactList>(R.layout.item_im_forward_recent)
            addType<ContactList>(R.layout.item_im_forward_contact)
            addType<SearchContactList>(R.layout.item_im_forward_contact_search)
            onCreate {
                when (itemViewType) {
                    R.layout.item_im_forward_recent->{
                        getBinding<ItemImForwardRecentBinding>().rv.linear().setup {
                            addType<Contact>(R.layout.item_im_contact)
                            onBind {
                                findView<View>(R.id.line).visibility = if (layoutPosition == modelCount - 1) View.INVISIBLE else View.VISIBLE
                                getBinding<ItemImContactBinding>().item.setOnClickListener {
                                    val model = getModel<Contact>(layoutPosition)
                                    forwardMsg(model)
                                }
                            }
                        }
                    }
                    R.layout.item_im_forward_contact->{
                        getBinding<ItemImForwardContactBinding>().rv.linear().setup {
                            addType<Contact>(R.layout.item_im_contact)
                            addType<ContactLetter>(R.layout.item_im_contact_letter)
                            onBind {
                                if(itemViewType == R.layout.item_im_contact){
                                    findView<TextView>(R.id.tv_contact_name).text = getSpan(getModel<Contact>().name)
                                    val condition = getModel<Contact>().last || layoutPosition == modelCount - 1
                                    findView<View>(R.id.line).visibility = if ( condition ) View.INVISIBLE else View.VISIBLE
                                    getBinding<ItemImContactBinding>().item.setOnClickListener {
                                        val model = getModel<Contact>(layoutPosition)
                                        forwardMsg(model)
                                    }
                                }
                            }
                        }
                    }
                    R.layout.item_im_forward_contact_search->{
                        getBinding<ItemImForwardContactSearchBinding>().rv.linear().setup {
                            addType<Contact>(R.layout.item_im_contact)
                            onBind {
                                if(itemViewType == R.layout.item_im_contact){
                                    findView<TextView>(R.id.tv_contact_name).text = getSpan(getModel<Contact>().name)
                                    val condition = getModel<Contact>().last || layoutPosition == modelCount - 1
                                    findView<View>(R.id.line).visibility = if ( condition ) View.INVISIBLE else View.VISIBLE
                                    getBinding<ItemImContactBinding>().item.setOnClickListener {
                                        val model = getModel<Contact>(layoutPosition)
                                        forwardMsg(model)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            onBind {
                when (itemViewType) {
                    R.layout.item_im_forward_recent->{
                        val model = getModel<RecentContactList>()
                        getBinding<ItemImForwardRecentBinding>().rv.models = model.list
                    }
                    R.layout.item_im_forward_contact->{
                        val model = getModel<ContactList>()
                        getBinding<ItemImForwardContactBinding>().rv.models = model.list
                    }
                    R.layout.item_im_forward_contact_search->{
                        val model = getModel<SearchContactList>()
                        getBinding<ItemImForwardContactSearchBinding>().rv.models = model.list
                    }
                }
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

    override fun initData() {
        mForwardMsgId = intent.extras?.getString("forward_msg_id").toString()
        Log.e(TAG, "initData: mForwardMsgId=$mForwardMsgId" )
        freshData()
    }

    private fun freshData() {
        viewModel.conversationCacheObservable?.observe(this) {
            showConversationList(it)
        }
        viewModel.getConversationFromCache()

        viewModel.contactObservable.observe(this) { response ->
            parseResource(response, object : OnResourceParseCallback<List<EaseUser>>() {
                override fun onSuccess(data: List<EaseUser>?) {
                    showContactListData(data)
                }
            })
        }
        viewModel.loadContactList(true)   // load contact data from local db
    }

    private fun showConversationList(data: List<EaseConversationInfo>?) {
        recentData.clear()
        if (data.isNullOrEmpty()) {
             return
        }else{
            var conversactionList = arrayListOf<Contact>()
            data?.also { list ->
                list.forEach { item ->
                    var imageUrl: String = ""
                    var nickname: String = ""
                    (item.info as? EMConversation)?.also {
                        val username = it.conversationId()
                        EaseIM.getInstance().userProvider?.also { provider ->
                            provider.getUser(username)?.also { user ->
                                val nickname = user.nickname
                                val avatar = user.avatar
                                val bgColor = ""   // todo waiting for our end developer to implement
                            }
                        }
                        nickname = username
                    }
                    conversactionList.add(Contact(name = nickname))
                }
            }
            recentData.addAll(conversactionList)
            freshNormalUI()
        }

    }

    private val allContactData = arrayListOf<Contact>()
    private val recentData = arrayListOf<Contact>()
    private val contactData = arrayListOf<Any>()

    // show contact list
    private fun showContactListData(data: List<EaseUser>?) {
        allContactData.clear()
        contactData.clear()
        val list = arrayListOf<Any>()

        data?.forEach {
            Log.e(TAG, "  userName=${it.username}  initialLetter=${it.initialLetter}" )
        }
        val markArray = BooleanArray(array.size)  // handle letter missing problem
        data?.sortedBy {
            it.username
        }?.forEach {
            if (!markArray[array.indexOf(it.initialLetter)]) {

                // if last name of a letter, change it to true
                (list.lastOrNull() as? Contact)?.also {c->
                    c.last = true
                }

                list.add(ContactLetter(it.initialLetter))
                markArray[array.indexOf(it.initialLetter)] = true
            }
            Contact(name = it.username).apply {
                list.add(this)
                allContactData.add(this)
            }
        }

        if(list.isNotEmpty()){
            contactData.addAll(list)
            freshNormalUI()
        }
    }

    private fun freshNormalUI(){
        val allData = arrayListOf<Any>()
        allData.add(RecentContactList(recentData))
        allData.add(ContactList(contactData))
        mDataBinding.rv.models = allData
    }

    private fun freshSearchUI() {
        val name = mDataBinding.edtSearchContact.text.toString()
        inputTxt = name
        mDataBinding.rv.models = arrayListOf<Any>(SearchContactList(allContactData.filter {
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
        data.add(RecentContactList(arrayListOf(Contact(), Contact(), Contact(), Contact(), Contact())))
        data.add(ContactList(arrayListOf(ContactLetter("A"), Contact(), Contact(), Contact(), Contact(), Contact(), Contact(last = true), ContactLetter("B"),Contact(), Contact(),  Contact(), Contact(), Contact(last = true), ContactLetter("Z"), Contact(), Contact(),  Contact(), Contact(), Contact(last = true))))
        return data
    }

    private fun getCacheData(): SearchContactList = SearchContactList( list = arrayListOf(Contact(), Contact(), Contact(), Contact(), Contact()))
}
