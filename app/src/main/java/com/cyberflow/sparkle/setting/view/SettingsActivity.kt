package com.cyberflow.sparkle.setting.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.MyApp
import com.cyberflow.sparkle.databinding.ActivitySettingBinding
import com.cyberflow.sparkle.login.view.LoginAct
import com.cyberflow.sparkle.login.widget.ShadowTxtButton
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : BaseDBAct<BaseViewModel, ActivitySettingBinding>() {

    companion object {
        fun go(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }

    // 0. clear cache
    // 1. walletConnect
    // 2. twitter
    // 3. web3Auth
    // 4. unipass
    private fun logout() {

        sb.clear()
        if (loginMethod == "Twitter") {
            sb.append("exit twitter")
            sb.append("\n")
            freshUI()
            twitter()
        } else {
            sb.append("exit wallet-connect")
            sb.append("\n")
            freshUI()
            walletConnect()
        }

        sb.append("clear local cache")
        sb.append("\n")
        freshUI()
        CacheUtil.setUserInfo(null)
        CacheUtil.savaString(CacheUtil.LOGIN_METHOD, "")
        CacheUtil.savaString(CacheUtil.UNIPASS_PUBK, "")
        CacheUtil.savaString(CacheUtil.UNIPASS_PRIK, "")

        sb.append("waiting for 5 seconds then go login page.......")
        freshUI()

        Thread {
            Thread.sleep(1 * 1000)
            runOnUiThread {
                LoginAct.go(this)
                finish()
            }
        }.start()
    }

    var sb = StringBuilder("")

    private fun getInitInfo() {
        sb.append("userInfo: $userInfo")
        sb.append("\n")
        sb.append("bindList: $bindList")
        sb.append("\n")
        sb.append("wallets: $wallets")
        sb.append("\n")
        sb.append(other)
        sb.append("\n")
        sb.append("loginMethod: $loginMethod")
        sb.append("\n")
        sb.append("publicKey: $publicKey")
        sb.append("\n")
        sb.append("privateKey: $privateKey")
    }

    var userInfo = ""
    var bindList = ""
    var wallets = ""
    var other = ""
    var loginMethod = ""
    var publicKey = ""
    var privateKey = ""

    override fun initView(savedInstanceState: Bundle?) {
        CacheUtil.apply {
            getUserInfo()?.let {
                it.user?.let {
                    userInfo =
                        "name: ${it.nick}  \t gender: ${it.gender} \t birthdate: ${it.birthdate} \t birthtime: ${it.birth_time} \t birth_location: ${it.birthplace_info} \n open_uid: ${it.open_uid}"
                    it.bind_list?.first()?.let {
                        bindList = "type: ${it.type} \t nick: ${it.nick}"
                    }
                    wallets = "wallet_address: ${it.wallet_address} \t ca_wallet: ${it.ca_wallet}"
                    other =
                        "task_completed: ${it.task_completed} \t profile_permission: ${it.profile_permission} \t signature: ${it.signature}"
                }
            }
            getString(LOGIN_METHOD)?.let {
                loginMethod = it
            }
            getString(UNIPASS_PUBK)?.let {
                publicKey = it
            }
            getString(UNIPASS_PRIK)?.let {
                privateKey = it
            }
        }

        mDataBinding.btnLogout.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked(d: Boolean) {
                Toast.makeText(this@SettingsActivity, "ready logout now", Toast.LENGTH_LONG).show()
                logout()
            }
        })
        getInitInfo()
        freshUI()
    }

    private fun walletConnect() {
        MyApp.instance.checkWalletConnect()
        MyApp.instance.walletConnectKit?.disconnect {
            it.printStackTrace()
            Log.e(TAG, "clicked:  $it")

            sb.append("succeed exit wallet-connect")
            sb.append("\n")
            freshUI()
        }
    }

    private fun twitter() {
        FirebaseAuth.getInstance().signOut()
        sb.append("succeed exit twitter")
        sb.append("\n")
        freshUI()
    }

    private fun web3Auth() {

    }

    private fun freshUI() {
        mDataBinding.tv.text = sb.toString()
    }


    override fun initData() {

    }
}