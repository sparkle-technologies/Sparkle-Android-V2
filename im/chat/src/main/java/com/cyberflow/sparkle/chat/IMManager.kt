package com.cyberflow.sparkle.chat

import android.app.Application
import android.util.Log
import androidx.annotation.UiThread
import com.cyberflow.base.BaseApp
import com.cyberflow.base.util.SafeGlobalScope
import com.cyberflow.base.util.callback.IMActionResult
import com.cyberflow.base.util.callback.IMLoginResponse
import com.cyberflow.base.util.callback.IMV2Callback
import com.cyberflow.sparkle.chat.common.db.DemoDbHelper
import com.cyberflow.sparkle.chat.common.repositories.EMChatManagerRepository
import com.cyberflow.sparkle.chat.common.repositories.EMClientRepository
import com.cyberflow.sparkle.chat.common.utils.PreferenceManager
import com.hyphenate.EMCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.easeui.ui.dialog.LoadingDialog
import com.hyphenate.easeui.ui.dialog.LoadingDialogHolder
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


// its the only
class IMManager private constructor() {

    companion object {
        val instance: IMManager by lazy { IMManager() }
        const val TAG = "IMManager"
        const val Account = "test5"
        const val Pwd = "123"
    }

    @UiThread
    fun initUI() {
        LoadingDialogHolder.setLoadingDialog(LoadingDialog.Companion)
        EmojiManager.install(IosEmojiProvider())
    }

    private var repository: EMClientRepository? = null
    private var mChatRepository: EMChatManagerRepository? = null

    fun initSDKAndDB(app: Application) {
        PreferenceManager.init(app)
        DemoHelper.getInstance().init(app)
        repository = EMClientRepository()
        mChatRepository = EMChatManagerRepository()

    }

    private var job: Job? = null

    fun loginToIM(imAccount: String, imPwd: String, callback: IMV2Callback<IMLoginResponse>) {
        job?.let {
            it.cancel()
        }
        val newJob = SupervisorJob().also { job = it }
        SafeGlobalScope.launch(newJob + Dispatchers.IO) {
            Log.e(TAG, "im login: imAccount=$imAccount, imPwd=$imPwd")
            repository?.also {
                it.login(imAccount, imPwd, object : IMV2Callback<IMActionResult> {
                    override fun onEvent(event: IMActionResult) {
                        if (event is IMActionResult.Success) {
//                            DemoHelper.getInstance().autoLogin = true

                            val userName = DemoHelper.getInstance().model.currentUsername
                            DemoDbHelper.getInstance(BaseApp.instance).initDb(userName)

                            callback.onEvent(IMLoginResponse(true))
                        } else {
                            if (event is IMActionResult.Failure) {
                                callback.onEvent(IMLoginResponse(false, event.msg))
                            } else {
                                callback.onEvent(IMLoginResponse(false))
                            }
                        }
                    }
                })
            }
        }
    }

    fun keepLogout() {
        Log.e(TAG, "keepLogout: ---0----isLoggedIn=${EMClient.getInstance().isLoggedIn}")
        if (EMClient.getInstance().isLoggedIn) {
            job?.let { it.cancel() }
            val newJob = SupervisorJob().also { job = it }
            SafeGlobalScope.launch(newJob + Dispatchers.IO) {
                EMClient.getInstance().logout(true)
                Log.e(TAG, "keepLogout: ---1----isLoggedIn=${EMClient.getInstance().isLoggedIn}")
            }
        }
    }

    fun logoutIM(callback: IMV2Callback<Boolean>) {
        DemoHelper.getInstance().logout(true, object : EMCallBack {
            override fun onSuccess() {
                DemoHelper.getInstance().model.phoneNumber = ""
                callback.onEvent(true)
            }

            override fun onError(code: Int, error: String?) {
                callback.onEvent(false)
            }
        })
    }
}
