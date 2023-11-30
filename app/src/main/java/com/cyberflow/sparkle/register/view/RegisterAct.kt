package com.cyberflow.sparkle.register.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.cyberflow.base.act.BaseVBAct
import com.cyberflow.base.model.RegisterRequestBean
import com.cyberflow.sparkle.MyApp
import com.cyberflow.sparkle.databinding.ActivityRegiserBinding
import com.cyberflow.sparkle.login.viewmodel.LoginRegisterViewModel
import com.hyphenate.easeui.input.util.ViewUtil

class RegisterAct : BaseVBAct<LoginRegisterViewModel, ActivityRegiserBinding>() {

    companion object{
        fun go(context: Context){
            val intent = Intent(context, RegisterAct::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
//        initAnim()

        var adapter = PageAdapter(supportFragmentManager, lifecycle)
        adapter.addFragment(SelectGenderFragment())
        adapter.addFragment(SelectBirthdayFragment())
        adapter.addFragment(BirthPlaceFragment())
        adapter.addFragment(NickNameFragment())
        mViewBind.pager.apply {
            isUserInputEnabled = false
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            this.adapter = adapter
            registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                   handleIndicator(position)
                }
            })
        }
//        mViewBind.dotsIndicator.attachTo(mViewBind.pager)

        mViewBind.root.setOnClickListener {
            mViewBind.pager.clearFocus()
            ViewUtil.hideKeyboard(this, mViewBind.pager)
        }
    }

    private fun handleIndicator(position: Int) {
         when(position){
             0 -> {
                 mViewBind.iv1.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_dot_1)
                 mViewBind.iv2.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_dot_empty)
                 mViewBind.iv3.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_dot_empty)
                 mViewBind.iv4.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_dot_empty)
             }
             1 -> {
                 mViewBind.iv1.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_dot_1)
                 mViewBind.iv2.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_dot_2)
                 mViewBind.iv3.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_dot_empty)
                 mViewBind.iv4.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_dot_empty)
             }
             2 -> {
                 mViewBind.iv1.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_dot_1)
                 mViewBind.iv2.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_dot_2)
                 mViewBind.iv3.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_dot_3)
                 mViewBind.iv4.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_dot_empty)
             }
             3 -> {
                 mViewBind.iv1.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_dot_1)
                 mViewBind.iv2.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_dot_2)
                 mViewBind.iv3.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_dot_3)
                 mViewBind.iv4.setImageResource(com.cyberflow.base.resources.R.drawable.register_ic_dot_4)
             }
         }
    }

    private fun initAnim() {
        /*val rotateDrawable = mViewBind.ivRotate.background as RotateDrawable
        ObjectAnimator.ofInt(rotateDrawable, "level", 0, 10000).apply {
            duration = 9000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }*/
    }

    override fun initData() {
        viewModel.previous.observe(this){
            Log.e(TAG, " previous changed $it " )
            goPrevious()
        }

        viewModel.next.observe(this){
            Log.e(TAG, " next changed $it " )
            goNext()
        }

        viewModel.registerBean = RegisterRequestBean()  // init request bean

        MyApp.instance.initGooglePlace()
    }

    private fun goPrevious(){
        Log.e(TAG, "goPrevious: ", )
        mViewBind.pager.apply {
            if(currentItem > 0){
                setCurrentItem(currentItem - 1, true)
            }
        }
    }

    private fun goNext(){
        Log.e(TAG, "goNext: ", )
        mViewBind.pager.apply {
            adapter?.also { a->
                if(currentItem < a.itemCount - 1){
                    setCurrentItem(currentItem + 1, true)
                }
            }
        }
    }
}

class PageAdapter(fm: FragmentManager, lifecycle: androidx.lifecycle.Lifecycle) :
    FragmentStateAdapter(fm, lifecycle) {

    private val fragmentList = arrayListOf<Fragment>()

    fun addFragment(fragment: Fragment) {
        fragmentList.add(fragment)
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

}