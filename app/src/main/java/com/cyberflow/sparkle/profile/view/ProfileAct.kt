package com.cyberflow.sparkle.profile.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.PageConst
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ActivityProfileBinding
import com.cyberflow.sparkle.im.DBManager
import com.cyberflow.sparkle.main.view.MainProfileFragment
import com.drake.net.utils.withMain
import com.therouter.router.Route
import kotlinx.coroutines.launch

@Route(path = PageConst.App.PAGE_PROFILE)
class ProfileAct : BaseDBAct<BaseViewModel, ActivityProfileBinding>() {

    companion object {
        const val OPEN_UID = "open_uid"

        const val CHAT = 0
        const val ADD_FRIEND = 1
        const val ACCEPT_FRIEND = 2

        fun go(context: Context, openUid: String) {
            val intent = Intent(context, ProfileAct::class.java)
            intent.putExtra(OPEN_UID, openUid)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

    }

    private var action = ADD_FRIEND
    private var open_uid = ""

    override fun initData() {
        intent.getStringExtra(OPEN_UID)?.apply {
            open_uid = this.replace("_", "-")
        }

        lifecycleScope.launch{
            val conversationCache = DBManager.instance.db?.imConversationCacheDao()?.getAll()
            val friendRequestCache = DBManager.instance.db?.imFriendRequestDao()?.getAll()
            val conversation = conversationCache?.filter {
                it.open_uid == open_uid
            }
            val request = friendRequestCache?.filter {
                it.from_open_uid == open_uid
            }
            if(conversation?.isNotEmpty() == true){
                action = CHAT
            }
            if(request?.isNotEmpty() == true){
                action = ACCEPT_FRIEND
            }
            withMain {
                val fragment = MainProfileFragment()
                fragment.setOpenUid(action, open_uid)
                supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
            }
        }
    }

    fun refresh(openUid: String?, action: Int){
        openUid?.also {
            val fragment = MainProfileFragment()
            fragment.setOpenUid(action, it)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}
