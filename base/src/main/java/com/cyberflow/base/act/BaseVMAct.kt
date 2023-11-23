package com.cyberflow.base.act

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.cyberflow.base.BaseApp
import com.cyberflow.base.ext.COLOR_TRANSPARENT
import com.cyberflow.base.ext.immersive
import com.cyberflow.base.ext.notNull
import com.cyberflow.base.viewmodel.BaseViewModel
import com.hjq.language.MultiLanguages
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
        initView(savedInstanceState)
        initData()
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

    private fun transparentNavigationBar(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        var systemUiVisibility = window.decorView.systemUiVisibility
        systemUiVisibility =
            systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        window.decorView.systemUiVisibility = systemUiVisibility
        window.navigationBarColor = Color.TRANSPARENT

        //设置导航栏按钮或导航条颜色
//        setNavigationBarBtnColor(window, NightMode.isNightMode(window.context))
        setNavigationBarBtnColor(window, isNightMode(window.context))
    }

    private fun isNightMode(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    private fun setNavigationBarBtnColor(window: Window, night: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var systemUiVisibility = window.decorView.systemUiVisibility
            systemUiVisibility = if (night) { //白色按钮
                systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else { //黑色按钮
                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
            window.decorView.systemUiVisibility = systemUiVisibility
        }
    }
}