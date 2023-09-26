package com.cyberflow.sparkle

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.cyberflow.base.BaseApp
import com.cyberflow.sparkle.main.widget.NumView

object DBComponent {

    @BindingAdapter(value = ["colorStr", "numStr"], requireAll = false)
    @JvmStatic
    fun NumView.initDB(_color: String?, _num: String?) {
        try {
            color = Color.parseColor(_color)
            num = _num.orEmpty()
        } catch (_: Exception) {
        }
    }

    @BindingAdapter("layoutMarginBottomTop")
    @JvmStatic
    fun setLayoutMarginBottom(view: View, dimen: Float) {
        val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = dimen.toInt()
        view.layoutParams = layoutParams
    }

    @BindingAdapter(value = ["imgCircle", "holder"], requireAll = false)
    @JvmStatic
    fun loadImageCircle(v: ImageView, url: Any?, holder: Drawable?) {
        if (url == null && holder == null) v.setImageDrawable(null)
        Glide.with(v.context).load(url).circleCrop().placeholder(holder)
            .diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(false).into(v)
    }


    @SuppressLint("CheckResult")
    @BindingAdapter(value = ["img", "holder", "corner"], requireAll = false)
    @JvmStatic
    fun loadImageWithHolder(v: ImageView, url: Any?, holder: Drawable?, corner: Int?) {
        if (url == null && holder == null) v.setImageDrawable(null)
        val requestBuilder = Glide.with(v.context).load(url).placeholder(holder)
            .diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(false)
        if (corner != null) {
            requestBuilder.transform(CenterCrop(), RoundedCorners((corner * BaseApp.instance!!.resources.displayMetrics.density).toInt()))
        }
        requestBuilder.into(v)
    }


    @BindingAdapter(value = ["url"], requireAll = true)
    @JvmStatic
    fun loadImage(v: ImageView, url: Any?) {
        Glide.with(v.context).load(url).diskCacheStrategy(DiskCacheStrategy.ALL)
            .skipMemoryCache(false).into(v)
    }
}