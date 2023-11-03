package com.cyberflow.sparkle.im.view

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.chat.ui.handleQRCode
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
import com.cyberflow.sparkle.databinding.ActivityImScanBinding
import com.cyberflow.sparkle.im.viewmodel.IMViewModel
import com.cyberflow.sparkle.profile.view.ProfileAct
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.huawei.hms.hmsscankit.OnLightVisibleCallBack
import com.huawei.hms.hmsscankit.OnResultCallback
import com.huawei.hms.hmsscankit.RemoteView
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import java.io.IOException


class IMScanAct : BaseDBAct<IMViewModel, ActivityImScanBinding>() {

    var remoteView: RemoteView? = null

    var mScreenWidth = 0
    var mScreenHeight = 0

    val img = intArrayOf(R.drawable.flashlight_on, R.drawable.flashlight_off)

    companion object {

        const val REQUEST_CODE_PHOTO = 0X1113
        fun go(context: Context) {
            val intent = Intent(context, IMScanAct::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.btnBack.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                onBackPressed()
            }
        })
        val dm = resources.displayMetrics
        val density = dm.density
        mScreenWidth = resources.displayMetrics.widthPixels
        mScreenHeight = resources.displayMetrics.heightPixels
        val rect = Rect()
        rect.left = 0
        rect.right = mScreenWidth
        rect.top = 0
        rect.bottom = mScreenHeight

        remoteView = RemoteView.Builder().setContext(this).setBoundingBox(rect)
            .setFormat(HmsScan.ALL_SCAN_TYPE).build()
        remoteView?.setOnLightVisibleCallback(OnLightVisibleCallBack { visible ->
//            if (visible) {
//                mDataBinding.flushBtn.visibility = View.VISIBLE
//            }
        })
        remoteView?.setOnResultCallback(OnResultCallback { result ->
            if (result != null && result.isNotEmpty() && result[0] != null && !TextUtils.isEmpty(
                    result[0].getOriginalValue()
                )
            ) {
                handleQRCode(result)
                finish()
            }
        })
        remoteView?.onCreate(savedInstanceState)
        val params = FrameLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
        )
        mDataBinding.frameLayout.addView(remoteView, params)
        setPictureScanOperation()
//        setFlashOperation()

        initAnimation()
    }

    override fun initData() {

    }

    private fun initAnimation() {
        val mAnimation = TranslateAnimation(TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE,0f, TranslateAnimation.RELATIVE_TO_PARENT, 0f,
            TranslateAnimation.RELATIVE_TO_PARENT, 0.9f)
        mAnimation.duration = 1500
        mAnimation.repeatCount = -1
        mAnimation.repeatMode = Animation.RESTART
        mAnimation.interpolator = LinearInterpolator()
        mDataBinding.ivAnima.animation = mAnimation
    }

    private fun setPictureScanOperation() {
        mDataBinding.btnGallery.setClickListener(object: ShadowImgButton.ShadowClickListener{
            override fun clicked() {
                val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                this@IMScanAct.startActivityForResult(pickIntent, REQUEST_CODE_PHOTO)
            }
        })
    }

   /* private fun setFlashOperation() {
        mDataBinding.flushBtn.setOnClickListener {
            if (remoteView?.lightStatus == true) {
                remoteView?.switchLight()
                mDataBinding.flushBtn.setImageResource(img[1])
            } else {
                remoteView?.switchLight()
                mDataBinding.flushBtn.setImageResource(img[0])
            }
        }
    }*/


    override fun onStart() {
        super.onStart()
        remoteView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        remoteView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        remoteView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        remoteView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        remoteView?.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data?.data)
                val hmsScans = ScanUtil.decodeWithBitmap(
                    this@IMScanAct,
                    bitmap,
                    HmsScanAnalyzerOptions.Creator().setPhotoMode(true).create()
                )
                if (hmsScans != null && hmsScans.isNotEmpty() && hmsScans[0] != null && !TextUtils.isEmpty(
                        hmsScans[0]!!.getOriginalValue()
                    )
                ) {
                    Log.e(TAG, "onActivityResult: ${hmsScans[0]}")
                    if (hmsScans[0].scanType == HmsScan.QRCODE_SCAN_TYPE) {
                        val url = hmsScans[0].getOriginalValue()
                        Log.e(TAG, "onActivityResult: url=$url")
                        val openUid = url.substring(url.lastIndexOf("/") + 1)

                        if (IMDataManager.instance.getConversationData().filter {
                                it.open_uid == openUid
                            }.isNotEmpty()) {
                            ProfileAct.go(this@IMScanAct, openUid, ProfileAct.CHAT)
                        } else {
                            ProfileAct.go(this@IMScanAct, openUid, ProfileAct.ADD_FRIEND)
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

