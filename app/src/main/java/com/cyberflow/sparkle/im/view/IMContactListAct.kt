package com.cyberflow.sparkle.im.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.KeyboardUtil
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ActivityImContactListBinding
import com.cyberflow.sparkle.databinding.ItemImContactBodyBinding
import com.cyberflow.sparkle.databinding.ItemImRequestBodyBinding
import com.cyberflow.sparkle.im.viewmodel.Contact
import com.cyberflow.sparkle.im.viewmodel.ContactLetter
import com.cyberflow.sparkle.im.viewmodel.ContactList
import com.cyberflow.sparkle.im.viewmodel.ContactListHeader
import com.cyberflow.sparkle.im.viewmodel.FriendRequest
import com.cyberflow.sparkle.im.viewmodel.FriendRequestEmpty
import com.cyberflow.sparkle.im.viewmodel.FriendRequestHeader
import com.cyberflow.sparkle.im.viewmodel.FriendRequestList
import com.cyberflow.sparkle.im.viewmodel.IMViewModel
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup


class IMContactListAct : BaseDBAct<IMViewModel, ActivityImContactListBinding>() {

    companion object {
        fun go(context: Context) {
            val intent = Intent(context, IMContactListAct::class.java)
            context.startActivity(intent)
        }
    }


    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.llBack.setOnClickListener {
            onBackPressed()
        }
        mDataBinding.tvSearch.setOnClickListener {

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
                } else {
                    mDataBinding.ivClear.visibility = View.VISIBLE
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
                                findView<View>(R.id.line).visibility = if (layoutPosition == modelCount - 1) View.INVISIBLE else View.VISIBLE
                            }
                        }
                    }
                    R.layout.item_im_contact_body->{
                        getBinding<ItemImContactBodyBinding>().rv.linear().setup {
                            addType<Contact>(R.layout.item_im_contact)
                            addType<ContactLetter>(R.layout.item_im_contact_letter)
                            onBind {
                                if(itemViewType == R.layout.item_im_contact){
                                    val condition = getModel<Contact>().last || layoutPosition == modelCount - 1
                                    findView<View>(R.id.line).visibility = if ( condition ) View.INVISIBLE else View.VISIBLE
                                }
                            }
                        }
                    }
                }
            }
            onBind {
                when (itemViewType) {
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
    }

    override fun initData() {
        mDataBinding.rv.models = getData()
    }

    private fun getData() : List<Any>{
        val data = arrayListOf<Any>()
        data.add(FriendRequestEmpty())
        data.add(FriendRequestHeader())
        data.add(FriendRequestList("", arrayListOf(FriendRequest(),FriendRequest() )))
        data.add(ContactListHeader())
        data.add(ContactList(arrayListOf(ContactLetter("A"), Contact(), Contact(), Contact(), Contact(), Contact(), Contact(last = true), ContactLetter("B"),Contact(), Contact(),  Contact(), Contact(), Contact(last = true), ContactLetter("Z"), Contact(), Contact(),  Contact(), Contact(), Contact(last = true))))
        return data
    }

}
