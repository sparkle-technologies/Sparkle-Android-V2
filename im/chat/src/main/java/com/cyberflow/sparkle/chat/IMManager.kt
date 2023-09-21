package com.cyberflow.sparkle.chat

import android.app.Application
import android.util.Log
import com.cyberflow.base.BaseApp
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.SafeGlobalScope
import com.cyberflow.base.util.callback.IMActionResult
import com.cyberflow.base.util.callback.IMV2Callback
import com.cyberflow.sparkle.chat.common.db.DemoDbHelper
import com.cyberflow.sparkle.chat.common.repositories.EMChatManagerRepository
import com.cyberflow.sparkle.chat.common.repositories.EMClientRepository
import com.cyberflow.sparkle.chat.common.utils.PreferenceManager
import com.hyphenate.EMCallBack
import com.hyphenate.easeui.ui.dialog.ThreadUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "IMManager"

class IMManager private constructor() {

    companion object {
        val instance: IMManager by lazy { IMManager() }
    }

    private var repository: EMClientRepository? = null
    private var mChatRepository: EMChatManagerRepository? = null

    fun init(app: Application) {
        PreferenceManager.init(app)
        if (DemoHelper.getInstance().autoLogin) {
            DemoHelper.getInstance().init(app)
        }
        repository = EMClientRepository().also {
            it.loadAllInfoFromHX()
        }
        mChatRepository = EMChatManagerRepository()
    }

    private var job: Job? = null

    fun login(callback: IMV2Callback<Boolean>) {
        job?.let {
            it.cancel()
        }
        val newJob = SupervisorJob().also { job = it }
        SafeGlobalScope.launch(newJob + Dispatchers.IO) {
            var openId = "war"   // given by server - its fake data for demo purposes  lover war

            val imAccount = CacheUtil.getString("imAccount")
            val imPwd = CacheUtil.getString("imPwd")
            Log.e(TAG, "im login: imAccount=$imAccount, imPwd=$imPwd" )
            repository?.also {
                it.login(imAccount, imPwd, object : IMV2Callback<IMActionResult> {
                    override fun onEvent(event: IMActionResult) {
                        var result = false
                        if(event is IMActionResult.Success){
                            DemoHelper.getInstance().autoLogin = true
                            val userName = DemoHelper.getInstance().model.currentUsername
                            DemoDbHelper.getInstance(BaseApp.instance).initDb(userName)

                            callback.onEvent(true)
                        }else{
                            // todo   ----  login failure
                        }
                        ThreadUtil.runOnMainThread(){
                            callback.onEvent(result)
                        }
                    }
                })
            }
        }
    }

    fun logout(callback: IMV2Callback<Boolean>) {
        DemoHelper.getInstance().logout(true, object : EMCallBack{
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
