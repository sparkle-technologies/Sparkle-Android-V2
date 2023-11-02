package com.hyphenate.easeui.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.hyphenate.easeui.R
import com.luck.picture.lib.engine.ImageEngine

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class GlideEngine private constructor() : ImageEngine {
    override fun loadImage(context: Context, url: String, imageView: ImageView) {
        api.load(context, url, imageView, GlideLoadStrategy.Simple)
    }

    override fun loadImage(
        context: Context,
        imageView: ImageView,
        url: String,
        maxWidth: Int,
        maxHeight: Int
    ) {
        api.load(context, url, imageView, GlideLoadStrategy.OverrideOnly(maxWidth, maxHeight))
    }

    override fun loadAlbumCover(context: Context, url: String, imageView: ImageView) {
        api.load(context, url, imageView, GlideLoadStrategy.AlbumCover)
    }

    override fun loadGridImage(context: Context, url: String, imageView: ImageView) {
        api.load(context, url, imageView, GlideLoadStrategy.GridImage)
    }

    override fun pauseRequests(context: Context) {
        api.pauseRequests(context)
    }

    override fun resumeRequests(context: Context) {
        api.resumeRequests(context)
    }

    private object InstanceHolder {
        val instance = GlideEngine()
    }


    companion object {
        fun createGlideEngine(): ImageEngine {
            return InstanceHolder.instance
        }

        val api: IGlideApi by lazy { YXGlideImpl() }
    }
}

/**
 * Glide的二次封装主要解决以下问题
 * 1.使用策略模式拆分不同加载方式，修改策略不影响其他策略，增加全局hook点
 * 2.对业务屏蔽glide的细节，去除间接依赖，提升编译速度
 * 3.简化业务层调用，鼓励进行复用
 */
interface IGlideApi {
    fun load(
        host: Any,
        loadable: Any,
        imageView: ImageView,
        strategy: GlideLoadStrategy = GlideLoadStrategy.Simple
    )

    fun pauseRequests(host: Any)

    fun resumeRequests(host: Any)
}

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
private class YXGlideImpl : IGlideApi {

    override fun load(
        host: Any,
        loadable: Any,
        imageView: ImageView,
        strategy: GlideLoadStrategy
    ) {
        getRequestManager(host)?.let {
            strategy.handle(it, loadable, imageView)
        }
    }

    override fun pauseRequests(host: Any) {
        getRequestManager(host)?.pauseRequests()
    }

    override fun resumeRequests(host: Any) {
        getRequestManager(host)?.resumeRequests()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun getRequestManager(host: Any): RequestManager? {
        return when (host) {
            is Activity -> {
                safeGetRequestManager(host)
            }
            is Fragment -> {
                if (!host.isAdded || host.isRemoving) {
                    null
                } else {
                    Glide.with(host)
                }
            }
            is Context -> {
                safeGetRequestManager(host)
            }
            is View -> {
                safeGetRequestManager(host.context)
            }
            else -> null
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun safeGetRequestManager(host: Context): RequestManager? {
        val needToShow = host.getWrappedActivity()?.let { (!it.isDestroyed && !it.isFinishing) } ?: true
        return if (needToShow) Glide.with(host) else null
    }
}

sealed class GlideLoadStrategy {

    open fun handle(r: RequestManager, loadable: Any, iv: ImageView) {
        r.load(loadable).into(iv)
    }

    object Simple : GlideLoadStrategy() {
        override fun handle(r: RequestManager, loadable: Any, iv: ImageView) {
            r.load(loadable).into(iv)
        }
    }

    class OverrideOnly(private val w: Int, private val h: Int) : GlideLoadStrategy() {
        override fun handle(r: RequestManager, loadable: Any, iv: ImageView) {
            r.load(loadable).override(w, h).into(iv)
        }
    }

    object AlbumCover : GlideLoadStrategy() {
        override fun handle(r: RequestManager, loadable: Any, iv: ImageView) {
            r.asBitmap().load(loadable)
                .override(180, 180)
                .sizeMultiplier(0.5f)
                .transform(CenterCrop(), RoundedCorners(8))
                .placeholder(android.R.drawable.ic_menu_camera)
                .into(iv)
        }
    }

    object GridImage : GlideLoadStrategy() {
        override fun handle(r: RequestManager, loadable: Any, iv: ImageView) {
            r.load(loadable)
                .override(200, 200)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_camera)
                .into(iv)
        }
    }

    open class PlaceHolderAvatar(private val placeholder: Int) : GlideLoadStrategy() {
        override fun handle(r: RequestManager, loadable: Any, iv: ImageView) {
            r.load(loadable)
                .override(200, 200)
                .centerCrop()
                .placeholder(placeholder)
                .error(placeholder)
                .thumbnail()
                .into(iv)
        }
    }

    object Avatar : PlaceHolderAvatar(R.drawable.svg_ic_avatar_default)
}