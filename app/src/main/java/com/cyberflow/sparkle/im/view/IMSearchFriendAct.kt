package com.cyberflow.sparkle.im.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.net.Api
import com.cyberflow.base.util.KeyboardUtil
import com.cyberflow.sparkle.DBComponent.loadAvatar
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ActivityImSearchFriendBinding
import com.cyberflow.sparkle.databinding.ItemImSearchAddFriendsBodyBinding
import com.cyberflow.sparkle.databinding.ItemImSearchBinding
import com.cyberflow.sparkle.databinding.ItemImSearchHeaderBinding
import com.cyberflow.sparkle.databinding.ItemImSearchMyFriendsBodyBinding
import com.cyberflow.sparkle.im.db.IMMyFriendsList
import com.cyberflow.sparkle.im.db.IMSearchData
import com.cyberflow.sparkle.im.db.IMSearchFriendHead
import com.cyberflow.sparkle.im.db.IMUserSearchList
import com.cyberflow.sparkle.im.db.TYPE_ADD_FRIENDS
import com.cyberflow.sparkle.im.db.TYPE_MY_FRIENDS
import com.cyberflow.sparkle.im.viewmodel.IMViewModel
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Post
import com.drake.net.utils.scope
import com.drake.spannable.replaceSpanFirst
import com.drake.spannable.span.ColorSpan


class IMSearchFriendAct : BaseDBAct<IMViewModel, ActivityImSearchFriendBinding>() {

    companion object {
        private const val EXTRA_DATA = "contactData"
        fun go(context: Context, contactList: List<String>) {
            val intent = Intent(context, IMSearchFriendAct::class.java)
            intent.putStringArrayListExtra(EXTRA_DATA, ArrayList(contactList))
            context.startActivity(intent)
        }
    }

    private var inputTxt = ""
    private var contactList = ArrayList<String>()
    private fun getSpan(txt: String): CharSequence {
        if (inputTxt.isNullOrEmpty()) return ""
        return txt.replaceSpanFirst(inputTxt, ignoreCase = true) { ColorSpan("#8B82DB") }
    }

