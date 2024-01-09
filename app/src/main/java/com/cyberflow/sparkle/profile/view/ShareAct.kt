package com.cyberflow.sparkle.profile.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.ext.screenHeight
import com.cyberflow.base.ext.screenWidth
import com.cyberflow.base.model.AIOResult
import com.cyberflow.base.model.IMConversationCache
import com.cyberflow.base.model.ManyImageData
import com.cyberflow.base.model.TarotCard
import com.cyberflow.base.model.User
import com.cyberflow.base.net.Api
import com.cyberflow.base.net.GsonConverter
import com.cyberflow.base.net.GsonConverter.Companion.gson
import com.cyberflow.base.resources.R
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.ConstantGlobal
import com.cyberflow.base.util.PageConst
import com.cyberflow.base.util.ViewExt.convertViewToBitmap
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.util.dp2px
import com.cyberflow.sparkle.DBComponent
import com.cyberflow.sparkle.DBComponent.loadAvatarWithCornor
import com.cyberflow.sparkle.chat.DemoHelper
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.common.manager.PushAndMessageHelper
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
import com.cyberflow.sparkle.databinding.ActivityShareBinding
import com.cyberflow.sparkle.im.viewmodel.Contact
import com.cyberflow.sparkle.im.widget.ForwardDialog
import com.cyberflow.sparkle.profile.viewmodel.ShareViewModel
import com.cyberflow.sparkle.profile.widget.ShareDialog
import com.cyberflow.sparkle.widget.PermissionDialog
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.net.utils.withMain
import com.drake.spannable.addSpan
import com.drake.spannable.movement.ClickableMovementMethod
import com.drake.spannable.replaceSpan
import com.drake.spannable.setSpan
import com.drake.spannable.span.CenterImageSpan
import com.drake.spannable.span.ColorSpan
import com.drake.spannable.span.HighlightSpan
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsBuildBitmapOption
import com.huawei.hms.ml.scan.HmsScan
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMTextMessageBody
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.ui.dialog.LoadingDialogHolder
import com.luck.picture.lib.basic.PictureMediaScannerConnection
import com.luck.picture.lib.permissions.PermissionUtil
import com.luck.picture.lib.utils.DownloadFileUtils
import com.therouter.TheRouter
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

