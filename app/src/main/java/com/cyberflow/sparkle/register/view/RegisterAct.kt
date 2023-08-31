package com.cyberflow.sparkle.register.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.cyberflow.base.act.BaseVBAct
import com.cyberflow.sparkle.databinding.ActivityRegiserBinding
import com.cyberflow.sparkle.login.viewmodel.LoginViewModel


class RegisterAct : BaseVBAct<LoginViewModel, ActivityRegiserBinding>() {

    override fun initView(savedInstanceState: Bundle?) {

        var adapter = PageAdapter(supportFragmentManager, lifecycle)
        adapter.addFragment(SelectGenderFragment())
        adapter.addFragment(SelectBirthdayFragment())
        mViewBind.pager.apply {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            this.adapter = adapter
        }
        mViewBind.dotsIndicator.attachTo(mViewBind.pager)
    }

    override fun initData() {

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