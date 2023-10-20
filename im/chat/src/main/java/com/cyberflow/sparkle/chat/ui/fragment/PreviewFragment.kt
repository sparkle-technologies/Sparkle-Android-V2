package com.cyberflow.sparkle.chat.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cyberflow.sparkle.chat.R
import com.cyberflow.sparkle.chat.common.model.EmojiconExampleGroupData
import com.cyberflow.sparkle.chat.common.utils.CompressFileEngineImpl
import com.cyberflow.sparkle.chat.ui.fragment.ChatFragment.MeOnCameraInterceptListener
import com.cyberflow.sparkle.widget.PermissionDialog
import com.cyberflow.sparkle.widget.PermissionDialog.PermissionClickListener
import com.drake.tooltip.dialog.BubbleDialog
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.domain.EaseEmojicon
import com.hyphenate.easeui.input.InputAwareLayout
import com.hyphenate.easeui.input.KeyboardAwareLinearLayout
import com.hyphenate.easeui.interfaces.MessageListItemClickListener
import com.hyphenate.easeui.modules.chat.EaseChatExtendMenu
import com.hyphenate.easeui.modules.chat.EaseChatInputMenu
import com.hyphenate.easeui.modules.chat.EaseChatMessageListLayout
import com.hyphenate.easeui.modules.chat.interfaces.ChatInputMenuListener
import com.hyphenate.easeui.modules.chat.interfaces.IChatExtendMenu
import com.hyphenate.easeui.modules.chat.presenter.EaseHandleMessagePresenter
import com.hyphenate.easeui.modules.chat.presenter.EaseHandleMessagePresenterImpl
import com.hyphenate.easeui.modules.chat.presenter.IHandleMessageView
import com.hyphenate.easeui.ui.dialog.LoadingDialogHolder
import com.hyphenate.easeui.utils.EaseFileUtils
import com.hyphenate.easeui.utils.GlideEngine.Companion.createGlideEngine
import com.hyphenate.easeui.utils.PicSelectorHelper
import com.hyphenate.util.ImageUtils
import com.luck.picture.lib.basic.PictureMediaScannerConnection
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnCallbackListener
import com.luck.picture.lib.interfaces.OnCameraInterceptListener
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.permissions.PermissionUtil
import com.luck.picture.lib.utils.DownloadFileUtils
import com.luck.picture.lib.utils.ToastUtils
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.io.File
import java.io.IOException

