package com.cyberflow.sparkle.profile.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.sparkle.databinding.ActivityShareBinding
import com.cyberflow.sparkle.profile.viewmodel.ProfileViewModel
import com.cyberflow.sparkle.profile.widget.ShareDialog
import com.cyberflow.sparkle.widget.ShadowImgButton

class ShareAct : BaseDBAct<ProfileViewModel, ActivityShareBinding>() {

    companion object {
        const val OPEN_UID = "open_uid"
        const val FRIEND_STATUS = "friend_status"

        fun go(context: Context, openUid: String, friendStatus: Int = 0) {
            val intent = Intent(context, ShareAct::class.java)
            intent.putExtra(OPEN_UID, openUid)
            intent.putExtra(FRIEND_STATUS, friendStatus)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.bgRoot.setOnClickListener {
            finish()
        }
        mDataBinding.btnDelete.setClickListener(object : ShadowImgButton.ShadowClickListener{
            override fun clicked() {
                 finish()
            }
        })

        dialog = ShareDialog(this, object : ShareDialog.Callback {
            override fun onTimeSelected(timestamp: Long) {

            }
        })
    }

    override fun initData() {

    }

    private var dialog : ShareDialog? = null

    override fun onResume() {
        super.onResume()
        dialog?.show()
    }

    override fun onDestroy() {
        dialog?.onDestroy()
        super.onDestroy()
    }


}