class ShareAct : BaseDBAct<ShareViewModel, ActivityShareBinding>(),
    EasyPermissions.PermissionCallbacks {

    companion object {

        const val SHARE_FROM_PROFILE = 1001
        const val SHARE_FROM_CHAT = 1002
        const val SHARE_FROM_COMPATIBILITY = 1003

        const val USER_AVATAR_URL = "server_img_url"
        const val FROM_ACTIVITY = "from_activity"

        const val REQUEST_DOWNLOAD = 233
        const val REQUEST_SHARE = 234

        fun go(context: Context, from: Int, serverImageUrl: String?) {
            val intent = Intent(context, ShareAct::class.java)
            intent.putExtra(FROM_ACTIVITY, from)
            intent.putExtra(USER_AVATAR_URL, serverImageUrl)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.bgRoot.setOnClickListener {
            Log.e(TAG, "bgRoot click")
            finish()
        }
        mDataBinding.bg.setOnClickListener {
            Log.e(TAG, "bg click")
            dialog?.hideOrShow()
        }

        mDataBinding.scroll.setOnScrollChangeListener { view, x, y, oldX, oldY ->
//            Log.e(TAG, "initView: (y-oldY)=${y-oldY}" )
            if (y - oldY > 3) {
                dialog?.justHide()
            }

            if (oldY - y > 3) {
                dialog?.justShow()
            }
        }
        mDataBinding.layChat.setOnClickListener {
            Log.e(TAG, "layChat click")
            dialog?.hideOrShow()
        }
        mDataBinding.layCompatibility.setOnClickListener {
            Log.e(TAG, "layCompatibility click")
            dialog?.hideOrShow()
        }
        mDataBinding.btnDelete.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                finish()
            }
        })
        dialog = ShareDialog(this@ShareAct, object : ShareDialog.Callback {
            override fun onSelected(user: IMConversationCache?, type: Int) {
                Log.e(TAG, "onSelected: type=$type  user=$user")
                when (type) {
                    ShareDialog.TYPE_SHARE -> {
                        isMore = false
                        shareUser = user
                        Log.e(TAG, "onSelected: isMore=$isMore shareUser=$shareUser")
                        share()
                    }

                    ShareDialog.TYPE_MORE -> {
                        isMore = true
                        shareUser = null
                        share()
                    }

                    ShareDialog.TYPE_COPY_LINK -> {
                        textCopyThenPost(qrUrl)
                    }

                    ShareDialog.TYPE_DOWNLOAD -> {
                        download()
                    }
                }
            }
        })
    }

    private var isMore = false
    private var shareUser: IMConversationCache? = null

    /**
     * more: jump to IMForwardListAct   or  just show the ForwardDialog
     */
    private fun share() {
        Log.e(TAG, "share: imgUri=$imgUri")
        if (imgUri == null) {
            generateIMShareBitmap()
            return
        }
        val msg = EMMessage.createImageSendMessage(imgUri, true, "")
        IMDataManager.instance.setForwardMsg(msg)
        IMDataManager.instance.setForwardImageUri(imgUri)
        Log.e(TAG, "share: isMore=$isMore")

        if (isMore) {
            TheRouter.build(PageConst.IM.PAGE_IM_FORWARD).withString("forward_msg_id", "")
                .navigation()  // must be empty, clear to know is this a forward msg, or a new created msg?
        } else {
            Log.e(TAG, "share: shareUser=$shareUser")
            shareUser?.also {
                shareTo(
                    Contact(
                        name = it.nick,
                        openUid = it.open_uid,
                        avatar = it.avatar,
                        gender = it.gender
                    )
                )
            }
        }
    }

    private var sendDialog: ForwardDialog? = null
    private var imgUri: Uri? = null
    private fun shareTo(model: Contact) {
        Log.e(TAG, "shareTo: model=$model")
        sendDialog = ForwardDialog(this, model, object : ForwardDialog.Callback {
            override fun onSelected(ok: Boolean) {
                if (ok) {
                    sendDialog?.onDestroy()
                    LoadingDialogHolder.getLoadingDialog()?.show(this@ShareAct)
                    if (from == SHARE_FROM_PROFILE) {
                        val from = CacheUtil.getUserInfo()?.user?.open_uid?.replace("-", "_")
                        PushAndMessageHelper.sendProfileShareImageMessage(
                            from,
                            model.openUid.replace("-", "_"),
                            imgUri
                        )
                    } else if (from == SHARE_FROM_COMPATIBILITY) {
                        PushAndMessageHelper.sendImageMessage(
                            model.openUid.replace("-", "_"),
                            imgUri
                        )
                    } else {
                        PushAndMessageHelper.sendImageMessage(
                            model.openUid.replace("-", "_"),
                            imgUri
                        )
                    }
                } else {
                    sendDialog?.onDestroy()
                }
            }
        })
        sendDialog?.show()
    }

    private fun setMsgCallBack() {
        LiveDataBus.get().with(DemoConstant.MESSAGE_FORWARD, EaseEvent::class.java).observe(this) {
            LoadingDialogHolder.getLoadingDialog()?.hide()
            toastSuccess(getString(R.string.message_sent))
            finish()
        }

        LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_SEND_ERROR, String::class.java)
            .observe(this) {
                LoadingDialogHolder.getLoadingDialog()?.hide()
                toastError(getString(R.string.send_message_error))
            }
    }

    private var from: Int = SHARE_FROM_PROFILE
    private var serverImageUrl: String? = null
    private var qrUrl: String =
        "https://www.sparkle.fun/traveler/933fb26a-a181-4731-964e-ec2cfee89daf"

    override fun initData() {
        from = intent.getIntExtra(FROM_ACTIVITY, SHARE_FROM_PROFILE)
        hideOrShow()

        if (from == SHARE_FROM_PROFILE) {
            IMDataManager.instance.getUser()?.also {
                intent.getStringExtra(USER_AVATAR_URL)?.apply {
                    serverImageUrl = this
                    Log.e(TAG, "initData: serverImageUrl=$serverImageUrl")
                    if (serverImageUrl.isNullOrEmpty()) {
                        requestImgNoDialog(it.open_uid)
                    } else {
                        loadBigImg(serverImageUrl!!)
                    }
                }
                showBody(it)
                IMDataManager.instance.setUser(null)
            }
        }

        if (from == SHARE_FROM_CHAT) {
            IMDataManager.instance.getShareMsg()?.also {
                showChatShareUI(it)
                IMDataManager.instance.setForwardMsg(null)
            }
        }

        if (from == SHARE_FROM_COMPATIBILITY) {
            showCompatibilityUI()
        }

        setMsgCallBack()
    }

    private fun showCompatibilityUI() {
        CacheUtil.getUserInfo()?.user?.also {
            loadAvatarWithCornor(mDataBinding.ivCompatibilityAvatar, it.avatar, it.gender, 16)
            mDataBinding.tvCompatibilityName.text = it.nick
            generateQRcode(
                "${ConstantGlobal.SHARE_BODY}${it.open_uid}",
                mDataBinding.ivCompatibilityQr
            )
        }
    }

    private fun hideOrShow() {
        mDataBinding.bg.isVisible = from == SHARE_FROM_PROFILE
        mDataBinding.bgIm.isVisible = from == SHARE_FROM_PROFILE

        mDataBinding.scroll.isVisible = from == SHARE_FROM_CHAT
        mDataBinding.layChat.isVisible = from == SHARE_FROM_CHAT

        mDataBinding.layCompatibility.isVisible = from == SHARE_FROM_COMPATIBILITY
    }

    private fun showChatShareUI(message: EMMessage) {
        val txtBody = message?.body as? EMCustomMessageBody
        if (txtBody != null) {
            val event = txtBody.event()
            val customExt = txtBody.params
            val msgId = customExt["msgId"] // 透传客户端问题消息 i
            val msgType = customExt["msgType"] // 0-普通消息，1-校验消息，2-结果消息
            val isValid = customExt["isValid"] // 可选字段（只有校验消息才有这个字段），是否有效，1-有效，0-无效
            val content = customExt["content"] // 消息内容，结果消息且有结果的情况，是结构化消息；否则，是 aio 显示的消息
            val hasResult = customExt["hasResult"] // 可选字段（只有结果消息才有这个字段），是否有结果，1-有结果，0-无结果
            Log.e(TAG, "handleAIOMessage: ${GsonConverter.gson.toJson(customExt)}")
            val questionMsg = DemoHelper.getInstance().chatManager.getMessage(msgId)
            val questionStr = (questionMsg?.body as? EMTextMessageBody)?.message
//            Log.e(TAG, "showChatShareUI: questionMsg.msgTime=${questionMsg.msgTime}  questionMsg.localTime()=${questionMsg.localTime()}" )
//            Log.e(TAG, "showChatShareUI: ${System.currentTimeMillis()}", )
//            DateUtils.dayDiff(questionMsg.msgTime, System.currentTimeMillis())

            mDataBinding.tvQuestion.text = questionStr
            val dateTime = Date(questionMsg.msgTime)
            mDataBinding.tvChatTime.text = SimpleDateFormat("HH:mm dd/MM/yyyy").format(dateTime)
            val coraAvatar = CacheUtil.getCoraInfo()?.user?.avatar
            DBComponent.loadAvatar(mDataBinding.ivChatCora, coraAvatar, 2)

            if (msgType == "2") {
                val result = gson.fromJson(content, AIOResult::class.java)
                if (hasResult == "1") {
                    mDataBinding.apply {
                        val list = result.tarotCards
                        if (list.size > 0) showItem(list[0], tv1, iv1, tv1Bottom)
                        if (list.size > 1) showItem(list[1], tv2, iv2, tv2Bottom)
                        if (list.size > 2) showItem(list[2], tv3, iv3, tv3Bottom)
                    }
                }
                mDataBinding.tvChatContent.text = result.result
            }
//            mDataBinding.tvChatExplore.text = getString(com.cyberflow.sparkle.R.string.explore_more_on_starry_book)
//            setExploreSpan(mDataBinding.tvChatExplore)
            val open_uid = message.to.replace("_", "-")
            Log.e(TAG, "generateQRcode: open_uid=$open_uid")
            generateQRcode("${ConstantGlobal.SHARE_BODY}${open_uid}", mDataBinding.ivChatQr)
        }
    }

    private fun showItem(tarotCard: TarotCard, tv: TextView, iv: ImageView, tvBottom: TextView) {
        val name = tarotCard.name
        val url = tarotCard.imgUrl
        val uprightOrReversed = tarotCard.uprightOrReversed
        tv.text = name
        Glide.with(this).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(false)
            .into(iv)
        tvBottom.text = uprightOrReversed
    }

    private fun requestImgNoDialog(open_uid: String) {
        scopeNetLife {
            val data = Post<ManyImageData>(Api.GET_IMAGE_URLS) {
                json("open_uid" to open_uid)
            }.await()
            data?.let {
                it.image_list?.profile_native?.apply {
                    serverImageUrl = this
                    loadBigImg(this)
                }
            }
        }
    }

    private fun loadBigImg(url: String) {
        val holder = ResourcesCompat.getDrawable(
            resources, com.cyberflow.sparkle.R.drawable.profile_default_avatar, null
        )
        DBComponent.loadImageWithHolder(mDataBinding.ivAvatar, url, holder, 24)
        // the reason I made two same image views is save time under pressure, make sure finish it before Friday
        DBComponent.loadImageWithHolder(mDataBinding.ivAvatarIm, url, holder, 24)
    }

    private fun showBody(user: User) {
        mDataBinding.tvName.text = user.nick  // name
        mDataBinding.tvNameIm.text = user.nick
//        mDataBinding.tvContent.text = getString(com.cyberflow.sparkle.R.string.join_me_on_starry_book)
//        setSpan(mDataBinding.tvContent)       // content
        generateQRcode("${ConstantGlobal.SHARE_BODY}${user.open_uid}", mDataBinding.ivQrCode)
    }

    private var resultImage: Bitmap? = null

    private fun generateQRcode(content: String, iv: ImageView) {
        val margin = 0
        val color = Color.BLACK
        val background = Color.WHITE
        val type = HmsScan.QRCODE_SCAN_TYPE
        val width = dp2px(54f)
        val height = width
//        Log.e(TAG, "generateCodeBtnClick: content=$content", )
//        Log.e(TAG, "generateCodeBtnClick: margin=$margin   color=$color    background=$background" )
//        Log.e(TAG, "generateCodeBtnClick: type=$type   width=$width    height=$height" )
        val options = HmsBuildBitmapOption.Creator().setBitmapMargin(margin).setBitmapColor(color)
            .setBitmapBackgroundColor(background).create()
        resultImage = ScanUtil.buildBitmap(content, type, width, height, options)
        iv.setImageBitmap(resultImage)
    }

    private fun setSpan(tv: TextView) {
        tv.movementMethod = ClickableMovementMethod.getInstance()
        tv.text = ("Add me on ").setSpan(ColorSpan("#000000")).addSpan(
            "image", CenterImageSpan(this, R.drawable.share_ic_sparkle).setDrawableSize(
                dp2px(12f)
            ).setMarginHorizontal(dp2px(2f))
        ).addSpan(" Sparkle").setSpan(ColorSpan("#6A4BFB")).addSpan(" !")
            .setSpan(ColorSpan("#000000")).replaceSpan("image") {
                HighlightSpan("#8B82DB") {
//                    ToastUtil.show(this, "click img, go flutter page ")
                }
            }.replaceSpan(" Sparkle") {
                HighlightSpan("#8B82DB") {
//                    ToastUtil.show(this, "click txt, go flutter page ")
                }
            }
    }

    private fun setExploreSpan(tv: TextView) {
        tv.movementMethod = ClickableMovementMethod.getInstance()
        tv.text = ("Explore me in ").setSpan(ColorSpan("#000000")).addSpan(
            "image", CenterImageSpan(this, R.drawable.share_ic_sparkle).setDrawableSize(
                dp2px(12f)
            ).setMarginHorizontal(dp2px(2f))
        ).addSpan(" Sparkle").setSpan(ColorSpan("#6A4BFB")).addSpan(" !")
            .setSpan(ColorSpan("#000000")).replaceSpan("image") {
                HighlightSpan("#8B82DB") {
//                    ToastUtil.show(this, "click img, go flutter page ")
                }
            }.replaceSpan(" Sparkle") {
                HighlightSpan("#8B82DB") {
//                    ToastUtil.show(this, "click txt, go flutter page ")
                }
            }
    }

    private var dialog: ShareDialog? = null

    override fun onResume() {
        super.onResume()
        dialog?.show(this@ShareAct)
    }

    private fun textCopyThenPost(textCopied: String) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        // When setting the clip board text.
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", textCopied))
        // Only show a toast for Android 12 and lower.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            toastSuccess(getString(com.cyberflow.sparkle.R.string.link_copied))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun generateIMShareBitmap() {
        if (checkIfHasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_SHARE)) {
            lifecycleScope.launch {
                val iconBitmap =
                    BitmapFactory.decodeResource(resources, com.cyberflow.sparkle.R.drawable.ic_app)
                val bitmap = if (from == SHARE_FROM_PROFILE) {
                    convertViewToBitmap(mDataBinding.bgIm)
                } else if (from == SHARE_FROM_COMPATIBILITY) {
                    val bgBitmap = BitmapFactory.decodeResource(
                        resources,
                        com.cyberflow.sparkle.R.drawable.share_bg
                    )
                    val viewBitmap = convertViewToBitmap(mDataBinding.layCompatibility)
                    combineBitmap(bgBitmap, viewBitmap, iconBitmap)
                } else {
                    val bgBitmap = BitmapFactory.decodeResource(
                        resources,
                        com.cyberflow.sparkle.R.drawable.share_bg
                    )
                    val viewBitmap = convertViewToBitmap(mDataBinding.layChat)
                    combineBitmap(bgBitmap, viewBitmap, iconBitmap)
                }
                val storePath = application.getExternalFilesDir(null)!!.absolutePath
                val appDir = File(storePath)
                if (!appDir.exists()) {
                    appDir.mkdir()
                }
                val fileName = System.currentTimeMillis().toString() + ".png"
                val file = File(appDir, fileName)
                val fileOutputStream = FileOutputStream(file)
                val isSuccess = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                if (isSuccess) {
                    imgUri = Uri.fromFile(file)
                    share()
                } else {
                    withMain {
                        toastError(getString(com.cyberflow.sparkle.R.string.fail_to_generate_image))
                    }
                }
            }
        }
    }

    private fun download() {
        if (checkIfHasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_DOWNLOAD)) {
            LoadingDialogHolder.getLoadingDialog()?.show(this@ShareAct)
            lifecycleScope.launch {
                val bgBitmap = BitmapFactory.decodeResource(
                    resources,
                    com.cyberflow.sparkle.R.drawable.share_bg
                )
                val iconBitmap =
                    BitmapFactory.decodeResource(resources, com.cyberflow.sparkle.R.drawable.ic_app)
                val viewBitmap = if (from == SHARE_FROM_PROFILE) {
                    convertViewToBitmap(mDataBinding.bg)
                } else if (from == SHARE_FROM_COMPATIBILITY) {
                    convertViewToBitmap(mDataBinding.layCompatibility)
                } else {
                    convertViewToBitmap(mDataBinding.layChat)
                }
                val bitmap = combineBitmap(bgBitmap, viewBitmap, iconBitmap)
                val storePath = application.getExternalFilesDir(null)!!.absolutePath
                val appDir = File(storePath)
                if (!appDir.exists()) {
                    appDir.mkdir()
                }
                val fileName = System.currentTimeMillis().toString() + ".png"
                val file = File(appDir, fileName)
                val fileOutputStream = FileOutputStream(file)
                val isSuccess = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                if (!isSuccess) {
                    withMain {
                        toastError(getString(com.cyberflow.sparkle.R.string.oops_image_download_failed))
                        LoadingDialogHolder.getLoadingDialog()?.hide()
                    }
                    return@launch
                }
                withMain {
                    DownloadFileUtils.saveLocalFile(
                        this@ShareAct,
                        file.absolutePath,
                        "image/jpeg"
                    ) { realPath ->
                        LoadingDialogHolder.getLoadingDialog()?.hide()
                        if (TextUtils.isEmpty(realPath)) {
                            toastError(getString(com.cyberflow.sparkle.R.string.oops_image_download_failed))
                        } else {
                            PictureMediaScannerConnection(this@ShareAct, realPath)
                            toastSuccess(getString(com.cyberflow.sparkle.R.string.image_successfully_downloaded))
                        }
                    }
                }
            }
        }
    }

    // 如果图比屏幕小  那么正常显示   在尾部添加一个logo
    // 如果图比屏幕还大 左右留   上下留  拉长后居中   在尾部添加一个logo
    // 看图和屏幕  把背景按比例处理
    private fun combineBitmap(background: Bitmap, content: Bitmap, iconBitmap: Bitmap): Bitmap {
        if (background == null) {
            return content
        }
        val fgW = content.width
        val fgH = content.height

        // 左右 20  上下30
        var targetW = screenWidth.toFloat()
        var targetH = screenHeight.toFloat()

        var marginLeft = (screenWidth - fgW) / 2f
        var marginTop = (screenHeight - fgH) / 2f

        if (fgH > screenHeight) {
            marginLeft = dp2px(20f).toFloat()
            marginTop = dp2px(70f).toFloat()
            targetW = fgW + marginLeft * 2
            targetH = fgH + marginTop * 2
        }

//        val zoomBitmap = zoomImg(iconBitmap, dp2px(30f).toFloat(), dp2px(30f).toFloat())
        val zoomBitmap = Bitmap.createScaledBitmap(iconBitmap, dp2px(30f), dp2px(30f), true)
        val iconX = targetW / 2 - zoomBitmap.width / 2
        val iconY = targetH - dp2px(20f) - zoomBitmap.height

        val bmOverlay = Bitmap.createBitmap(targetW.toInt(), targetH.toInt(), background.config)
        val canvas = Canvas(bmOverlay)
        canvas.drawBitmap(background, Matrix(), null)
        canvas.drawBitmap(content, marginLeft, marginTop, null)
        canvas.drawBitmap(zoomBitmap, iconX, iconY, null)
        background.recycle()
        content.recycle()
        iconBitmap.recycle()
        zoomBitmap.recycle()
        return bmOverlay
    }

    private fun zoomImg(bm: Bitmap, targetWidth: Float, targetHeight: Float): Bitmap {
        val srcWidth = bm.width
        val srcHeight = bm.height
        val widthScale = targetWidth * 1.0f / srcWidth
        val heightScale = targetHeight * 1.0f / srcHeight
        val matrix = Matrix()
        matrix.postScale(widthScale, heightScale, 0f, 0f)
        val bmpRet = Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)
        val canvas = Canvas(bmpRet)
        val paint = Paint()
        canvas.drawBitmap(bm, matrix, paint)
        return bmpRet
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_DOWNLOAD) {
            download()
        }
        if (requestCode == REQUEST_SHARE) {
            generateIMShareBitmap()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        var title = ""
        var content = ""
        if (requestCode == REQUEST_DOWNLOAD) {
            title = getString(com.cyberflow.sparkle.R.string.unable_to_save_files)
            content =
                getString(com.cyberflow.sparkle.R.string.you_have_turned_off_storage_permissions)
        }
        showPermissionDialog(title, content, requestCode)
    }

    private fun showPermissionDialog(title: String, content: String, requestCode: Int) {
        val permissionDialog = PermissionDialog(this,
            title,
            content,
            object : PermissionDialog.PermissionClickListener {
                override fun leftClicked() {
                    // do nothing
                }

                override fun rightClicked() {
                    PermissionUtil.goIntentSetting(this@ShareAct, requestCode)
                }
            })
        permissionDialog.show()
    }

    @SuppressLint("RestrictedApi")
    private fun checkIfHasPermissions(permission: String, requestCode: Int): Boolean {
        if (!EasyPermissions.hasPermissions(this, permission)) {
            val request = PermissionRequest.Builder(this, requestCode, permission).build()
            request.helper.directRequestPermissions(requestCode, permission)
            return false
        }
        return true
    }
}
