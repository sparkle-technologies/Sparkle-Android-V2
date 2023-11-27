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
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.model.IMUserInfo
import com.cyberflow.base.model.ManyImageData
import com.cyberflow.base.model.User
import com.cyberflow.base.net.Api
import com.cyberflow.base.resources.R
import com.cyberflow.base.util.ConstantGlobal
import com.cyberflow.base.util.PageConst
import com.cyberflow.base.util.ToastUtil
import com.cyberflow.base.util.ViewExt.convertViewToBitmap
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.util.dp2px
import com.cyberflow.sparkle.DBComponent
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.common.manager.PushAndMessageHelper
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
import com.cyberflow.sparkle.databinding.ActivityShareBinding
import com.cyberflow.sparkle.im.viewmodel.Contact
import com.cyberflow.sparkle.im.widget.ForwardDialog
import com.cyberflow.sparkle.profile.viewmodel.ShareViewModel
import com.cyberflow.sparkle.profile.widget.ShareDialog
import com.cyberflow.sparkle.widget.NotificationDialog
import com.cyberflow.sparkle.widget.PermissionDialog
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.cyberflow.sparkle.widget.ToastDialogHolder
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
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.ui.dialog.LoadingDialogHolder
import com.luck.picture.lib.basic.PictureMediaScannerConnection
import com.luck.picture.lib.interfaces.OnCallbackListener
import com.luck.picture.lib.permissions.PermissionUtil
import com.luck.picture.lib.utils.DownloadFileUtils
import com.luck.picture.lib.utils.ToastUtils
import com.therouter.TheRouter
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.io.File
import java.io.FileOutputStream

class ShareAct : BaseDBAct<ShareViewModel, ActivityShareBinding>(), EasyPermissions.PermissionCallbacks{

