package com.cyberflow.sparkle.im.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.KeyboardUtil
import com.cyberflow.base.util.PageConst
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
            toastSuccess(getString(com.cyberflow.base.resources.R.string.message_sent))
            finish()
        }

        LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_SEND_ERROR, String::class.java).observe(this) {
            LoadingDialogHolder.getLoadingDialog()?.hide()
            toastError(getString(com.cyberflow.base.resources.R.string.send_message_error))
        }
    }

    /**
     * the message data store at IMDataManager.forwardMsg before show this dialog
     * if need forward msg, it only need    1.forwardMsg:EMMessage   2.model:Contact
     * if share a img at ShareAct, just create a EMMessage without mForwardMsgId , and then save image uri to IMDataManager.forwardImageUri
     * so, that's all, two usage for now
      */
    private fun forwardMsg(model: Contact) {
//        Log.e(TAG, "forwardMsg: name=${model.name}  openUid=${model.openUid}  mForwardMsgId=$mForwardMsgId" )
        dialog = ForwardDialog(this, model, object : ForwardDialog.Callback {
            override fun onSelected(ok: Boolean) {
                if(ok){
                    dialog?.onDestroy()
                    LoadingDialogHolder.getLoadingDialog()?.show(this@IMForwardListAct)
                    if(mForwardMsgId.isNullOrEmpty()){   // it means not forward, just share image
                        val imgUri = IMDataManager.instance.getForwardImageUri()
                        PushAndMessageHelper.sendImageMessage(model.openUid.replace("-", "_"), imgUri)
                    }else{
                        PushAndMessageHelper.sendForwardMessage(model.openUid.replace("-", "_"), mForwardMsgId)
                    }
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
        showConversationList()
    }

    private fun showConversationList() {
        lifecycleScope.launch {
            recentData.clear()
            DBManager.instance.db?.imConversationCacheDao()?.getAll()?.also {
                recentData.addAll(it.map {
                    Contact(name = it.nick, avatar = it.avatar, gender = it.gender, openUid = it.open_uid)
                })
                withMain {
                    freshNormalUI()
                }
            }
        }
    }

    private val allContactData = arrayListOf<Contact>()
    private val recentData = arrayListOf<Contact>()

    private fun freshNormalUI(){
        val allData = arrayListOf<Any>()
        allData.add(RecentContactList(recentData))
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
