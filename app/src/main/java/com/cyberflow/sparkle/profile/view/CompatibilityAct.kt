package com.cyberflow.sparkle.profile.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.model.Compatibility
import com.cyberflow.base.model.CompatibilityItem
import com.cyberflow.base.net.Api
import com.cyberflow.base.net.GsonConverter
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.PageConst
import com.cyberflow.base.util.dp2px
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.DBComponent
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ActivityCompatibilityBinding
import com.cyberflow.sparkle.im.DBManager
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.net.utils.withIO
import com.drake.net.utils.withMain
import com.therouter.router.Route
import com.wuyr.fanlayout.FanLayout
import kotlinx.coroutines.launch

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

        mDataBinding.btnInviteTwo.setClickListener(object : ShadowTxtButton.ShadowClickListener {
            override fun clicked(disable: Boolean) {
                ShareAct.go(this@CompatibilityAct, ShareAct.SHARE_FROM_COMPATIBILITY, "")
            }
        })
        mDataBinding.btnInviteList.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                CompatibilityRelationAct.go(this@CompatibilityAct)
            }
        })
    }

    override fun initData() {
        batchFetch()
        initFanLayout()

        CacheUtil.getUserInfo()?.user?.apply {
            mDataBinding.tvA.text = this.nick
            DBComponent.loadAvatar(mDataBinding.ivAvatar, avatar, gender)
        }
        mDataBinding.tvB.text = "?"
        mDataBinding.tvDetails.text = getString(R.string.choose_a_constellation_to_see_how_you_guys_fit)
        mDataBinding.layArrow.isVisible = false
    }


    private fun initFanLayout() {
        mDataBinding.fanLayout.apply {
            setOnItemRotateListener { rotation ->
                Log.e(TAG, "FanLayout: ItemRotateListener rotation=$rotation" )
                initRotation += rotation
                mDataBinding.ivRotate.rotation = initRotation
            }
            setOnBearingClickListener { }
            setOnItemClickListener { view, index ->
                Log.e(TAG, "FanLayout: ItemClickListener index=$index" )
                stopRotateImg()
            }
            setOnItemSelectedListener { item ->
                Log.e("TAG", "FanLayout onSelected: isFirstSelect=$isFirstSelect ")
                if(isFirstSelect){
                    isFirstSelect = false
                    return@setOnItemSelectedListener
                }
                val selectIdx: Int = currentSelectedIndex
                val str: String = result[selectIdx % 12]
                Log.e("TAG", "FanLayout onSelected: selectIdx=$selectIdx  str=$str")
                handleSelect(str)
            }

            repeat(12) {
                addView(getView())
            }

            // handle user interaction
            mDataBinding.frameLayout.setViews(mDataBinding.scrollView, mDataBinding.ivAnchor, mDataBinding.fanLayout)
            mDataBinding.frameLayout.setTxtStrict(mDataBinding.layBottom, mDataBinding.tvDetails, dp2px(20f))
        }
    }

    private var isFirstSelect = true

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
        "Aries",
        "Taurus",
        "Gemini",
        "Cancer",
        "Leo",
        "Virgo",
        "Libra",
        "Scorpio",
        "Sagittarius",
        "Capricorn",
        "Aquarius",
        "Pisces"
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

    private fun handleSelect(str: String) {
        Log.e(TAG, "handleSelect: " )

        mDataBinding.tvSelected.text = str
        mDataBinding.layArrow.isVisible = true

        lifecycleScope.launch {
            val cache = DBManager.instance.db?.compatibilityCacheDao()?.fetch(str)
            cache?.also {
                withIO {
                    val data = GsonConverter.gson.fromJson(it.data, CompatibilityItem::class.java)
                    withMain {
                        showData(data)
                    }
                }
            }
        }
    }

    private fun batchFetch() {
        result.forEach { name ->
            scopeNetLife {
                /*val cache = DBManager.instance.db?.compatibilityCacheDao()?.fetch(name)
                if (cache == null) {

                }*/
                val result = Post<CompatibilityItem>(Api.USER_COMPATIBILITY) {
                    json("constellation" to name)
                }.await()
                withIO {
                    val data = GsonConverter.gson.toJson(result)
                    data?.also {
                        DBManager.instance.db?.compatibilityCacheDao()
                            ?.insert(Compatibility(name, it))
                    }
                }
            }
        }
    }

    private fun showData(data: CompatibilityItem?) {
        Log.e(TAG, "showData: $data" )
//        mDataBinding.scrollView.fullScroll(View.FOCUS_DOWN)
        mDataBinding.tvB.text = data?.constellation_b
        mDataBinding.tvDetails.text = data?.content

        /* data?.content?.also {
            val maxLength = it.length
            val randomLength = Random.nextInt(1, maxLength + 1) // 随机生成截取的长度
            val startIndex = Random.nextInt(0, it.length - randomLength + 1) // 随机生成截取的起始索引
            val endIndex = startIndex + randomLength // 计算截取的结束索引
            val substring = it.substring(startIndex, endIndex) // 截取字符串的一部分
            mDataBinding.tvDetails.text = substring
        }*/
    }

    private var isFirst = true
    private var stillRotate = false

    override fun onStart() {
        super.onStart()
        if (isFirst) {
            rotateImg()
            isFirst = false
        } else {
            if (stillRotate) {
                rotateImg()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stillRotate = (timerTask?.isCancelled == false)
        stopRotateImg()
    }

    private var initRotation = 0f

    private var timerTask: TimerTask? = null
    private fun rotateImg() {
        Log.e(TAG, "rotateImg: ")
        if(timerTask!=null){
            timerTask?.cancel()
            timerTask = null
        }
        timerTask = TimerTask(100) {
            Log.e(TAG, "tick rotateImg: ")
            mDataBinding.fanLayout.rotation(0.6f)
        }
        timerTask?.start()
    }

    private fun stopRotateImg() {
        Log.e(TAG, "stopRotateImg: ")
        timerTask?.cancel()
        timerTask = null
    }

//    private fun isTouchPointInView(view: View, x: Float, y: Float): Boolean {
//        val location = IntArray(2)
//        view.getLocationOnScreen(location)
//        val left = location[0]
//        val top = location[1]
//        val right = left + view.measuredWidth
//        val bottom = top + view.measuredHeight
//        return y >= top && y <= bottom && x >= left && x <= right
//    }
//
//    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        ev?.apply {
//            if (timerTask != null && action == MotionEvent.ACTION_DOWN && isTouchPointInView(mDataBinding.ivAnchor, rawX, rawY)) {
//                if (timerTask?.isCancelled == false) {
//                    stopRotateImg()
//                }
//            }
//        }
//        return super.dispatchTouchEvent(ev)
//    }

    class TimerTask(private val interval: Long, private val callback: () -> Unit) {
        private val handler = Handler()
        public var isCancelled = false
        private var runnable: Runnable? = null

        fun start() {
            isCancelled = false
            runnable = object : Runnable {
                override fun run() {
                    if (!isCancelled) {
                        callback.invoke()
                        handler.postDelayed(this, interval)
                    }
                }
            }
            handler.postDelayed(runnable!!, interval)
        }

        fun cancel() {
            isCancelled = true
            runnable?.let {
                handler.removeCallbacks(it)
                runnable = null
            }
        }
    }
}
