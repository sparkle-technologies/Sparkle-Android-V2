package com.cyberflow.sparkle.chat

import com.cyberflow.base.BaseApp
import com.google.firebase.FirebaseApp
import com.hyphenate.easeui.ui.dialog.LoadingDialog
import com.hyphenate.easeui.ui.dialog.LoadingDialogHolder
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider

class MApp : BaseApp() {
    override fun onCreate() {
        super.onCreate()
        LoadingDialogHolder.setLoadingDialog(LoadingDialog.Companion)
        EmojiManager.install(IosEmojiProvider())
        FirebaseApp.initializeApp(this)
    }
}