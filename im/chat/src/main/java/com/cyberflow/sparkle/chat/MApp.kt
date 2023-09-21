package com.cyberflow.sparkle.chat

import com.cyberflow.base.BaseApp
import com.hyphenate.easeui.ui.dialog.LoadingDialog
import com.hyphenate.easeui.ui.dialog.LoadingDialogHolder

class MApp : BaseApp() {
    override fun onCreate() {
        super.onCreate()
        LoadingDialogHolder.setLoadingDialog(LoadingDialog.Companion)
    }
}