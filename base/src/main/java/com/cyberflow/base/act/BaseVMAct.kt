package com.cyberflow.base.act

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.cyberflow.base.BaseApp
import com.cyberflow.base.ext.COLOR_TRANSPARENT
import com.cyberflow.base.ext.immersive
import com.cyberflow.base.ext.notNull
import com.cyberflow.base.ext.transparentNavigationBar
import com.cyberflow.base.net.NetworkingErrorHandler
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.PageConst
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.widget.NotificationDialog
import com.cyberflow.sparkle.widget.ToastDialogHolder
import com.hjq.language.MultiLanguages
import com.therouter.TheRouter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class BaseVMAct<VM : BaseViewModel> : AppCompatActivity() {

    companion object {
        val TAG = javaClass.simpleName.toString()
    }

    abstract fun layoutID(): Int
    lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDataBind().notNull({
            setContentView(it)
        }, {
            setContentView(layoutID())
        })
        viewModel = createViewModel(this)
        immersive(COLOR_TRANSPARENT, true)
        transparentNavigationBar(window)
//        setNavigationBar(false)
        initView(savedInstanceState)
        initData()
        addNetworkErrorHandle()
    }



    private fun createViewModel(viewModelStoreOwner: ViewModelStoreOwner): VM {
        var modelClass: Class<VM>?
        val type: Type? = javaClass.genericSuperclass
        modelClass = if (type is ParameterizedType) {
            type.actualTypeArguments[0] as? Class<VM>
        } else null
        if (modelClass == null) {
            modelClass = BaseViewModel::class.java as Class<VM>
        }
        return ViewModelProvider(
            viewModelStoreOwner,
            ViewModelProvider.AndroidViewModelFactory(BaseApp.instance!!)
        ).get(modelClass)
    }

    abstract fun initView(savedInstanceState: Bundle?)

    abstract fun initData()

    open fun initDataBind(): View? {
        return null
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(MultiLanguages.attach(newBase))
    }

    private fun addNetworkErrorHandle() {
        LiveDataBus.get().with(NetworkingErrorHandler.EVENT_TOKEN_EXPIRED, String::class.java).observe(this) {

            CacheUtil.setUserInfo(null)
            CacheUtil.savaString(CacheUtil.LOGIN_METHOD, "")
            CacheUtil.savaString(CacheUtil.UNIPASS_PUBK, "")
            CacheUtil.savaString(CacheUtil.UNIPASS_PRIK, "")

            TheRouter.build(PageConst.App.PAGE_LOGIN).navigation()
            finish()
        }

        // for request error
        LiveDataBus.get().with(NetworkingErrorHandler.EVENT_REQUEST_ERROR, String::class.java).observe(this) {
            toastError(it)
        }

        // for global event
        LiveDataBus.get().with(NotificationDialog.EVENT_SUCCESS, String::class.java).observe(this) {
            toastSuccess(it)
        }

        LiveDataBus.get().with(NotificationDialog.EVENT_WARN, String::class.java).observe(this) {
            toastWarn(it)
        }

        LiveDataBus.get().with(NotificationDialog.EVENT_ERROR, String::class.java).observe(this) {
            toastError(it)
        }
    }

    fun toastSuccess(msg: String){
        myToast(NotificationDialog.TYPE_SUCCESS, msg)
    }
    fun toastWarn(msg: String){
        myToast(NotificationDialog.TYPE_WARN, msg)
    }
    fun toastError(msg: String){
        myToast(NotificationDialog.TYPE_ERROR, msg)
    }
    fun myToast(type: Int = NotificationDialog.TYPE_SUCCESS, msg: String) {
        ToastDialogHolder.getDialog()?.show(applicationContext, type, msg)
    }
}