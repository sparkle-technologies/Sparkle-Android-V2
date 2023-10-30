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
import com.cyberflow.sparkle.DBComponent.loadImage
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ActivityImSearchFriendBinding
import com.cyberflow.sparkle.databinding.ItemImSearchBinding
import com.cyberflow.sparkle.im.db.IMSearchData
import com.cyberflow.sparkle.im.db.IMUserSearchList
import com.cyberflow.sparkle.im.viewmodel.IMViewModel
import com.cyberflow.sparkle.profile.view.ProfileAct
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Post
import com.drake.net.utils.scope
import com.drake.spannable.replaceSpanFirst
import com.drake.spannable.span.ColorSpan
import com.vanniktech.ui.hideKeyboard


class IMSearchFriendAct : BaseDBAct<IMViewModel, ActivityImSearchFriendBinding>() {

    companion object {
        fun go(context: Context) {
            val intent = Intent(context, IMSearchFriendAct::class.java)
            context.startActivity(intent)
        }
    }

    private var inputTxt = ""
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
            addType<IMSearchData>(R.layout.item_im_search)
            onBind {
                val data = getModel<IMSearchData>()
                val binding = getBinding<ItemImSearchBinding>()
                binding.tvFriendResult.text = getSpan(data.nick)

                Log.e(TAG, "initView: ${data.avatar}" )

                loadImage(binding.ivHead, data.avatar)
                val address = if(data.wallet_address.isNullOrEmpty()) data.ca_wallet else data.wallet_address
                if(address.length > 5){
                    binding.tvAddress.text = "${address.substring(0, 5)}...${address.substring(address.length - 5, address.length)}"
                }
                binding.line.visibility = if (layoutPosition == modelCount - 1) View.INVISIBLE else View.VISIBLE
                binding.cardview.setOnClickListener {
                    hideKeyboard(mDataBinding.edtSearchFriend)
                    ProfileAct.go(this@IMSearchFriendAct, data.open_uid, ProfileAct.ADD_FRIEND)
                }
            }
            R.id.tv_add.onClick {
                val model = getModel<IMSearchData>()
                Log.e(TAG, "you choose:  $model")
                onItemClicked(model)
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
                if(i == EditorInfo.IME_ACTION_SEARCH){
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

    override fun initData() {
        viewModel.imUserListData?.observe(this){
            if(it.list.isNullOrEmpty()){
                mDataBinding.state.showEmpty()
            }else{
                mDataBinding.state.showContent()
                mDataBinding.rv.models = it.list
            }
        }
    }

    // make fake data
    private fun searchFriends() {
        val query = mDataBinding.edtSearchFriend.text.toString().trim()
        Log.e(TAG, "searchFriends: query=$query" )

        if(query.isNullOrEmpty()){
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