    companion object {
        const val USER_AVATAR_URL = "server_img_url"

        const val REQUEST_DOWNLOAD = 233
        const val REQUEST_SHARE = 234

        fun go(context: Context,  serverImageUrl: String?) {
            val intent = Intent(context, ShareAct::class.java)
            intent.putExtra(USER_AVATAR_URL, serverImageUrl)
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
            override fun onSelected(user: IMUserInfo?, type: Int) {
//                Log.e(TAG, "onSelected: type=$type  user=$user" )
                when(type){
                    ShareDialog.TYPE_SHARE -> {
                        isMore = false
                        shareUser = user
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
    private var shareUser: IMUserInfo? = null

    /**
     * more: jump to IMForwardListAct   or  just show the ForwardDialog
     */
    private fun share() {
        if(imgUri == null){
            generateIMShareBitmap()
            return
        }
        val msg = EMMessage.createImageSendMessage(imgUri, true, "")
        IMDataManager.instance.setForwardMsg(msg)
        IMDataManager.instance.setForwardImageUri(imgUri)
        if(isMore){
            TheRouter.build(PageConst.IM.PAGE_IM_FORWARD)
                .withString("forward_msg_id", "")  // must be empty, clear to know is this a forward msg, or a new created msg?
                .navigation()
        }else{
            shareUser?.also {
                shareTo(Contact(name=it.nick, openUid=it.open_uid, avatar=it.avatar, gender = it.gender))
            }
        }
    }


    private var sendDialog : ForwardDialog? = null
    private var imgUri : Uri? = null
    private fun shareTo(model: Contact) {
        sendDialog = ForwardDialog(this, model, object: ForwardDialog.Callback {
            override fun onSelected(ok: Boolean) {
                if(ok){
                    sendDialog?.onDestroy()
                    LoadingDialogHolder.getLoadingDialog()?.show(this@ShareAct)
                    PushAndMessageHelper.sendImageMessage(model.openUid.replace("-", "_"), imgUri)
                }else{
                    sendDialog?.onDestroy()
                }
            }
        })
        sendDialog?.show()
    }

    private fun setMsgCallBack(){
        LiveDataBus.get().with(DemoConstant.MESSAGE_FORWARD, EaseEvent::class.java).observe(this) {
            LoadingDialogHolder.getLoadingDialog()?.hide()
            ToastUtil.show(this@ShareAct, "Message Sent!")
            finish()
        }

        LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_SEND_ERROR, String::class.java).observe(this) {
            LoadingDialogHolder.getLoadingDialog()?.hide()
            ToastUtil.show(this@ShareAct, "Send Message error")
        }
    }

    private var serverImageUrl : String?  = null
    private var qrUrl : String  = "https://www.sparkle.fun/traveler/933fb26a-a181-4731-964e-ec2cfee89daf"

    override fun initData() {
        IMDataManager.instance.getUser()?.also {
            intent.getStringExtra(USER_AVATAR_URL)?.apply {
                serverImageUrl = this
                Log.e(TAG, "initData: serverImageUrl=$serverImageUrl" )
                if(serverImageUrl.isNullOrEmpty()){
                    requestImgNoDialog(it.open_uid)
                }else{
                    loadBigImg(serverImageUrl!!)
                }
            }
            showBody(it)
        }
        setMsgCallBack()
    }

    private fun requestImgNoDialog(open_uid: String){
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

    private fun loadBigImg(url:String){
        val holder = ResourcesCompat.getDrawable(resources, com.cyberflow.sparkle.R.drawable.profile_default_avatar,null)
        DBComponent.loadImageWithHolder(mDataBinding.ivAvatar, url, holder, 24)
        // the reason I made two same image views is save time under pressure, make sure finish it before Friday
        DBComponent.loadImageWithHolder(mDataBinding.ivAvatarIm, url, holder, 24)
    }

    private fun showBody(user: User){
        mDataBinding.tvName.text = user.nick  // name
        mDataBinding.tvNameIm.text = user.nick   
        setSpan(mDataBinding.tvContent)       // content
//        generateQRcode("https://www.sparkle.fun/traveler/933fb26a-a181-4731-964e-ec2cfee89daf")
        generateQRcode("${ConstantGlobal.SHARE_BODY}${user.open_uid}")
    }

    private var resultImage: Bitmap? = null

    private fun generateQRcode(content: String) {
        val margin = 0
        val color = Color.BLACK
        val background = Color.WHITE
        val type = HmsScan.QRCODE_SCAN_TYPE
        val width = dp2px(54f)
        val height = width
//        Log.e(TAG, "generateCodeBtnClick: content=$content", )
//        Log.e(TAG, "generateCodeBtnClick: margin=$margin   color=$color    background=$background" )
//        Log.e(TAG, "generateCodeBtnClick: type=$type   width=$width    height=$height" )
        val options = HmsBuildBitmapOption.Creator().setBitmapMargin(margin).setBitmapColor(color).setBitmapBackgroundColor(background).create()
        resultImage = ScanUtil.buildBitmap(content, type, width, height, options)
        mDataBinding.ivQrCode.setImageBitmap(resultImage)
    }

    private fun setSpan(tv: TextView) {
        tv.movementMethod = ClickableMovementMethod.getInstance()
        tv.text = ("Add me on " ).setSpan(ColorSpan("#000000"))
            .addSpan("image", CenterImageSpan(this, R.drawable.share_ic_sparkle).setDrawableSize(
                dp2px(12f)
            ).setMarginHorizontal(dp2px(2f)) )
            .addSpan(" Sparkle").setSpan(ColorSpan("#6A4BFB"))
            .addSpan(" !").setSpan(ColorSpan("#000000"))
            .replaceSpan("image"){
                HighlightSpan("#8B82DB"){
                    ToastUtil.show(this, "click img, go flutter page ")
                }
            }
            .replaceSpan(" Sparkle"){
                HighlightSpan("#8B82DB"){
                    ToastUtil.show(this, "click txt, go flutter page ")
                }
            }
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


    private fun textCopyThenPost(textCopied: String) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        // When setting the clip board text.
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", textCopied))
        // Only show a toast for Android 12 and lower.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2){
            ToastDialogHolder.getDialog()?.show(this@ShareAct, NotificationDialog.TYPE_SUCCESS, getString(com.cyberflow.sparkle.R.string.link_copied))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
    
    private fun generateIMShareBitmap(){
        if (checkIfHasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_SHARE)) {
            lifecycleScope.launch {
                val bitmap = convertViewToBitmap(mDataBinding.bgIm)
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
                if(isSuccess){
                    imgUri = Uri.fromFile(file)
                    share()
                }else{
                    withMain {
                        ToastUtils.showToast(this@ShareAct, "fail to generate image")
                    }
                }
            }
        }
    }
    
    private fun download() {
        if (checkIfHasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_DOWNLOAD)) {
            LoadingDialogHolder.getLoadingDialog()?.show(this@ShareAct)
            lifecycleScope.launch {
                val bgBitmap = BitmapFactory.decodeResource(resources, com.cyberflow.sparkle.R.drawable.share_bg)
                val viewBitmap = convertViewToBitmap(mDataBinding.bg)
                val bitmap = combineBitmap(bgBitmap, viewBitmap)
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
                if(!isSuccess){
                    withMain {
                        ToastUtils.showToast(this@ShareAct, "fail to compress image")
                        LoadingDialogHolder.getLoadingDialog()?.hide()
                    }
                    return@launch
                }
                withMain {
                    DownloadFileUtils.saveLocalFile(this@ShareAct, file.absolutePath, "image/jpeg", object : OnCallbackListener<String?> {
                        override fun onCall(realPath: String?) {
                            LoadingDialogHolder.getLoadingDialog()?.hide()
                            if (TextUtils.isEmpty(realPath)) {
                                val errorMsg: String = getString(com.luck.picture.lib.R.string.ps_save_image_error)
                                ToastUtils.showToast(this@ShareAct, errorMsg)
                            } else {
                                PictureMediaScannerConnection(this@ShareAct, realPath)
                                ToastUtils.showToast(this@ShareAct, "${getString(com.luck.picture.lib.R.string.ps_save_success)}\n$realPath")
                            }
                        }
                    })
                }
            }
        }
    }


    private fun combineBitmap(background: Bitmap, foreground: Bitmap): Bitmap{
        if(background == null){
            return foreground
        }
        val bgW = background.width
        val bgH = background.height
        val fgW = foreground.width
        val fgH = foreground.height
        // w=302  h=430    screenW = 375   screenH = 812
        val targetW = bgW * 302 / 375
        val targetH = targetW * fgH / fgW   // keep the origin picture ration
        val zoomBitmap = zoomImg(foreground, targetW, targetH)

        val left = (bgW - targetW).toFloat() / 2
        val top =  (bgH - targetH).toFloat() / 2

        val newBmp = Bitmap.createBitmap(bgW, bgH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBmp)
        canvas.drawBitmap(background, 0f, 0f, null)
        canvas.drawBitmap(zoomBitmap, left, top, null)
        canvas.save()
        canvas.restore()
        return newBmp
    }


    private fun zoomImg(bm: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val srcWidth = bm.width
        val srcHeight = bm.height
        val widthScale = targetWidth * 1.0f / srcWidth
        val heightScale = targetHeight * 1.0f / srcHeight
        val matrix = Matrix()
        matrix.postScale(widthScale, heightScale, 0f, 0f)
        val bmpRet = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmpRet)
        val paint = Paint()
        canvas.drawBitmap(bm, matrix, paint)
        return bmpRet
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if(requestCode == REQUEST_DOWNLOAD){
             download()
        }
        if(requestCode == REQUEST_SHARE){
            generateIMShareBitmap()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        var title = ""
        var content  = ""
        if(requestCode == REQUEST_DOWNLOAD){
            title = "Unable to save files"
            content  = "You have turned off storage  permissions"
        }
        showPermissionDialog(title, content, requestCode)
    }

    private fun showPermissionDialog(title: String, content: String, requestCode: Int) {
        val permissionDialog = PermissionDialog(this, title, content, object : PermissionDialog.PermissionClickListener {
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
