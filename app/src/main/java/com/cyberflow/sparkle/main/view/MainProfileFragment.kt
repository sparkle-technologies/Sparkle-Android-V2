package com.cyberflow.sparkle.main.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.DBComponent
import com.cyberflow.sparkle.databinding.FragmentMainProfileBinding
import com.cyberflow.sparkle.mainv2.view.MainActivityV2
import com.cyberflow.sparkle.profile.view.ProfileAct
import com.hjq.language.LocaleContract
import com.hjq.language.MultiLanguages
import com.hjq.language.OnLanguageListener
import java.util.Locale

class MainProfileFragment : BaseDBFragment<BaseViewModel, FragmentMainProfileBinding>() {
    override fun initData() {

    }

    private val TAG = "MultiLanguages"

    override fun initView(savedInstanceState: Bundle?) {
        mDatabind.ivHead.setOnClickListener {
            ProfileAct.go(requireActivity(), CacheUtil.getUserInfo()?.user?.open_uid.orEmpty())
        }

        CacheUtil.getUserInfo()?.user?.apply {
            DBComponent.loadAvatar(mDatabind.ivHead, avatar, gender)
        }

        val current = MultiLanguages.getAppLanguage()  // 获取当前的语种
        Log.e(TAG, " current = $current" )

        mDatabind.tvHint.text = "current mode: $current"

        mDatabind.btn1.setOnClickListener {
            val restart = MultiLanguages.clearAppLanguage(requireActivity())
            restartApp(restart)
        }

        mDatabind.btn2.setOnClickListener {
            val restart = MultiLanguages.setAppLanguage(requireActivity(), LocaleContract.getSimplifiedChineseLocale())
            restartApp(restart)
        }

        mDatabind.btn3.setOnClickListener {
            val restart = MultiLanguages.setAppLanguage(requireActivity(), LocaleContract.getEnglishLocale())
            restartApp(restart)
        }

        MultiLanguages.setOnLanguageListener(object : OnLanguageListener {
            override fun onAppLocaleChange(oldLocale: Locale?, newLocale: Locale?) {
                 mDatabind.tvHint.text = "监听应用切换了语言    老的: $oldLocale, 新的: $newLocale"
            }

            override fun onSystemLocaleChange(oldLocale: Locale?, newLocale: Locale?) {
                mDatabind.tvHint.text = "监听系统切换了语言    老的: $oldLocale, 新的: $newLocale"
            }
        })
    }

    private fun restartApp(restart: Boolean){
        Log.e(TAG, "restartApp: restart=$restart" )
        if(restart){
            requireActivity().apply {
                startActivity(Intent(requireActivity(), MainActivityV2::class.java))
                overridePendingTransition(com.cyberflow.base.resources.R.anim.activity_alpha_in, com.cyberflow.base.resources.R.anim.activity_alpha_out)
//                finish()
            }
        }
    }
}