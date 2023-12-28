package com.cyberflow.sparkle.profile.view

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.util.PageConst
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ActivityCompatibilityBinding
import com.therouter.router.Route
import com.wuyr.fanlayout.FanLayout
import java.util.Timer
import java.util.TimerTask

@Route(path = PageConst.App.PAGE_COMPATIBILITY)
class CompatibilityAct : BaseDBAct<BaseViewModel, ActivityCompatibilityBinding>() {

    companion object {

        fun go(context: Context) {
            val intent = Intent(context, CompatibilityAct::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.llBack.setOnClickListener {
            finish()
        }
    }

    override fun initData() {

        initFanLayout()
    }


    private fun initFanLayout() {
        mDataBinding.fanLayout.apply {
            setOnItemRotateListener { rotation ->
                initRotation += rotation
                mDataBinding.ivRotate.rotation = initRotation
                if (!isRestored) {
                    for (i in 0 until childCount) {
                        val v: View = getChildAt(i)
                        if (!isBearingView(v)) {
                            val viewGroup = v as ViewGroup
                            for (j in 0 until viewGroup.childCount) {
                                val child = viewGroup.getChildAt(j)
                                if (child is ImageView) {
                                    child.drawable.setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.DST)
                                    child.invalidate()
                                }
                            }
                        }
                    }
                    isRestored = true
                }
            }
            setOnBearingClickListener { }
            setOnItemClickListener { view, index ->
                stopRotateImg()
            }
            setOnItemSelectedListener { item ->
                val selectIdx: Int = getCurrentSelectedIndex()
                Log.d("TAG", "onSelected: selectIdx=$selectIdx")
                val str: String = result[selectIdx % 12]
                showToast(str)

                if (item is ViewGroup) {
                    val viewGroup = item as ViewGroup
                    for (i in 0 until viewGroup.childCount) {
                        val child = viewGroup.getChildAt(i)
                        if (child is ImageView) {
                            val imageView = child
                            imageView.drawable.setColorFilter(
                                resources.getColor(com.cyberflow.base.resources.R.color.almost_black),
                                PorterDuff.Mode.MULTIPLY
                            )
                            imageView.invalidate()
                        }
                    }
                    isRestored = false
                }
            }

            repeat(12){
                addView(getView())
            }

            rotateImg()

            // handle user interaction
            mDataBinding.frameLayout.setViews(mDataBinding.scrollView, mDataBinding.ivAnchor, mDataBinding.fanLayout)
        }
    }
    private var isRestored = true
    private var initRotation = 0f

    private var timer: Timer? = null // 30s 转完

    private fun rotateImg() {
        Log.e("TAG", "rotateImg: ")
        if (timer != null) {
            stopRotateImg()
        }
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                Log.e(TAG, " timer run: ")
                mDataBinding.fanLayout.rotation(0.6f)
            }
        }, 0, 100)
    }

    private fun stopRotateImg() {
        timer?.cancel()
        timer?.purge()
        timer = null
        mDataBinding.fanLayout.playFixingAnimation()
    }

    private val mIds = arrayListOf<Int>(
        R.drawable.ic_0,
        R.drawable.ic_1,
        R.drawable.ic_2,
        R.drawable.ic_3,
        R.drawable.ic_4,
        R.drawable.ic_5,
        R.drawable.ic_6,
        R.drawable.ic_7,
        R.drawable.ic_8,
        R.drawable.ic_9,
        R.drawable.ic_10,
        R.drawable.ic_11,
    )

    private val result = arrayListOf<String>(
        "牡羊座（白羊座）Aries",
        "金牛座 Taurus",
        "雙子座 Gemini",
        "巨蟹座 Cancer",
        "獅子座 Leo",
        "處女座 Virgo",
        "天秤座 Libra",
        "天蠍座 Scorpio",
        "射手座 Sagittarius",
        "摩羯座 Capricorn",
        "水瓶座 Aquarius",
        "雙魚座 Pisces"
    )
    private fun getView(): View? {
        val viewGroup = LayoutInflater.from(this).inflate(R.layout.item, null) as ViewGroup
        var index: Int = mDataBinding.fanLayout.childCount
        if (mDataBinding.fanLayout.bearingType == FanLayout.TYPE_VIEW) {
            index--
        }
        if (index >= mIds.size) {
            index %= mIds.size
        }
        val id: Int = mIds[index]
        for (i in 0 until viewGroup.childCount) {
            val view = viewGroup.getChildAt(i)
            if (view is ImageView) {
                view.setImageResource(id)
            }
        }
        return viewGroup
    }
    private fun showToast(content: String) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
    }

}
