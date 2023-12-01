package com.cyberflow.sparkle.profile.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.PageConst
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ActivityProfileBinding
import com.cyberflow.sparkle.main.view.MainProfileFragment
import com.therouter.router.Route

@Route(path = PageConst.App.PAGE_PROFILE)
class ProfileAct : BaseDBAct<BaseViewModel, ActivityProfileBinding>() {

    companion object {
        const val OPEN_UID = "open_uid"
        const val FRIEND_STATUS = "friend_status"

        const val CHAT = 0
        const val ADD_FRIEND = 1
        const val ACCEPT_FRIEND = 2

        fun go(context: Context, openUid: String, friendStatus: Int = 0) {
            val intent = Intent(context, ProfileAct::class.java)
            intent.putExtra(OPEN_UID, openUid)
            intent.putExtra(FRIEND_STATUS, friendStatus)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

    }

    private var action = CHAT
    private var open_uid = ""

    override fun initData() {
        intent.getIntExtra(FRIEND_STATUS, CHAT).apply {
            action = this
            Log.e(TAG, "initData: action=$action" )
        }
        intent.getStringExtra(OPEN_UID)?.apply {
            open_uid = this.replace("_", "-")
        }
        val fragment = MainProfileFragment()
        fragment.setOpenUid(action, open_uid)
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()
    }

    fun refresh(openUid: String?, action: Int){
        openUid?.also {
            val fragment = MainProfileFragment()
            fragment.setOpenUid(action, it)
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }
}
