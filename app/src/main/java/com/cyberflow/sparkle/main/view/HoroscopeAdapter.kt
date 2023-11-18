package com.cyberflow.sparkle.main.view

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.main.widget.HoroscopeView
import com.cyberflow.sparkle.main.widget.calendar.DateBean
import com.youth.banner.adapter.BannerAdapter

class HoroscopeAdapter(imageUrls: List<HoroscopeReq>) : BannerAdapter<HoroscopeReq, HoroscopeView>(imageUrls) {

    private val TAG = "HoroscopeAdapter"

    private val views = arrayListOf<HoroscopeView>()


    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): HoroscopeView {
        Log.e(TAG, "onCreateHolder: viewType=$viewType" )
        val view = HoroscopeView(LayoutInflater.from(parent!!.context).inflate(R.layout.item_horoscope_banner, parent, false))
        views.add(view)
        return view
    }

    // 不知道为啥  设置了 offscreenPageLimit = 2  还是会多次创建 view
    override fun onBindView(holder: HoroscopeView, data: HoroscopeReq, position: Int, size: Int) {
        Log.e(TAG, "onBindView: position=$position  data=$data", )
        holder.injectData(data)
    }

    fun slideUpdate(position: Int, realPos: Int) {
        Log.e(TAG, "slideUpdate: size of view : ${views.size}")
        views.forEach {
            it.slideUpdate(position, realPos)
        }
    }
}

data class HoroscopeReq(val selectMode: Int, val initTabIdx: Int, var param: DateBean? = null)