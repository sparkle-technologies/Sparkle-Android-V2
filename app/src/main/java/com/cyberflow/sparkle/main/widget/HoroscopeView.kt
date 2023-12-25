package com.cyberflow.sparkle.main.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.cyberflow.base.model.DBHoroscope
import com.cyberflow.base.model.DailyHoroScopeData
import com.cyberflow.base.model.YearlyHoroScopeData
import com.cyberflow.base.net.Api
import com.cyberflow.base.net.GsonConverter
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.dp2px
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ItemHoroscopeBannerBinding
import com.cyberflow.sparkle.databinding.ItemHoroscopeBinding
import com.cyberflow.sparkle.im.DBManager
import com.cyberflow.sparkle.main.view.EmptyItem
import com.cyberflow.sparkle.main.view.HoroscopeHeadItem
import com.cyberflow.sparkle.main.view.HoroscopeItem
import com.cyberflow.sparkle.main.view.HoroscopeReq
import com.cyberflow.sparkle.main.view.MainHoroscopeFragment.Companion.DAILY
import com.cyberflow.sparkle.main.view.MainHoroscopeFragment.Companion.MONTH
import com.cyberflow.sparkle.main.view.MainHoroscopeFragment.Companion.WEEKLY
import com.cyberflow.sparkle.main.view.MainHoroscopeFragment.Companion.YEAR
import com.cyberflow.sparkle.main.widget.calendar.DateBean
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Post
import com.drake.net.utils.scope
import com.drake.net.utils.withIO
import com.drake.net.utils.withMain
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class HoroscopeView : RecyclerView.ViewHolder {
    private val TAG = "HoroscopeView"

    private var birthDate : DateBean? = null
    private var currentDate : DateBean? = null

    constructor(itemView: View) : super(itemView) {
        initView(itemView)
        initData()
        CacheUtil.getUserInfo()?.user?.apply {
            var date: Date? = null
            val format = "yyyy-MM-dd"
            try{
//                Log.e(TAG, "initBirthDate: birthdate=$birthdate" )
                date = SimpleDateFormat(format).parse(birthdate)
            }catch (e: Exception){
                birthdate = "1990-01-01"
                date = SimpleDateFormat(format).parse(birthdate)
            }finally {
                val calendar = Calendar.getInstance()
                currentDate = DateBean(year = calendar[Calendar.YEAR], month = calendar[Calendar.MONTH] + 1, day = calendar[Calendar.DAY_OF_MONTH])
                calendar.time = date
                birthDate = DateBean(year = calendar[Calendar.YEAR], month = calendar[Calendar.MONTH] + 1, day = calendar[Calendar.DAY_OF_MONTH])
            }
        }
    }

    var mDatabind: ItemHoroscopeBannerBinding? = null

    private fun initView(itemView: View) {
        mDatabind = ItemHoroscopeBannerBinding.bind(itemView)
        mDatabind?.apply {
            rv.linear().setup {
                addType<HoroscopeItem>(R.layout.item_horoscope)
                addType<HoroscopeHeadItem>(R.layout.item_horoscope_head)
                addType<EmptyItem>(R.layout.item_horoscope_empty)
                onClick(R.id.left) { anima(INDEX_LOVE) }
                onClick(R.id.center) { anima(INDEX_FORTUNE) }
                onClick(R.id.right) { anima(INDEX_CAREER) }
                onBind {
//                Log.e("TAG", "modelCount: $modelCount  layoutPosition: $layoutPosition ")
                    when (itemViewType) {
                        R.layout.item_horoscope -> {
                            getBinding<ItemHoroscopeBinding>().apply {
                                getModel<HoroscopeItem>().line.apply {
                                    val left = if (this == 0) View.VISIBLE else View.INVISIBLE
                                    dotLeft.visibility = left
                                    lineLeftUp.visibility = left
                                    lineLeftDown.visibility = left

                                    val center = if (this == 1) View.VISIBLE else View.INVISIBLE
                                    dotCenter.visibility = center
                                    lineCenterUp.visibility = center
                                    lineCenterDown.visibility = center

                                    val right = if (this == 2) View.VISIBLE else View.INVISIBLE
                                    dotRight.visibility = right
                                    lineRightUp.visibility = right
                                    lineRightDown.visibility = right
                                }
                                if (modelCount == layoutPosition + 1) {   // handle footer view
                                    lineCenterDown.visibility = View.INVISIBLE
                                    lineLeftDown.visibility = View.INVISIBLE
                                    lineRightDown.visibility = View.INVISIBLE
                                }
                            }
                        }
                    }
                }
            }

            bgLove.setOnClickListener { anima(INDEX_LOVE) }
            bgFortune.setOnClickListener { anima(INDEX_FORTUNE) }
            bgCareer.setOnClickListener { anima(INDEX_CAREER) }

            barLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
//            Log.e("TAG", "verticalOffset: $verticalOffset" )
//            Log.e("TAG", "totel: ${appBarLayout.totalScrollRange}" )
                val rate = 1 - abs(verticalOffset) / appBarLayout.totalScrollRange.toFloat()
//            Log.e("TAG", "rate: $rate", )
                if (rate < 0.05f) layPan.alpha = 0.0f
                else layPan.alpha = 1.0f
            }
        }
    }

    // 外部切换   左右滑  切换周期的时候
    fun slideUpdate(currentPos: Int, realPos: Int) {
//        Log.e(TAG, "slideUpdate: currentPos=$currentPos  realPos=$realPos   $params")
        dailyTransform(params, currentPos, realPos)
    }

    private var params: HoroscopeReq? = null

    // 第一次初始化的时候  调用这个      选择模式  当前tab下标  日期
    fun injectData(reqData: HoroscopeReq) {
//        Log.e(TAG, "injectData: $reqData" )
        params = reqData
       /* if(reqData.initTabIdx == 0){
            slideUpdate(0, 0)
        }*/
        if(firstRequest){
            slideUpdate(reqData.initTabIdx, reqData.initTabIdx)
        }
    }

    // 计算相差的天数  得到一个新的 yyyy-MM-dd
    private fun dailyTransform(origin: HoroscopeReq?, currentPos: Int, realPos: Int){
        if(currentPos == params?.initTabIdx){
            origin?.param?.let {
                val diff = realPos - currentPos     // 例如 realPos=5  currentPos=2   相差3天     如果 realPos=-3  currentPos=0  相差-3 天

                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, it.year)
                calendar.set(Calendar.MONTH, it.month - 1)
                calendar.set(Calendar.DAY_OF_MONTH, it.day)

                when(params?.selectMode){
                    DAILY -> {
                        calendar.add(Calendar.DAY_OF_MONTH, diff)
                    }
                    WEEKLY -> {
                        calendar.add(Calendar.WEEK_OF_MONTH, diff)
                    }
                    MONTH -> {
                        calendar.add(Calendar.MONTH, diff)
                    }
                    YEAR -> {
                        calendar.add(Calendar.YEAR, diff)
                    }
                }

                val realYear = calendar.get(Calendar.YEAR)
                val realMonth = calendar.get(Calendar.MONTH) + 1
                val realDay = calendar.get(Calendar.DAY_OF_MONTH)

//                Log.e(TAG, "dailyTransform: before ${it.year}-${it.month}-${it.day}")
//                Log.e(TAG, "dailyTransform: after ${realYear}-${realMonth}-${realDay}")

                resetView()

                mDatabind?.tvHoroscopeTitle?.text = "${realYear}-${realMonth}-${realDay}"
                var needRequestData = true
                when(params?.selectMode){
                    DAILY -> {
                        val birth = birthDate!!.year * 365 + birthDate!!.month * 30 + birthDate!!.day
                        val today = currentDate!!.year * 365 + currentDate!!.month * 30 + currentDate!!.day
                        val select: Int = realYear * 365 + realMonth * 30 + realDay
                        if(select < birth){
                            needRequestData = false
                            showVoid()
                        }
                        if(select == birth){
                            needRequestData = false
                            showHappyBirthday()
                        }
                        if(select > today){
                            needRequestData = false
                            showFutureHoroscope()
                        }
                    }
                    WEEKLY -> {

                    }
                    MONTH -> {
                        val birth = birthDate!!.year * 365 + birthDate!!.month * 30
                        val today = currentDate!!.year * 365 + currentDate!!.month * 30
                        val select: Int = realYear * 365 + realMonth * 30

                        if(select < birth){
                            needRequestData = false
                            showVoid()
                        }
                        if(select == birth){
                            needRequestData = false
                            showHappyBirthday()
                        }
                        if(select > today){
                            needRequestData = false
                            showFutureHoroscope()
                        }
                    }
                    YEAR -> {
                        needRequestData = false
                        showCoraAnalyze()
                    }
                }
                if(needRequestData){
                    requestData(realYear, realMonth, realDay)
                }
            }
        }
    }

    private var requestYear = 0
    private var requestMonth = 0
    private var requestDay = 0
    private var firstRequest = true

    fun requestData(year: Int, month: Int, day: Int) {
        this.requestYear = year
        this.requestMonth = month
        this.requestDay = day
        firstRequest = false

//        Log.e(TAG, "requestData: requestYear=$requestYear \t requestMonth=$requestMonth \t requestDay=$requestDay " )

        mDatabind?.state?.apply {
            showLoading()
            scope {

                var requestKey = "${params?.selectMode}_${year}_${month}_${day}"
                val cache = DBManager.instance.db?.horoscopeCacheDao()?.fetch(requestKey) // get cache from db
//                Log.e(TAG, "---0--- requestKey=$requestKey  cache.requestKey=${cache?.requestKey}" )
                if(cache == null){
//                    Log.e(TAG, "---1--- we need request data from server " )
                    when (params?.selectMode) {
                        DAILY -> {
                            horoScopeData = Post<DailyHoroScopeData>(Api.DAILY_HOROSCOPE) {
                                json("date" to "${year}-${month}-${day}")  // YYYY-MM-DD
                            }.await()
                        }

                        WEEKLY -> {
                            horoScopeData = Post<DailyHoroScopeData>(Api.WEEKLY_HOROSCOPE) {
                                json("date" to "${year}-${month}-${day}")  // "2023-11-11"
                            }.await()
                        }

                        MONTH -> {
                            horoScopeData = Post<DailyHoroScopeData>(Api.MONTHLY_HOROSCOPE) {
                                json("year" to year, "month" to month)
                            }.await()
                        }

                        YEAR -> {
                            yearlyHoroScopeData = Post<YearlyHoroScopeData>(Api.YEARLY_HOROSCOPE) {
                                json("year" to year)   //2023
                            }.await()
                        }
                    }

                    // update cache
                    withIO {
                        val data = when (params?.selectMode) {
                            DAILY, WEEKLY , MONTH-> {
                                GsonConverter.gson.toJson(horoScopeData)
                            }
                            YEAR -> {
                                GsonConverter.gson.toJson(yearlyHoroScopeData)
                            }
                            else -> { null }
                        }
                        data?.also {
                            DBManager.instance.db?.horoscopeCacheDao()?.insert(DBHoroscope(requestKey, it))
                        }
                    }
                }else{
//                    Log.e(TAG, "---2--- we fetch data from local db " )
                    when (params?.selectMode) {
                        DAILY, WEEKLY , MONTH-> {
                            horoScopeData = GsonConverter.gson.fromJson(cache.data, DailyHoroScopeData::class.java)
                        }
                        YEAR -> {
                            yearlyHoroScopeData = GsonConverter.gson.fromJson(cache.data, YearlyHoroScopeData::class.java)
                        }
                    }
                }

                withMain { showData() }
            }
        }
    }

    private fun showData() {
        mDatabind?.state?.showContent()
        showData(horoScopeData)
    }

    // happy birthday
    // future horoscope
    private fun showEmptyView(){
        when(params?.selectMode){
            YEAR -> {
                showCoraAnalyze()
            }
            MONTH -> {

            }
            DAILY -> {

            }
        }
    }

    private fun showCoraAnalyze(){
        mDatabind?.apply {
            layMain.isVisible = false
            layEmpty.isVisible = true
            tvCoraAnalyzing.isVisible = true
            btnCoraCommingSoon.isVisible = true
            tvHint.isVisible = false
        }
    }

    private fun showVoid(){
        mDatabind?.apply {
            layMain.isVisible = false
            layEmpty.isVisible = false
            tvCoraAnalyzing.isVisible = false
            btnCoraCommingSoon.isVisible = false
            tvHint.isVisible = false
        }
    }

    private fun showHappyBirthday(){
        mDatabind?.apply {
            layMain.isVisible = false
            layEmpty.isVisible = true
            tvCoraAnalyzing.isVisible = false
            btnCoraCommingSoon.isVisible = false
            tvHint.isVisible = true
            tvHint.text = tvHint.context.getString(R.string.happy_birthday)
        }
    }

    private fun showFutureHoroscope(){
        mDatabind?.apply {
            layMain.isVisible = false
            layEmpty.isVisible = true
            tvCoraAnalyzing.isVisible = false
            btnCoraCommingSoon.isVisible = false
            tvHint.isVisible = true
            tvHint.text = tvHint.context.getString(R.string.feedback_contact)
        }
    }

    private fun initData() {
        mDatabind?.state?.onError {
            findViewById<ShadowTxtButton>(R.id.btn).setClickListener(object :
                ShadowTxtButton.ShadowClickListener {
                override fun clicked(disable: Boolean) {
                    requestData(requestYear, requestMonth, requestDay)
                }
            })
        }
    }

    private var horoScopeData: DailyHoroScopeData? = null
    private var yearlyHoroScopeData: YearlyHoroScopeData? = null

    private fun showData(data: DailyHoroScopeData?) {
        data?.also {
            mDatabind?.apply {
                layMain.visibility = View.VISIBLE
                layEmpty.visibility = View.GONE
                showTotalAnima(it.total_score)
                tvLove.text = it.love_score.toString()
                tvFortune.text = it.wealth_score.toString()
                tvCareer.text = it.career_score.toString()
                anima(INDEX_LOVE)
            }
        }
    }

    private fun resetView(){
        mDatabind?.apply {
            layMain.isVisible = false
            layEmpty.isVisible = false
            tvCoraAnalyzing.isVisible = false
            btnCoraCommingSoon.isVisible = false
            tvHint.isVisible = false
            showTotalAnima(0)
            tvLove.text = ""
            tvFortune.text = ""
            tvCareer.text = ""
        }
    }

    private fun showTotalAnima(dd: Int) {
        mDatabind?.smc?.setPercentWithAnimation(dd)

        ValueAnimator.ofInt(0, dd).apply {
            duration = (12 * dd).toLong()
            interpolator = LinearInterpolator()
            addUpdateListener {
                mDatabind?.tvRange?.text = it.animatedValue.toString()
            }
            start()
        }
    }

    private val arrays = arrayOf(
        com.cyberflow.base.resources.R.drawable.main_bg_page_half_left_200_radius,
        com.cyberflow.base.resources.R.drawable.main_bg_page_selected_0_radius,
        com.cyberflow.base.resources.R.drawable.main_bg_page_half_right_200_radius
    )

    private var lastIndex = -1
    private fun anima(index: Int = INDEX_LOVE) {
        //        Log.e("Click", "anima:  index=$index  lastIndex=$lastIndex")

        if (index == lastIndex) return

        mDatabind?.apply {
            rv.models = getData(index)

            val dis = bgCareer.width + dp2px(5f).toFloat()
            var st = lastIndex * dis
            var et = index * dis
            //        Log.e("TAG", "anima: dis=$dis")
            val anim = ObjectAnimator.ofFloat(bgTmp, "translationX", st, et).apply {
                duration = 100
                addUpdateListener {
                    //      Log.e("TAG", "anima: ${it.animatedValue}", )
                    if (it.animatedValue as Float > dis / 2) {
                        bgTmp.setBackgroundResource(arrays[index])
                    }
                }
            }
            lastIndex = index
            anim.start()
        }
    }


    companion object {
        const val INDEX_LOVE = 0
        const val INDEX_FORTUNE = 1
        const val INDEX_CAREER = 2
    }

    // 数据转化
    private fun getData(index: Int = INDEX_LOVE): List<Any> {
        val result = arrayListOf<Any>()
        horoScopeData?.also {
            val data = when (index) {
                INDEX_LOVE -> {
                    it.love_progress_list?.map { horo ->
                        HoroscopeItem(name = horo.title, desc = horo.content, line = index)
                    }
                }

                INDEX_FORTUNE -> {
                    it.wealth_progress_list?.map { horo ->
                        HoroscopeItem(name = horo.title, desc = horo.content, line = index)
                    }
                }

                INDEX_CAREER -> {
                    it.career_progress_list?.map { horo ->
                        HoroscopeItem(name = horo.title, desc = horo.content, line = index)
                    }
                }

                else -> {
                    arrayListOf<Any>()
                }
            }

            if (data.isNullOrEmpty()) {
                result.add(HoroscopeHeadItem())
                result.add(EmptyItem())
            } else {
                result.add(HoroscopeHeadItem())
                result.addAll(data)
            }
        }

        return result
    }
}