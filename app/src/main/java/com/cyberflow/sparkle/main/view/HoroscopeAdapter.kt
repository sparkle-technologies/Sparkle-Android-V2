package com.cyberflow.sparkle.main.view

import android.view.LayoutInflater
import android.view.ViewGroup
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.main.widget.HoroscopeView
import com.youth.banner.adapter.BannerAdapter

class HoroscopeAdapter(imageUrls: List<String>) : BannerAdapter<String, HoroscopeView>(imageUrls) {

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): HoroscopeView {
        return HoroscopeView(LayoutInflater.from(parent!!.context).inflate(R.layout.item_horoscope_banner, parent, false))
    }

    override fun onBindView(holder: HoroscopeView, data: String, position: Int, size: Int) {
        holder.initData(data)
    }
}