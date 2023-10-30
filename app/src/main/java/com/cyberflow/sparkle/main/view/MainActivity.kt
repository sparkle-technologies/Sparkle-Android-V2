package com.cyberflow.sparkle.main.view

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.util.dp2px
import com.cyberflow.sparkle.DBComponent
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.common.db.entity.InviteMessageStatus
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.chat.common.utils.ChatPresenter
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
import com.cyberflow.sparkle.databinding.ActivityMainBinding
import com.cyberflow.sparkle.im.DBManager
import com.cyberflow.sparkle.im.db.IMUserInfo
import com.cyberflow.sparkle.im.view.IMContactListAct
import com.cyberflow.sparkle.im.view.IMSearchFriendAct
import com.cyberflow.sparkle.main.viewmodel.MainViewModel
import com.cyberflow.sparkle.main.viewmodel.parseResource
import com.cyberflow.sparkle.main.widget.DoubleClickListener
import com.cyberflow.sparkle.main.widget.NumView
import com.cyberflow.sparkle.profile.view.ProfileAct
import com.cyberflow.sparkle.register.view.PageAdapter
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.drake.net.utils.withMain
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent
import kotlinx.coroutines.launch

class MainActivity : BaseDBAct<MainViewModel, ActivityMainBinding>() {

    companion object {
        fun go(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var dis = 0f
    private var fromLeft2Right: ObjectAnimator? = null
    private var fromRight2Left: ObjectAnimator? = null
    private var lastLeft = false
    private var lastRight = false

    // the principle is : try to make logic clear, more code is acceptable
    private fun clickTopMenu(left: Boolean, right: Boolean, justTopAnima: Boolean = false) {
        if (left && right) return // avoid same click
        if (lastRight == right && lastLeft == left) return
        lastLeft = left
        lastRight = right

        if (left) {
            fromRight2Left?.start()
        }

        if (right) {
            fromLeft2Right?.start()
        }

        mDataBinding.ivMenuLeft.setImageResource((if (left) com.cyberflow.base.resources.R.drawable.svg_ic_horoscope_select else com.cyberflow.base.resources.R.drawable.svg_ic_horoscope_unselect))
        mDataBinding.ivMenuRight.setImageResource((if (right) com.cyberflow.base.resources.R.drawable.svg_ic_contact_select else com.cyberflow.base.resources.R.drawable.svg_ic_contact_unselect))
        if (justTopAnima) return
        if (left) goPrevious()
        if (right) goNext()
    }

    private val left: MainLeftFragment by lazy { MainLeftFragment() }
    private val right: MainRightFragment by lazy { MainRightFragment() }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.viewMenuLeft.setOnClickListener { clickTopMenu(true, false) }

        mDataBinding.viewMenuRight.setOnClickListener(object : DoubleClickListener() {
            override fun onDoubleClick() {
                right.pullToRefreshUI()
            }

            override fun onSingleClick() {
                clickTopMenu(false, true)
            }
        })

        mDataBinding.ivHead.setOnClickListener {
            ProfileAct.go(this, CacheUtil.getUserInfo()?.user?.open_uid.orEmpty())
        }

        mDataBinding.btnAddFriends.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                mDataBinding.layDialogAdd.apply {
                    visibility = if (this.visibility == View.VISIBLE) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }
            }
        })

        mDataBinding.layDialogAdd.apply {
            findViewById<View>(R.id.lay_add_friends).setOnClickListener {
                IMSearchFriendAct.go(this@MainActivity, viewModel.getContactList())
                mDataBinding.layDialogAdd.visibility = View.GONE
            }
            findViewById<View>(R.id.lay_contacts).setOnClickListener {
                IMContactListAct.go(this@MainActivity)
                mDataBinding.layDialogAdd.visibility = View.GONE
            }
        }

        var adapter = PageAdapter(supportFragmentManager, lifecycle)
        adapter.addFragment(left)
        adapter.addFragment(right)

        mDataBinding.pager.apply {
            offscreenPageLimit = 2
//            isUserInputEnabled = false
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            this.adapter = adapter
            registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (position == 0) clickTopMenu(true, false, true)
                    else clickTopMenu(false, true, true)
                }
            })
            setPageTransformer(MarginPageTransformer(100))
        }

        dis = dp2px(82f).toFloat()
        fromLeft2Right = ObjectAnimator.ofFloat(mDataBinding.bgMenuLeft, "translationX", 0f, dis)
        fromRight2Left = ObjectAnimator.ofFloat(mDataBinding.bgMenuLeft, "translationX", dis, 0f)

        clickTopMenu(true, false)
    }

    private fun goPrevious() {
        mDataBinding.pager.apply {
            if (currentItem > 0) {
                setCurrentItem(currentItem - 1, true)
            }
        }
    }

    private fun goNext() {
        mDataBinding.pager.apply {
            adapter?.also { a ->
                if (currentItem < a.itemCount - 1) {
                    setCurrentItem(currentItem + 1, true)
                }
            }
        }
    }

    override fun initData() {
        CacheUtil.getUserInfo()?.user?.apply {
            DBComponent.loadAvatar(mDataBinding.ivHead, avatar, gender)
        }

        loadIMConversations()
    }

    /********************* IM ***********************/

    private fun freshConversationData(event: EaseEvent?) {
        Log.e(TAG, "freshConversationData: event:${event?.event}  refresh:${event?.refresh}  message:${event?.message}", )
        event?.also {
            viewModel.freshConversationData()
        }
    }

    private fun freshContactData(event: EaseEvent?) {
        event?.also {
            viewModel.freshContactData()
        }
    }

    override fun onDestroy() {
        DBManager.instance.closeDB()
        super.onDestroy()
    }

    private fun loadIMConversations() {
        DBManager.instance.initDB(this)

        ChatPresenter.getInstance().init()  // chat global observer, like msg received , it should be called after login

        LiveDataBus.get().apply {
            with(DemoConstant.NOTIFY_CHANGE, EaseEvent::class.java).observe(this@MainActivity, this@MainActivity::freshConversationData)
            with(DemoConstant.MESSAGE_CHANGE_CHANGE, EaseEvent::class.java).observe(this@MainActivity, this@MainActivity::freshConversationData)
            with(DemoConstant.MESSAGE_FORWARD, EaseEvent::class.java).observe(this@MainActivity, this@MainActivity::freshConversationData)
            with(DemoConstant.CONVERSATION_READ, EaseEvent::class.java).observe(this@MainActivity, this@MainActivity::freshConversationData)

            with(DemoConstant.CONTACT_CHANGE, EaseEvent::class.java).observe(this@MainActivity, this@MainActivity::freshContactData)
            with(DemoConstant.CONTACT_ADD, EaseEvent::class.java).observe(this@MainActivity, this@MainActivity::freshContactData)
            with(DemoConstant.CONTACT_UPDATE, EaseEvent::class.java).observe(this@MainActivity, this@MainActivity::freshContactData)
        }

        viewModel.inviteMsgObservable.observe(this) { list ->
            list?.also {
                IMDataManager.instance.setInviteData(list)
                val res = it.filter { msg ->
                    val statusParam = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS)
                    val status = InviteMessageStatus.valueOf(statusParam)
                    status == InviteMessageStatus.BEINVITEED
                }
                val count = res.size

                val open_uid_list = res.map {
                    it.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM).replace("_", "-")
                }

                if(open_uid_list.isNotEmpty()){
                    viewModel.getIMNewFriendInfoList(open_uid_list)
                }

                mDataBinding.tvNum.apply {
                    if (it.isNotEmpty()) {
                        visibility = View.VISIBLE
                        setNum(count)
                    } else {
                        visibility = View.INVISIBLE
                    }
                }
                mDataBinding.layDialogAdd.findViewById<NumView>(R.id.tv_num).apply {
                    if (it.isNotEmpty()) {
                        visibility = View.VISIBLE
                        setNum(count)
                    } else {
                        visibility = View.INVISIBLE
                    }
                }
            }
        }

        viewModel.contactObservable?.observe(this){ response ->
            parseResource(response, object : OnResourceParseCallback<List<EaseUser>>() {
                override fun onSuccess(data: List<EaseUser>?) {
                    IMDataManager.instance.setContactData(data)
                    Log.e("MainActivity", "contact from server size: ${data?.size}" )
                    contactsBatchRequestInfo(data)
                }
            })
        }

        viewModel.imNewFriendListData?.observe(this){ infoList->
            saveToLocalDB(infoList.user_info_list, true)
        }

        viewModel.imContactListData?.observe(this){ infoList->
            saveToLocalDB(infoList.user_info_list, true)
        }

        viewModel.homeUnReadObservable.observe(this) {
            if (it.isNotEmpty()) {
                Log.e("MainActivity", "UnRead Count: $it")   // todo :  PM not decide yet
            }
        }

        viewModel.freshContactData()
    }

    private fun saveToLocalDB(userInfoList: List<IMUserInfo>?, needFetchConversation: Boolean = false) {
        Log.e(TAG, "saveToLocalDB: isContactData=$needFetchConversation", )
        if(userInfoList.isNullOrEmpty()){
            return
        }
        lifecycleScope.launch {
            DBManager.instance.db?.imUserInfoDao()?.insert(*userInfoList.toTypedArray())
            if(needFetchConversation){
                withMain {
                    freshConversationData(EaseEvent())
                }
            }
        }
    }

    // get data from server then save into local db
    private fun contactsBatchRequestInfo(data: List<EaseUser>?){
        Log.e(TAG, "batchRequest:  size: ${data?.size}", )
        val open_uid_list = data?.map {
            it.username.replace("_", "-")
        }?.filter { it.length > 20 }?.toList()
        if(open_uid_list.isNullOrEmpty()){
            Log.e(TAG, "contactsBatchRequestInfo: open_uid_list isNullOrEmpty" )
            right.showEmpty()  // no friends
            return
        }
        viewModel.getIMContactInfoList(open_uid_list)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val home = Intent(Intent.ACTION_MAIN)
            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            home.addCategory(Intent.CATEGORY_HOME)
            startActivity(home)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}