open class PreviewFragment : Fragment(), EaseChatMessageListLayout.OnMessageTouchListener,
    EasyPermissions.PermissionCallbacks, IHandleMessageView,
    MessageListItemClickListener, EaseChatMessageListLayout.OnChatErrorListener,
    KeyboardAwareLinearLayout.OnKeyboardShownListener,
    KeyboardAwareLinearLayout.OnKeyboardHiddenListener,
    EaseChatInputMenu.OnConversationInputPanelStateChangeListener, ChatInputMenuListener {

    companion object {
        const val TAG = "PreviewFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preivew, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private var rootLinearLayout: InputAwareLayout? = null
    private var inputMenu: EaseChatInputMenu? = null
    private var messageListLayout: EaseChatMessageListLayout? = null


    private fun initView(view: View) {
        rootLinearLayout = view.findViewById(R.id.rootLinearLayout)
        inputMenu = view.findViewById(R.id.inputMenu)
        messageListLayout = view.findViewById(R.id.messageListLayout)

//        Log.e(TAG, "go: conversationId=$conversationId \t chatType=$chatType localMedia=${localMedia?.path}")

        initInputView()
    }

    private var localMedia: LocalMedia? = null

    public fun saveImageOrVideo(localMedia: LocalMedia?) {
        this.localMedia = localMedia
        if (checkIfHasPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, ChatFragment.REQUEST_CODE_STORAGE_FILE)) {
            realSave(localMedia)
        }
    }

    public fun onOutSideClicked(){
        inputMenu?.onOutSideClicked()
    }

    @SuppressLint("RestrictedApi")
    private  fun checkIfHasPermissions(permission: String, requestCode: Int): Boolean {
        if (!EasyPermissions.hasPermissions(requireContext(), permission)) {
            val request = PermissionRequest.Builder(this, requestCode, permission).build()
            request.helper.directRequestPermissions(requestCode, permission)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
         if(requestCode == ChatFragment.REQUEST_CODE_STORAGE_FILE){
             realSave(localMedia)
         }
        if(requestCode == ChatFragment.REQUEST_CODE_CAMERA){
            takePictureOrRecordVideo()
        }
        if(requestCode == ChatFragment.REQUEST_CODE_STORAGE_PICTURE){
            selectPictureOrVideoFromGallery()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        var title = ""
        var content  = ""
        if(requestCode == ChatFragment.REQUEST_CODE_STORAGE_FILE){
              title = "Unable to save files"
              content  = "You have turned off storage  permissions"
        }
        if(requestCode == ChatFragment.REQUEST_CODE_CAMERA){
            title = "Unable to take photos"
            content  = "You have turned off camera  permissions."
        }
        if(requestCode == ChatFragment.REQUEST_CODE_STORAGE_PICTURE){
            title = "Unable to access the gallery"
            content  = "You have turned off gallery  permissions."
        }
        showPermissionDialog(title, content, requestCode)
    }

    private fun showPermissionDialog(title: String, content: String, requestCode: Int) {
        val dialog = PermissionDialog(requireContext(), title, content, object : PermissionClickListener {
                override fun leftClicked() {
                    // do nothing
                }

                override fun rightClicked() {
                    PermissionUtil.goIntentSetting(requireActivity(), requestCode)
                }
            })
        dialog.show()
    }

    private fun realSave(localMedia: LocalMedia?) {
        localMedia?.let {
            val media = it
            val path = media.availablePath
            if (PictureMimeType.isHasHttp(path)) {
                LoadingDialogHolder.getLoadingDialog()?.show(requireContext())
            }
            DownloadFileUtils.saveLocalFile(context, path, media.mimeType, object : OnCallbackListener<String?> {
                override fun onCall(realPath: String?) {
                    LoadingDialogHolder.getLoadingDialog()?.hide()
                    if (TextUtils.isEmpty(realPath)) {
                        var errorMsg: String = ""
                        errorMsg = if (PictureMimeType.isHasAudio(media.mimeType)) {
                            getString(com.luck.picture.lib.R.string.ps_save_audio_error)
                        } else if (PictureMimeType.isHasVideo(media.mimeType)) {
                            getString(com.luck.picture.lib.R.string.ps_save_video_error)
                        } else {
                            getString(com.luck.picture.lib.R.string.ps_save_image_error)
                        }
                        ToastUtils.showToast(context, errorMsg)
                    } else {
                        PictureMediaScannerConnection(activity, realPath)
                        ToastUtils.showToast(context, "${getString(com.luck.picture.lib.R.string.ps_save_success)}\n$realPath")
                    }
                }
            })
        }
    }

    private var conversationId: String = ""
    private var chatType: Int = 0

    fun setData(_conversationId: String, _chatType: Int) {
        this.conversationId = _conversationId
        this.chatType = _chatType
    }



    /****************** IM *********************/

    var presenter: EaseHandleMessagePresenter? = null 

    private fun initInputView() {
        rootLinearLayout?.addOnKeyboardShownListener(this);
        inputMenu?.init(rootLinearLayout)
        inputMenu?.setOnConversationInputPanelStateChangeListener(this)
        inputMenu?.setChatInputMenuListener(this)

        messageListLayout?.setOnMessageTouchListener(this)
        messageListLayout?.setMessageListItemClickListener(this)
        messageListLayout?.setOnChatErrorListener(this)

        resetChatExtendMenu()

        presenter = EaseHandleMessagePresenterImpl()
        presenter?.apply {
            setupWithToUser(chatType, conversationId)
            if (context is AppCompatActivity) {
                (context as AppCompatActivity).lifecycle.addObserver(this)
            }
            attachView(this@PreviewFragment)
        }
    }

    private fun resetChatExtendMenu() {
        inputMenu?.chatExtendMenu?.apply {
            val chatExtendMenu: IChatExtendMenu = this
            chatExtendMenu.clear()
            // seven menu in total, include camera, library, send token, send nft, daily horoscope, horoscope, compatibility
            for (i in EaseChatExtendMenu.itemdrawables.indices) {
                chatExtendMenu.registerMenuItem(
                    EaseChatExtendMenu.itemStrings[i],
                    EaseChatExtendMenu.itemdrawables[i],
                    EaseChatExtendMenu.itemIds[i]
                )
            }
            //添加扩展表情
            inputMenu?.emojiconMenu?.addEmojiconGroup(EmojiconExampleGroupData.getData())
        }
    }


    override fun onInputPanelExpanded() {
//        inputMenu.hideSoftKeyboard()
//        inputMenu.showExtendMenu(false)
    }

    override fun onInputPanelCollapsed() {

    }

    override fun onTyping(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun onSendMessage(content: String?) {
        presenter?.sendTextMessage(content)
    }

    override fun onExpressionClicked(emojicon: Any?) {
        if (emojicon is EaseEmojicon) {
            presenter?.sendBigExpressionMessage(emojicon.name, emojicon.identityGroupCode, emojicon.identityCode)
        }
    }

    override fun onPressToSpeakBtnTouch(v: View?, event: MotionEvent?): Boolean = false

    override fun onChatExtendMenuItemClick(itemId: Int, view: View?) {
        if (itemId == EaseChatExtendMenu.itemIds[0]) {    // camera + record video
            if (checkIfHasPermissions(Manifest.permission.CAMERA, ChatFragment.REQUEST_CODE_CAMERA)) {
                takePictureOrRecordVideo()
            }
        }
        if (itemId == EaseChatExtendMenu.itemIds[1]) {   // gallery =  image + video
            if (checkIfHasPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, ChatFragment.REQUEST_CODE_STORAGE_PICTURE)) {
                selectPictureOrVideoFromGallery()
            }
        }
    }
    
    private fun takePictureOrRecordVideo() {
        PictureSelector.create(this).openCamera(SelectMimeType.ofAll()).isOriginalSkipCompress(true)
            .setCameraInterceptListener(getCustomCameraEvent())
            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                override fun onResult(result: ArrayList<LocalMedia?>?) {
                    if (result.isNullOrEmpty()) {
                        return
                    }
                    result[0]?.also {
                        handleImgOrVideo(it, true)
                    }
                }

                override fun onCancel() {
                    Log.e(TAG, "onCancel: ")
                }
            })
    }

    private fun selectPictureOrVideoFromGallery() {
        PictureSelector.create(activity).openGallery(SelectMimeType.ofAll())
            .setSelectorUIStyle(PicSelectorHelper.getSelectMainStyle(activity))
            .setImageEngine(createGlideEngine()).isOriginalSkipCompress(true).isDisplayCamera(false)
            .setSelectionMode(SelectModeConfig.SINGLE)
            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                override fun onResult(result: ArrayList<LocalMedia?>?) {
                    if (result != null && result.size > 0) {
                        result[0]?.also {
                            handleImgOrVideo(it, false)
                        }
                    }
                }

                override fun onCancel() {}
            })
    }


    private fun handleImgOrVideo(select: LocalMedia, checkDegree: Boolean) {
        Log.e(TAG, "path: " + select.path)
        Log.e(TAG, "getAvailablePath: " + select.availablePath)
        Log.e(TAG, "getRealPath: " + select.realPath)
        Log.e(TAG, "compressPath: " + select.compressPath)
        val currentEditPath = select.availablePath
        val mapped =
            if (PictureMimeType.isContent(currentEditPath)) Uri.parse(currentEditPath) else Uri.fromFile(
                File(currentEditPath)
            )
        Log.e(TAG, "onResult:  finally " + mapped.path)
        if (PictureMimeType.isHasImage(select.mimeType)) {  // image logic
            if (checkDegree) {
                val restoreImageUri = ImageUtils.checkDegreeAndRestoreImage(requireContext(), mapped)
                presenter?.sendImageMessage(restoreImageUri, true)
            } else {
                if (mapped != null) {
                    val filePath = EaseFileUtils.getFilePath(requireContext(), mapped)
                    if (!TextUtils.isEmpty(filePath) && File(filePath).exists()) {
                        presenter?.sendImageMessage(Uri.parse(filePath), true)
                    } else {
                        EaseFileUtils.saveUriPermission(requireContext(), mapped, null)
                        presenter?.sendImageMessage(mapped, true)
                    }
                }
            }
        }
        if (PictureMimeType.isHasVideo(select.mimeType)) {   // video logic
            val mediaPlayer = MediaPlayer()
            try {
                mediaPlayer.setDataSource(requireContext(), mapped)
                mediaPlayer.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val duration = mediaPlayer.duration / 1000
            EaseFileUtils.saveUriPermission(requireContext(), mapped, null)
            val file = File(currentEditPath)
            val size = file.length().toFloat() / (1024 * 1024)
            Log.e(
                TAG,
                "onResult: path=" + mapped.path + "\t duration=" + duration + "\t size=" + size
            )
            if (size <= CompressFileEngineImpl.ORIGIN_VIDEO_MAX_SIZE) {
                presenter?.sendVideoMessage(mapped, duration)
            } else {
                if (size > CompressFileEngineImpl.VIDEO_SIZE_VERY_LOW) {
                    ToastUtils.showToast(requireContext(), "video size too large, make sure it less than " + CompressFileEngineImpl.VIDEO_SIZE_VERY_LOW + "MB")
                    return
                }
                showCompressDialog()
                val list = java.util.ArrayList<Uri>(1)
                list.add(mapped)
                CompressFileEngineImpl.check(requireContext(), list, OnKeyValueResultCallbackListener { srcPath, resultPath ->
                    hideCompressDialog()
                    Log.e(
                        TAG,
                        "onCallback: srcPath=$srcPath\t resultPath=$resultPath"
                    )
                    if (srcPath == null || resultPath == null) {
                        ToastUtils.showToast(requireContext(), "compress video error")
                        return@OnKeyValueResultCallbackListener
                    }
                    val oldF = File(srcPath)
                    val newF = File(resultPath)
                    val old_file_size = oldF.length().toFloat() / (1024 * 1024)
                    val new_file_size = newF.length().toFloat() / (1024 * 1024)
                    val percent = (old_file_size - new_file_size) / old_file_size

                    // size1=49.50347MB  	 size2=5.128317MB  	  percent=0.89640486       VERY_LOW
                    // size1=49.50347MB  	 size2=10.031748MB  	  percent=0.7973527    Low
                    // size1=49.50347MB  	 size2=14.908022MB  	  percent=0.698849     MEDIUM
                    // size1=49.50347MB  	 size2=19.796103MB  	  percent=0.6001068    HIGH
                    // size1=49.50347MB  	 size2=29.458006MB  	  percent=0.4049305    VERY_HIGH

                    // >10MB   不行
                    Log.e(TAG, "onCallback: size1=" + old_file_size + "MB  \t size2=" + new_file_size + "MB  \t  percent=" + percent)
                    if (new_file_size > CompressFileEngineImpl.ORIGIN_VIDEO_MAX_SIZE) {
                        ToastUtils.showToast(requireContext(), "video size cannot more than 10MB")
                        return@OnKeyValueResultCallbackListener
                    }
                    presenter?.sendVideoMessage(Uri.fromFile(File(resultPath)), duration)
                })
            }
        }
    }

    var compressDialog: BubbleDialog? = null

    private fun showCompressDialog() {
        if (compressDialog == null) {
            compressDialog = BubbleDialog(requireContext(), "compress video")
        }
        if (compressDialog!!.isShowing) {
            return
        }
        compressDialog!!.show()
    }

    private fun hideCompressDialog() {
        if (compressDialog != null) {
            compressDialog!!.dismiss()
        }
    }
    
    private fun getCustomCameraEvent(): OnCameraInterceptListener? {
        return MeOnCameraInterceptListener()
    }
    
    /********************************************************************/


    override fun onKeyboardHidden() {
        Log.e(TAG, "onKeyboardHidden: ")
        inputMenu?.onKeyboardHidden()
    }

    override fun onKeyboardShown() {
        Log.e(TAG, "onKeyboardShown: ")
        inputMenu?.onKeyboardShown()
    }


    /*override fun onBackPressed() {
        var consumed = false
        rootLinearLayout.apply {
            if (currentInput != null) {
                hideAttachedInput(true)
                inputMenu.closeConversationInputPanel()
                consumed = true
            }
        }
        if(!consumed)
            super.onBackPressed()
    }*/

    /********************************************************************/

    /*override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (null != this.currentFocus) {
            val mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            return mInputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
        }
        return super.onTouchEvent(event)
    }*/

    /********************************************************************/


    override fun onBubbleClick(message: EMMessage?): Boolean {
        return false
    }

    override fun onPicturePreview(localMedia: LocalMedia?, position: Int) {

    }

    override fun onResendClick(message: EMMessage?): Boolean {
        return true
    }

    override fun onBubbleLongClick(v: View?, message: EMMessage?): Boolean {
        return false
    }

    override fun onUserAvatarClick(username: String?) {

    }

    override fun onUserAvatarLongClick(username: String?) {

    }

    override fun onMessageCreate(message: EMMessage?) {

    }

    override fun onMessageSuccess(message: EMMessage?) {
    }

    override fun onMessageError(message: EMMessage?, code: Int, error: String?) {
    }

    override fun onMessageInProgress(message: EMMessage?, progress: Int) {
    }

    override fun onTouchItemOutside(v: View?, position: Int) {
        inputMenu?.onOutSideClicked()
    }

    override fun onViewDragging() {
        inputMenu?.hideSoftKeyboard()
        inputMenu?.showExtendMenu(false)
    }

    override fun onChatError(code: Int, errorMsg: String?) {

    }

    /********************************************************************/
    override fun context(): Context {
         return this.requireContext()
    }

    override fun createThumbFileFail(message: String?) {

    }

    override fun addMsgAttrBeforeSend(message: EMMessage?) {

    }

    override fun sendMessageFail(message: String?) {

    }

    override fun sendMessageFinish(message: EMMessage?) {

    }

    override fun deleteLocalMessageSuccess(message: EMMessage?) {

    }

    override fun recallMessageFinish(message: EMMessage?) {

    }

    override fun recallMessageFail(code: Int, message: String?) {

    }

    override fun onPresenterMessageSuccess(message: EMMessage?) {

    }

    override fun onPresenterMessageError(message: EMMessage?, code: Int, error: String?) {

    }

    override fun onPresenterMessageInProgress(message: EMMessage?, progress: Int) {

    }

    override fun translateMessageSuccess(message: EMMessage?) {

    }

    override fun translateMessageFail(message: EMMessage?, code: Int, error: String?) {

    }
}