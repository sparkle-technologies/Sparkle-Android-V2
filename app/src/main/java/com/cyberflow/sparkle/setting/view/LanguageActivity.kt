package com.cyberflow.sparkle.setting.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.databinding.ActivityLanguageBinding
import com.hjq.language.LocaleContract
import com.hjq.language.MultiLanguages


class LanguageActivity : BaseDBAct<BaseViewModel, ActivityLanguageBinding>() {

    companion object {
        fun go(context: Context) {
            val intent = Intent(context, LanguageActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.llBack.setOnClickListener {
            onBackPressed()
        }

        mDataBinding.layEnglish.setOnClickListener {
            selectedIv(mDataBinding.ivEnglish, true)
            selectedIv(mDataBinding.ivChinese, false)
            val restart = MultiLanguages.setAppLanguage(this, LocaleContract.getEnglishLocale())
            restartApp(restart)
        }
        mDataBinding.layChinese.setOnClickListener {
            selectedIv(mDataBinding.ivEnglish, false)
            selectedIv(mDataBinding.ivChinese, true)
            val restart = MultiLanguages.setAppLanguage(this, LocaleContract.getSimplifiedChineseLocale())
            restartApp(restart)
        }
    }

    private fun restartApp(restart: Boolean) {
        Log.e(TAG, "restartApp: restart=$restart")
        if (restart) {
           /* startActivity(Intent(this, SettingsActivity::class.java))
            overridePendingTransition(com.cyberflow.base.resources.R.anim.activity_alpha_in, com.cyberflow.base.resources.R.anim.activity_alpha_out)
            finish()*/
            restartApp(this)
        }
    }

    private fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        // 关闭当前的Activity
        if (context is Activity) {
            context.finish()
        }
    }

    private fun selectedIv(iv: ImageView, selected: Boolean) {
        iv.setImageResource(if (selected) com.cyberflow.base.resources.R.drawable.setting_ic_language_select else com.cyberflow.base.resources.R.drawable.setting_ic_language_unselect)
    }

    override fun initData() {
        val current = MultiLanguages.getAppLanguage()
        selectedIv(
            mDataBinding.ivEnglish,
            current.language.equals(LocaleContract.getEnglishLocale().language)
        )
        selectedIv(
            mDataBinding.ivChinese,
            current.language.equals(LocaleContract.getSimplifiedChineseLocale().language)
        )
    }
}