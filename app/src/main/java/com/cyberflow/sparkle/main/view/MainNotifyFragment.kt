package com.cyberflow.sparkle.main.view

import android.graphics.Color
import android.os.Bundle
import android.text.Layout
import android.text.StaticLayout
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.model.SiteMessage
import com.cyberflow.base.model.SiteMessageList
import com.cyberflow.base.net.Api
import com.cyberflow.base.util.toggleEllipsize
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.DBComponent
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.FragmentMainNotifyBinding
import com.cyberflow.sparkle.databinding.ItemSiteMessageBinding
import com.cyberflow.sparkle.databinding.ItemSiteMessageBodyBinding
import com.cyberflow.sparkle.flutter.FlutterProxyActivity
import com.cyberflow.sparkle.main.widget.ExpandTextView
import com.cyberflow.sparkle.mainv2.view.MainActivityV2
import com.cyberflow.sparkle.profile.view.ProfileAct
import com.drake.brv.PageRefreshLayout
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Post
import com.drake.net.utils.scope
import com.drake.net.utils.withMain
import com.drake.spannable.replaceSpanFirst
import com.drake.spannable.span.ColorSpan
import com.luck.picture.lib.utils.DateUtils
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.android.RenderMode
import io.flutter.embedding.android.TransparencyMode
import io.flutter.plugin.common.MethodChannel

class MainNotifyFragment : BaseDBFragment<BaseViewModel, FragmentMainNotifyBinding>() {

    private fun setSpan(tv: TextView ,txt: String, timeStamp: Long) {
        if(txt.isNullOrEmpty()) return
        val min =  (System.currentTimeMillis()/1000 - timeStamp) / 60f
        var timeTxt = ""
        if(min < 1){
            timeTxt = " ${getString(R.string.just_now)}"
        }else if(min <= 59){
            timeTxt = " ${min.toInt()} ${getString(R.string.minutes)}"
        }else{
            val hours = min / 60
            if(hours <= 23){
                timeTxt = " ${hours.toInt()} ${getString(R.string.hours)}"
            }else{
                val days = hours / 24
                if(days <= 6){
                    timeTxt = " ${days.toInt()} ${getString(R.string.days)}"
                }else{
                    val week = days / 7
                    timeTxt = " ${week.toInt()} ${getString(R.string.weeks)}"
                }
            }
        }
        toggleEllipsize(requireContext(), tv, 2, txt, timeTxt, com.cyberflow.base.resources.R.color.color_7D7D80)
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDatabind.rv.linear().setup {
            addType<String>(R.layout.item_site_message_header)
            addType<SiteMessageList>(R.layout.item_site_message_body)
            onCreate {
                when (itemViewType) {
                    R.layout.item_site_message_body->{
                        getBinding<ItemSiteMessageBodyBinding>().rv.linear().setup {
                            addType<SiteMessage>(R.layout.item_site_message)
                            onBind {
                                getBinding<ItemSiteMessageBinding>().apply {
                                    val model = getModel<SiteMessage>()
                                    DBComponent.loadAvatar(ivHead, model.`object`?.avatar, 1)
                                    tvFriendName.text = model.`object`?.nick
                                    setSpan(tvMsg, model.text, model.timestamp)
                                    line.visibility = if (layoutPosition == modelCount - 1) View.INVISIBLE else View.VISIBLE
                                    item.setOnClickListener {
                                        when(model.message_type){
                                            1->{
                                                ProfileAct.go(requireContext(), model.`object`?.open_uid.orEmpty())
                                            }
                                            9->{
                                                ProfileAct.go(requireContext(), model.`object`?.open_uid.orEmpty())
                                            }
                                            10->{
                                                ProfileAct.go(requireContext(), model.`object`?.open_uid.orEmpty())
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            onBind {
                when (itemViewType) {
                    R.layout.item_site_message_body->{
                        val model = getModel<SiteMessageList>()
                        getBinding<ItemSiteMessageBodyBinding>().rv.models = model.list
                    }
                }
            }
        }
    }

    fun freshData(){
        mDatabind.page.autoRefresh()
    }

    override fun initData() {
        mDatabind.page.onRefresh {
            index = 1
            request()
        }
        mDatabind.page.onLoadMore {
            if(index*30 < total){
                index++
                request()
            }else{
                mDatabind.page.finishLoadMore()
            }
        }
        mDatabind.page.autoRefresh()
    }

    private fun PageRefreshLayout.request() {
        scope {
            val data = Post<SiteMessageList>(Api.SITE_MESSAGE) {
                json("page" to index, "page_size" to 30)
            }.await()
            withMain {
                handleData(data)
            }
        }
    }

    private var total = 0
    private fun handleData(data: SiteMessageList){
        total = data.total
        if(mDatabind.page.index == 1 && data.list.isNullOrEmpty()){
            mDatabind.page.showEmpty()
            (requireActivity() as? MainActivityV2)?.setSiteUnRead(0)
            return
        }

        val today = arrayListOf<SiteMessage>()
        val yesterday = arrayListOf<SiteMessage>()
        val earlier = arrayListOf<SiteMessage>()
        val calendarNow = System.currentTimeMillis()
        var unRead = 0
        data.list?.forEach {
            val diff = DateUtils.dayDiff(calendarNow, it.timestamp * 1000)  // cause timestamp is second, need multi 1000
//            Log.e("TAG", "handleData: diff=$diff", )

            when(diff){
                0->{ today.add(it) }
                1->{ yesterday.add(it) }
                else->{ earlier.add(it) }
            }

            if(!it.have_read){
                unRead++
            }
        }
        val rvData = arrayListOf<Any>()
        if(today.isNotEmpty()){
            rvData.add(getString(R.string.stoday))
            rvData.add(SiteMessageList(list=today))
        }
        if(yesterday.isNotEmpty()){
            rvData.add(getString(R.string.yesterday))
            rvData.add(SiteMessageList(list=yesterday))
        }
        if(earlier.isNotEmpty()){
            rvData.add(getString(R.string.earlier))
            rvData.add(SiteMessageList(list=earlier))
        }
        mDatabind.rv.models = rvData
        mDatabind.page.showContent()
        (requireActivity() as? MainActivityV2)?.setSiteUnRead(unRead)
    }

    /********************************************************************************/
    private var methodChannel : MethodChannel? = null

    private fun initFlutter() {
        methodChannel = FlutterProxyActivity.prepareFlutterEngine(requireActivity(), FlutterProxyActivity.ENGINE_ID_NOTIFICATION_LIST, FlutterProxyActivity.ROUTE_NOTIFICATION_LIST, FlutterProxyActivity.CHANNEL_NOTIFICATION, FlutterProxyActivity.SCENE_NOTIFICATION_LIST) { scene, method, call, result ->
            FlutterProxyActivity.handleFlutterCommonEvent(requireActivity(), scene, method, call, result)
        }
        val fragment = FlutterFragment.withCachedEngine(FlutterProxyActivity.ENGINE_ID_NOTIFICATION_LIST)
            .renderMode(RenderMode.texture)
            .transparencyMode(TransparencyMode.transparent)
            .build<FlutterFragment>()
        requireActivity()
            .supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()
    }
}