    private fun onItemClicked(data: IMSearchData) {
        mDataBinding.edtSearchFriend.apply {
            KeyboardUtil.hide(this)
        }
        IMAddFriendAct.go(this, data)
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.rv.linear().setup {
            addType<IMSearchFriendHead>(R.layout.item_im_search_header)
            addType<IMMyFriendsList>(R.layout.item_im_search_my_friends_body)
            addType<IMUserSearchList>(R.layout.item_im_search_add_friends_body)
            addType<String>(R.layout.item_im_search_no_result)
            onCreate {
                when (itemViewType) {
                    R.layout.item_im_search_my_friends_body -> {
                        getBinding<ItemImSearchMyFriendsBodyBinding>().rv.linear().setup {
                            addType<IMSearchData>(R.layout.item_im_search)
                            onBind {
                                val data = getModel<IMSearchData>()
                                val binding = getBinding<ItemImSearchBinding>()
                                handleView(binding, data, layoutPosition,modelCount, false)
                            }
                        }
                    }
                    R.layout.item_im_search_add_friends_body -> {
                        getBinding<ItemImSearchAddFriendsBodyBinding>().rv.linear().setup {
                            addType<IMSearchData>(R.layout.item_im_search)
                            onBind {
                                val data = getModel<IMSearchData>()
                                val binding = getBinding<ItemImSearchBinding>()
                                handleView(binding, data, layoutPosition, modelCount, true)
                            }
                        }
                    }
                }
            }
            onBind {
                when (itemViewType) {
                    R.layout.item_im_search_header -> {
                        getBinding<ItemImSearchHeaderBinding>().apply {
                            val model = getModel<IMSearchFriendHead>()
                            tvTitle.text = model.name
                            laySeeMore.visibility = if (model.showMore) View.VISIBLE else View.GONE
                        }
                    }
                    R.layout.item_im_search_my_friends_body -> {
                        val model = getModel<IMMyFriendsList>()
                        getBinding<ItemImSearchMyFriendsBodyBinding>().rv.models = model.list
                    }

                    R.layout.item_im_search_add_friends_body -> {
                        val model = getModel<IMUserSearchList>()
                        getBinding<ItemImSearchAddFriendsBodyBinding>().rv.models = model.list
                    }
                }
            }
        }

        mDataBinding.llBack.setOnClickListener {
            onBackPressed()
        }
        mDataBinding.tvSearch.setOnClickListener {
            searchFriends()
        }
        mDataBinding.ivClear.setOnClickListener {
            mDataBinding.edtSearchFriend.setText("")
        }
        mDataBinding.edtSearchFriend.apply {
            setOnEditorActionListener { textView, i, keyEvent ->
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    searchFriends()
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
            requestFocus()
        }
    }

    private fun handleView(binding: ItemImSearchBinding, data: IMSearchData, layoutPosition: Int, modelCount: Int,  add: Boolean) {
        binding.tvFriendResult.text = getSpan(data.nick)

        loadAvatar(binding.ivHead, data.avatar, data.gender)

        binding.line.visibility = if (layoutPosition == modelCount - 1) View.INVISIBLE else View.VISIBLE

        if(add){
            binding.tvAddress.visibility = View.VISIBLE
            binding.tvAdd.visibility = View.VISIBLE
            val address = if (data.wallet_address.isNullOrEmpty()) data.ca_wallet else data.wallet_address
            if (address.length > 5) {
                binding.tvAddress.text = "${address.substring(0, 5)}...${address.substring(address.length - 5, address.length)}"
            }
            binding.tvAdd.setClickListener(object : ShadowTxtButton.ShadowClickListener {
                override fun clicked(disable: Boolean) {
                    Log.e(TAG, "you choose:  $data")
                    onItemClicked(data)
                }
            })
        }else{
            binding.tvAddress.visibility = View.INVISIBLE
            binding.tvAdd.visibility = View.INVISIBLE
        }
    }

    override fun initData() {
        intent.getStringArrayListExtra(EXTRA_DATA)?.apply {
            contactList = this
        }

        Log.e(TAG, "initData: contactList=$contactList")

        viewModel.imUserListData?.observe(this) {
            if (it.list.isNullOrEmpty()) {
                mDataBinding.state.showEmpty()
            } else {
                it.list?.apply {
                    val data = arrayListOf<Any>()
                    // friends
                    // new friends
                    val friends = filter {
                        contactList.contains(it.open_uid)
                    }
                    val newFriends = filter {
                        !contactList.contains(it.open_uid)
                    }

                    val maxCountMyFriend = 3
                    val maxCountAddFriend = 4

                    if(friends.isNullOrEmpty()){
                        data.add(IMSearchFriendHead(name = getString(com.cyberflow.base.resources.R.string.my_friends), type = TYPE_MY_FRIENDS, showMore = false))
                        data.add("no result found")
                    }else{
                        data.add(IMSearchFriendHead(name = getString(com.cyberflow.base.resources.R.string.my_friends), type = TYPE_MY_FRIENDS, showMore = friends.size > maxCountMyFriend))
                        data.add(IMMyFriendsList(total = friends.size, list = friends.take(maxCountMyFriend)))
                    }

                    if(newFriends.isNullOrEmpty()){
                        data.add(IMSearchFriendHead(name = getString(com.cyberflow.base.resources.R.string.add_friends), type = TYPE_ADD_FRIENDS, showMore = false))
                        data.add("no result found")
                    }else{
                        data.add(IMSearchFriendHead(name = getString(com.cyberflow.base.resources.R.string.add_friends), type = TYPE_ADD_FRIENDS, showMore = newFriends.size > maxCountAddFriend))
                        data.add(IMUserSearchList(total = newFriends.size, list = newFriends.take(maxCountAddFriend)))
                    }
                    mDataBinding.state.showContent()
                    mDataBinding.rv.models = data
                }
            }
        }
    }

    // make fake data
    private fun searchFriends() {
        val query = mDataBinding.edtSearchFriend.text.toString().trim()
        Log.e(TAG, "searchFriends: query=$query")

        if (query.isNullOrEmpty()) {
            mDataBinding.state.showEmpty()
            return
        }
        inputTxt = query
        mDataBinding.state.showLoading()
        mDataBinding.state.scope {
            viewModel.imUserListData?.value = Post<IMUserSearchList>(Api.IM_USER_SEARCH) {
                json("keyword" to inputTxt)
            }.await()
        }
    }
}
