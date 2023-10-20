package com.cyberflow.sparkle.chat.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.annotation.RequiresApi
import com.cyberflow.base.act.BaseVBAct
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.chat.R
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.databinding.ActivityPreivewBinding
import com.cyberflow.sparkle.chat.ui.fragment.PreviewFragment
import com.hyphenate.easeui.constants.EaseConstant
import com.luck.picture.lib.entity.LocalMedia

class PreviewActivity : BaseVBAct<BaseViewModel, ActivityPreivewBinding>() {

    companion object {
        fun go(act: Activity, conversationId: String, chatType: Int, localMedia: LocalMedia) {

            Log.e(
                TAG,
                "go: conversationId=$conversationId \t chatType=$chatType localMedia=${localMedia.path}"
            )
            val intent = Intent(act, PreviewActivity::class.java)
            intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, conversationId)
            intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, chatType)
            intent.putExtra(EaseConstant.EXTRA_LOCAL_MEDIA, localMedia)
            act.startActivity(intent)
        }
    }

    private var conversationId: String = ""
    private var chatType: Int = 0
    private var localMedia: LocalMedia? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun initView(savedInstanceState: Bundle?) {
        intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_ID)?.apply {
            conversationId = this
        }
        chatType = intent.getIntExtra(EaseConstant.EXTRA_CHAT_TYPE, 0)
        localMedia = intent.parcelable<LocalMedia>(EaseConstant.EXTRA_LOCAL_MEDIA)

        initChatFragment()
    }

    private fun initChatFragment() {

        localMedia?.also {
            PreviewFragment().apply {
                setData(conversationId, chatType, it)
                supportFragmentManager.beginTransaction().replace(R.id.fl_fragment, this, "preview").commit()
            }
        }
    }

    override fun initData() {

    }

    override fun finish() {
        LiveDataBus.get().with(DemoConstant.MSG_LIST_FRESH_TO_LATEST).postValue("${System.currentTimeMillis()}")
        super.finish()
    }
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}
