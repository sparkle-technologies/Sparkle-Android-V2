package com.cyberflow.sparkle.register.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.cyberflow.base.act.BaseVBAct
import com.cyberflow.sparkle.databinding.ActivityRegiserBinding
import com.cyberflow.sparkle.login.viewmodel.LoginRegisterViewModel


class RegisterAct : BaseVBAct<LoginRegisterViewModel, ActivityRegiserBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        var adapter = PageAdapter(supportFragmentManager, lifecycle)
        adapter.addFragment(SelectGenderFragment())
        adapter.addFragment(SelectBirthdayFragment())
        adapter.addFragment(NickNameFragment())
        mViewBind.pager.apply {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            this.adapter = adapter
        }
        mViewBind.dotsIndicator.attachTo(mViewBind.pager)
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