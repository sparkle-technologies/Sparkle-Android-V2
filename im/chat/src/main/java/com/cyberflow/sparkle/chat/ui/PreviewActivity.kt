package com.cyberflow.sparkle.chat.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.cyberflow.base.act.BaseVBAct
import com.cyberflow.base.util.ToastUtil
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.chat.R
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.databinding.ActivityPreivewBinding
import com.cyberflow.sparkle.chat.ui.fragment.PreviewFragment
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.easeui.utils.GlideEngine
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.photoview.OnViewTapListener
import com.luck.picture.lib.photoview.PhotoView
import com.luck.picture.lib.utils.BitmapUtils
import com.luck.picture.lib.utils.DensityUtil
import com.luck.picture.lib.utils.MediaUtils
import com.vanniktech.ui.hideKeyboard

class PreviewActivity : BaseVBAct<BaseViewModel, ActivityPreivewBinding>() {

    companion object {
        fun go(act: Activity, conversationId: String, chatType: Int, localMedia: LocalMedia) {

            Log.e(TAG, "go: conversationId=$conversationId \t chatType=$chatType localMedia=${localMedia.path}")
            val intent = Intent(act, PreviewActivity::class.java)
            intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, conversationId)
            intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, chatType)
            intent.putExtra(EaseConstant.EXTRA_LOCAL_MEDIA, localMedia)
            act.startActivity(intent)
        }
    }

    private var conversationId: String = ""
    private var chatType: Int = 0
    private var localMedia: LocalMedia? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun initView(savedInstanceState: Bundle?) {

        coverImageView = findViewById(R.id.coverImageView)

        mViewBind.shadowBtnDelete.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                finish()
            }
        })
        mViewBind.shadowBtnShare.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                ToastUtil.show(this@PreviewActivity, "coming soon...", true)
            }
        })
        mViewBind.shadowBtnDownload.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                fragment?.saveImageOrVideo(localMedia)
            }
        })

        coverImageView?.setOnViewTapListener(OnViewTapListener { view, x, y ->
            fragment?.onOutSideClicked()
        })

        coverImageView?.setOnViewDragListener { dx, dy ->
//            Log.e(TAG, "initView: $dy" )
            if(dy > 30){
                hideKeyboard()
                finish()
            }
        }
    }

    private var fragment : PreviewFragment? = null

    private fun initChatFragment() {
        conversationId?.also {
            fragment = PreviewFragment().apply {
                setData(it, chatType)
                supportFragmentManager.beginTransaction().replace(R.id.fl_fragment, this, "preview").commit()
            }
        }
    }

    private var coverImageView: PhotoView? = null

    override fun initData() {

        intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_ID)?.apply {
            conversationId = this
        }
        chatType = intent.getIntExtra(EaseConstant.EXTRA_CHAT_TYPE, 0)
        localMedia = intent.parcelable<LocalMedia>(EaseConstant.EXTRA_LOCAL_MEDIA)

        localMedia?.let { load(it) }

        initChatFragment()
    }

    /****************************** load image **************************************/

    // PicturePreviewAdapter  PreviewImageHolder
    private fun load(media: LocalMedia) {
        initScreenSize()
        val size = getRealSizeFromMedia(media)
        Log.e(PreviewFragment.TAG, "load: size=$size")
        val maxImageSize = BitmapUtils.getMaxImageSize(size!![0], size!![1])
        Log.e(PreviewFragment.TAG, "load: maxImageSize=$maxImageSize")
        loadImage(media, maxImageSize[0], maxImageSize[1])
//        setScaleDisplaySize(media)
        setCoverScaleType(media)
    }

    private var screenWidth = 0
    private var screenHeight = 0
    private var screenAppInHeight = 0

    private fun initScreenSize() {
        screenWidth = DensityUtil.getRealScreenWidth(this)
        screenHeight = DensityUtil.getScreenHeight(this)
        screenAppInHeight = DensityUtil.getRealScreenHeight(this)
    }

    private fun getRealSizeFromMedia(media: LocalMedia): IntArray? {
        return if (media.isCut && media.cropImageWidth > 0 && media.cropImageHeight > 0) {
            intArrayOf(media.cropImageWidth, media.cropImageHeight)
        } else {
            intArrayOf(media.width, media.height)
        }
    }

    private fun loadImage(media: LocalMedia, maxWidth: Int, maxHeight: Int) {
        Log.e(PreviewFragment.TAG, "loadImage: maxWidth=$maxWidth \t maxHeight=$maxHeight")
        GlideEngine.createGlideEngine().apply {
            val availablePath = media.availablePath
            Log.e(PreviewFragment.TAG, "loadImage: availablePath=$availablePath")
            if (maxWidth == PictureConfig.UNSET && maxHeight == PictureConfig.UNSET) {
                loadImage(this@PreviewActivity, availablePath, coverImageView)
            } else {
                loadImage(this@PreviewActivity, coverImageView, availablePath, maxWidth, maxHeight)
            }
        }
    }

    private fun setScaleDisplaySize(media: LocalMedia) {
        if (screenWidth < screenHeight) {
            if (media.width > 0 && media.height > 0) {
                val layoutParams = coverImageView!!.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.width = screenWidth
                layoutParams.height = screenAppInHeight
//                layoutParams.gravity = Gravity.CENTER
            }
        }
    }

    private fun setCoverScaleType(media: LocalMedia) {
        if (MediaUtils.isLongImage(media.width, media.height)) {
            coverImageView!!.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            coverImageView!!.scaleType = ImageView.ScaleType.FIT_CENTER
        }
    }

    override fun finish() {
        LiveDataBus.get().with(DemoConstant.MSG_LIST_FRESH_TO_LATEST).postValue("${System.currentTimeMillis()}")
        super.finish()
    }
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}
