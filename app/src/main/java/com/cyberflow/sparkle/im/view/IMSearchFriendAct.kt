package com.cyberflow.sparkle.im.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.model.IMSearchData
import com.cyberflow.base.util.KeyboardUtil
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ActivityImSearchFriendBinding
import com.cyberflow.sparkle.im.viewmodel.IMViewModel
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.spannable.replaceSpanFirst
import com.drake.spannable.span.ColorSpan


class IMSearchFriendAct : BaseDBAct<IMViewModel, ActivityImSearchFriendBinding>() {

    companion object {
        fun go(context: Context) {
            val intent = Intent(context, IMSearchFriendAct::class.java)
            context.startActivity(intent)
        }
    }

    // make fake data
    private fun searchFriends() {
        val query = mDataBinding.edtSearchFriend.text.toString().trim()

        if(query.isNullOrEmpty()){
            mDataBinding.state.showEmpty()
            return
        }
        inputTxt = query
        val dataList = arrayListOf<IMSearchData>()
        dataList.add(IMSearchData(query, "", "0xbcc...ad23", 1))
        dataList.add(IMSearchData("${query}'s'", "", "0xbcc...ad23", 1))
        dataList.add(IMSearchData("fake${query}'data", "", "0xbcc...ad23", 1))
        mDataBinding.state.showContent()
        mDataBinding.rv.models = dataList
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
        mDataBinding.rv.visibility = View.INVISIBLE
        IMAddFriendAct.go(this, data)
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.rv.linear().setup {
            addType<IMSearchData>(R.layout.item_im_search)
            onBind {
                findView<TextView>(R.id.tv_friend_result).text = getSpan(getModel<IMSearchData>().name)
                findView<View>(R.id.line).visibility = if (layoutPosition == modelCount - 1) View.INVISIBLE else View.VISIBLE
            }
            R.id.tv_add.onClick {
                val model = getModel<IMSearchData>()
                notifyItemChanged(layoutPosition)
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

    }
}
