package com.cyberflow.sparkle.im.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.model.IMUserInfo
import com.cyberflow.base.util.KeyboardUtil
import com.cyberflow.base.util.PageConst
import com.cyberflow.base.util.ToastUtil
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.sparkle.DBComponent.loadAvatar
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.common.manager.PushAndMessageHelper
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
import com.cyberflow.sparkle.databinding.ActivityImForwardListBinding
import com.cyberflow.sparkle.databinding.ItemImContactBinding
import com.cyberflow.sparkle.databinding.ItemImForwardContactBinding
import com.cyberflow.sparkle.databinding.ItemImForwardContactSearchBinding
import com.cyberflow.sparkle.databinding.ItemImForwardRecentBinding
import com.cyberflow.sparkle.im.DBManager
import com.cyberflow.sparkle.im.viewmodel.Contact
import com.cyberflow.sparkle.im.viewmodel.ContactLetter
import com.cyberflow.sparkle.im.viewmodel.ContactList
import com.cyberflow.sparkle.im.viewmodel.IMViewModel
import com.cyberflow.sparkle.im.viewmodel.RecentContactList
import com.cyberflow.sparkle.im.viewmodel.SearchContactList
import com.cyberflow.sparkle.im.widget.ForwardDialog
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.utils.withMain
import com.drake.spannable.replaceSpanFirst
import com.drake.spannable.span.ColorSpan
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.ui.dialog.LoadingDialogHolder
import com.therouter.TheRouter
import com.therouter.router.Route
import kotlinx.coroutines.launch


@Route(path = PageConst.IM.PAGE_IM_FORWARD)
class IMForwardListAct : BaseDBAct<IMViewModel, ActivityImForwardListBinding>() {

    private var dialog : ForwardDialog? = null

    private var mForwardMsgId = ""

    private fun setMsgCallBack(){
        LiveDataBus.get().with(DemoConstant.MESSAGE_FORWARD, EaseEvent::class.java).observe(this) {
            LoadingDialogHolder.getLoadingDialog()?.hide()
            ToastUtil.show(this@IMForwardListAct, "Message Sent!")
            finish()
        }

        LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_SEND_ERROR, String::class.java).observe(this) {
            LoadingDialogHolder.getLoadingDialog()?.hide()
            ToastUtil.show(this@IMForwardListAct, "Send Message error")
        }
    }
    private fun forwardMsg(model: Contact) {
        Log.e(TAG, "forwardMsg: name=${model.name}  openUid=${model.openUid}  mForwardMsgId=$mForwardMsgId" )
        dialog = ForwardDialog(this, mForwardMsgId,  model, object : ForwardDialog.Callback {
            override fun onSelected(ok: Boolean) {
                if(ok){
                    dialog?.onDestroy()
                    LoadingDialogHolder.getLoadingDialog()?.show(this@IMForwardListAct)
                    PushAndMessageHelper.sendForwardMessage(model.openUid.replace("-", "_"), mForwardMsgId)
                }else{
                    dialog?.onDestroy()
                }
            }
        })
        dialog?.show()
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
                                val model = getModel<Contact>(layoutPosition)
                                getBinding<ItemImContactBinding>().apply {
                                    line.visibility = if (layoutPosition == modelCount - 1) View.INVISIBLE else View.VISIBLE
                                    loadAvatar(ivHead, model.avatar, model.gender)
                                    item.setOnClickListener {
                                        forwardMsg(model)
                                    }
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
                                    val model = getModel<Contact>(layoutPosition)
                                    getBinding<ItemImContactBinding>().apply {
                                        tvContactName.text = getSpan(getModel<Contact>().name)
                                        val condition = model.last || layoutPosition == modelCount - 1
                                        line.visibility = if ( condition ) View.INVISIBLE else View.VISIBLE
                                        loadAvatar(ivHead, model.avatar, model.gender)
                                        item.setOnClickListener {
                                            val model = getModel<Contact>(layoutPosition)
                                            forwardMsg(model)
                                        }
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
                                    val model = getModel<Contact>(layoutPosition)
                                    getBinding<ItemImContactBinding>().apply {
                                        tvContactName.text = getSpan(model.name)
                                        val condition = getModel<Contact>().last || layoutPosition == modelCount - 1
                                        line.visibility = if ( condition ) View.INVISIBLE else View.VISIBLE
                                        loadAvatar(ivHead, model.avatar, model.gender)
                                        item.setOnClickListener {
                                            forwardMsg(model)
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
        setMsgCallBack()
        showCacheData()
    }

    private fun showCacheData() {
        lifecycleScope.launch {
            DBManager.instance.db?.imUserInfoDao()?.getAll()?.forEach {
                it.open_uid = it.open_uid.replace("-", "_")
                map[it.open_uid] = it
            }
            withMain {
                showConversationList()
                showContactListData()
            }
        }
    }

    private fun showConversationList() {
        recentData.clear()
        val data = IMDataManager.instance.getConversationData()
        if (data.isNullOrEmpty()) {
             return
        }else{
            recentData.addAll(data.map {
                Contact(name = it.nick, avatar = it.avatar, gender = it.gender, openUid = it.open_uid)
            })
            freshNormalUI()
        }
    }

    private val allContactData = arrayListOf<Contact>()
    private val recentData = arrayListOf<Contact>()
    private val contactData = arrayListOf<Any>()

    private val map = HashMap<String, IMUserInfo>()

    // show contact list
    private fun showContactListData() {
        allContactData.clear()
        contactData.clear()
        val list = arrayListOf<Any>()
        val data = IMDataManager.instance.getContactData()

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

//            Log.e(TAG, "showContactListData: ${it.nick}  ${it}" )

            Contact(name = it.nick, avatar = it.avatar, gender = it.gender, openUid = it.open_uid).apply {
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
}
