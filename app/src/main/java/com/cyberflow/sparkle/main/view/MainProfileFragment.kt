package com.cyberflow.sparkle.main.view

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.model.BondDetail
import com.cyberflow.base.model.DetailResponseData
import com.cyberflow.base.model.IMSearchData
import com.cyberflow.base.model.ManyImageData
import com.cyberflow.base.model.RecommandFriendList
import com.cyberflow.base.model.User
import com.cyberflow.base.net.Api
import com.cyberflow.base.resources.R
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.ToastUtil
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.util.bus.SparkleEvent
import com.cyberflow.base.util.dp2px
import com.cyberflow.sparkle.DBComponent
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
import com.cyberflow.sparkle.databinding.FragmentMainProfileBinding
import com.cyberflow.sparkle.flutter.FlutterProxyActivity
import com.cyberflow.sparkle.im.DBManager
import com.cyberflow.sparkle.im.view.ChatActivity
import com.cyberflow.sparkle.im.view.IMAddFriendAct
import com.cyberflow.sparkle.im.viewmodel.IMViewModel
import com.cyberflow.sparkle.main.widget.SynastryDialog
import com.cyberflow.sparkle.mainv2.view.MainActivityV2
import com.cyberflow.sparkle.profile.view.ProfileAct
import com.cyberflow.sparkle.profile.view.ProfileAct.Companion.ACCEPT_FRIEND
import com.cyberflow.sparkle.profile.view.ProfileAct.Companion.ADD_FRIEND
import com.cyberflow.sparkle.profile.view.ProfileAct.Companion.CHAT
import com.cyberflow.sparkle.profile.view.ShareAct
import com.cyberflow.sparkle.setting.view.ConnectedAccountActivity
import com.cyberflow.sparkle.setting.view.SettingsActivity
import com.cyberflow.sparkle.widget.NotificationDialog
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.cyberflow.sparkle.widget.ToastDialogHolder
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.net.utils.withMain
import com.drake.spannable.addSpan
import com.drake.spannable.movement.ClickableMovementMethod
import com.drake.spannable.replaceSpan
import com.drake.spannable.setSpan
import com.drake.spannable.span.CenterImageSpan
import com.drake.spannable.span.ColorSpan
import com.drake.spannable.span.HighlightSpan
import com.google.android.flexbox.FlexboxLayoutManager
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.android.RenderMode
import io.flutter.embedding.android.TransparencyMode
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.launch
import me.jessyan.autosize.utils.ScreenUtils

class MainProfileFragment : BaseDBFragment<IMViewModel, FragmentMainProfileBinding>() {

    private val TAG = "MainProfileFragment"

    private fun hideOrShowAllIcons() {
        mDatabind.apply {
            btnSetting.isVisible = isMySelf
            btnWallet.isVisible = isMySelf
            btnSharePurple.isVisible = !isMySelf

            btnLeft.isVisible = true

            if(isMySelf){
                btnLeft.setViewTxt(getString(com.cyberflow.sparkle.R.string.go_to_closet))
                btnRight.isVisible = true
                btnRight.setViewTxt(getString(com.cyberflow.sparkle.R.string.share))
            }else{
                when(action){
                    CHAT -> {
                        btnLeft.setViewTxt(getString(R.string.chat))
                    }
                    ADD_FRIEND -> {
                        btnLeft.setViewTxt(getString(R.string.add_friend))
                    }
                    ACCEPT_FRIEND -> {
                        btnLeft.setViewTxt(getString(R.string.accept_friend))
                    }
                }
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDatabind.llBack.setOnClickListener {
            if(mActivity is MainActivityV2){

            }else{
                mActivity.finish()
            }
        }

        mDatabind.btnSetting.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                SettingsActivity.go(requireActivity())
            }
        })

        mDatabind.btnSharePurple.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                goShare()
            }
        })

        mDatabind.btnWallet.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                ToastUtil.show(mActivity, "钱包还没做")
            }
        })

        mDatabind.btnLeft.setClickListener(object : ShadowTxtButton.ShadowClickListener{
            override fun clicked(disable: Boolean) {
                Log.e(TAG, "clicked: action=$action   user=$user  ")
                Log.e(TAG, "clicked: openUid=${user?.open_uid}   user.name=${user?.nick}  ")
                if(isMySelf){
                    ToastUtil.show(mActivity, "跳橱窗 还没做")
                }else{
                    when (action) {
                        CHAT -> {   // go chatActivity   avatar nickName
                            user?.also {
                                ChatActivity.launch(mActivity, open_uid.replace("-", "_"), it.avatar, it.nick)
                            }
                        }

                        ADD_FRIEND -> {  // go add friend activity  -- IMSearchData
                            user?.also {
                                IMSearchData(
                                    nick = it.nick,
                                    avatar = it.avatar,
                                    gender = it.gender,
                                    open_uid = it.open_uid,
                                    ca_wallet = it.ca_wallet,
                                    wallet_address = it.wallet_address
                                ).apply {
                                    IMAddFriendAct.go(mActivity, this)
                                }
                            }
                        }

                        ACCEPT_FRIEND -> {  // EMMessage
                            viewModel.acceptFriend(IMDataManager.instance.getOpenUidProfile())
                        }
                    }
                }
            }
        })

        mDatabind.btnRight.setClickListener(object : ShadowTxtButton.ShadowClickListener{
            override fun clicked(disable: Boolean) {
                if(isMySelf){
                    goShare()
                }else{
                    reavelRelation()
                }
            }
        })

        mDatabind.rvRecommand.divider {
            orientation = DividerOrientation.HORIZONTAL
            setDivider(10, true)
        }.setup {
            addType<com.cyberflow.base.model.RecommandFriend>(com.cyberflow.sparkle.R.layout.item_profile_friend_recommand)
            addType<String>(com.cyberflow.sparkle.R.layout.item_profile_friend_recommand_empty)
        }.onClick(com.cyberflow.sparkle.R.id.lay_go_chat){
//                        ToastUtil.show(requireContext(), "跳转profile页")
            val model = getModel<com.cyberflow.base.model.RecommandFriend>()

            (requireActivity() as? ProfileAct)?.apply {
                destroyFlutter()
                lifecycleScope.launch {
                    val conversationCache = DBManager.instance.db?.imConversationCacheDao()?.getAll()
                    val friendRequestCache = DBManager.instance.db?.imFriendRequestDao()?.getAll()
                    var ac = ADD_FRIEND
                    val conversation = conversationCache?.filter {
                        it.open_uid == open_uid
                    }
                    val request = friendRequestCache?.filter {
                        it.from_open_uid == open_uid
                    }
                    if(conversation?.isNotEmpty() == true){
                        ac = CHAT
                    }
                    if(request?.isNotEmpty() == true){
                        ac = ACCEPT_FRIEND
                    }
                    refresh(model.open_uid, ac)
                }
            }

            (requireActivity() as? MainActivityV2)?.apply {
                ProfileAct.go(requireActivity(), model.open_uid.orEmpty())
            }
        }
    }

    private fun goShare() {
        user?.also {
            IMDataManager.instance.setUser(it)
            ShareAct.go(mActivity, serverImageUrl)
        }
    }

    private var open_uid = ""
    fun setOpenUid(_action: Int = CHAT, _openUid: String) {
        Log.e(TAG, "setOpenUid: ")
        action = _action
        open_uid = _openUid
    }

    private var user: User? = null

    private var action = CHAT

    override fun initData() {
        if (open_uid.isNullOrEmpty()) {  // 加载自己的主页
            open_uid = CacheUtil.getUserInfo()?.user?.open_uid.orEmpty()
        }

        loadProfile()

        viewModel.acceptFriendObservable.observe(this) {
            if (!it.isNullOrEmpty()) {
                action = CHAT
                viewModel.IM_acceptFriend(it)
            }
            loadProfile()
        }

        LiveDataBus.get().apply {
            with(SparkleEvent.PROFILE_CHANGED, String::class.java).observe(
                mActivity,
                this@MainProfileFragment::profileDataChanged
            )
        }
    }

    private fun profileDataChanged(s: String) {
        loadProfile()
    }

    //    private var open_uid = ""
    private var isMySelf = false

    private fun loadProfile() {
        val cacheUser = CacheUtil.getUserInfo()?.user

        isMySelf = open_uid == cacheUser?.open_uid && open_uid.isNotEmpty()

        Log.e(TAG, "loadProfile: isMySelf=$isMySelf")

        mDatabind.llBack.isVisible = !isMySelf

        mDatabind.apply {
//            flutterSynastry.isVisible = false
//            flutterAstroCode.isVisible = false
//            btnLeft.isVisible = false
            btnRight.isVisible = false
            layRecommand.isVisible = false
        }

        if (isMySelf) {  // if user is me  - img logic
            val cache = CacheUtil.getString(CacheUtil.AVATAR_BIG)
            Log.e(TAG, "loadProfile: cache img: $cache")
            if (cache.isNullOrEmpty()) {
                requestImg()
            } else {
                loadBigImg(cache)
                requestImg()
            }
            showUserInfo(cacheUser)
        }else{
            requestDetail()
            requestSynastryDetail()
        }
    }

    private fun requestImg() {
        scopeNetLife {
            val data = Post<ManyImageData>(Api.GET_IMAGE_URLS) {
                json("open_uid" to open_uid)
            }.await()
            data?.let {
                it.image_list?.profile_native?.apply {
                    if (isMySelf) {
                        CacheUtil.savaString(CacheUtil.AVATAR_BIG, this)
                    }
                    serverImageUrl = this
                    loadBigImg(this)
                }
            }
        }
    }

    private var serverImageUrl: String? = null

    private fun loadBigImg(url: String) {
        val genderRes = if(user?.gender == 1) com.cyberflow.sparkle.R.drawable.profile_default_avatar_man else com.cyberflow.sparkle.R.drawable.profile_default_avatar_women
        val holder = ResourcesCompat.getDrawable(resources, genderRes, null)
        DBComponent.loadImageWithHolder(mDatabind.ivAvatar, url, holder, 24)
    }

    private fun requestDetail() {
        scopeNetLife {
            val data = Post<DetailResponseData>(Api.USER_DETAIL) {
                json("open_uid" to open_uid)
            }.await()

            data?.let {
                showUserInfo(it.user)
            }

            requestImg()
        }
    }

    private fun setSpan(tv: TextView) {
        tv.movementMethod = ClickableMovementMethod.getInstance()
        val measurePaint = tv.paint
        val pWidth = measurePaint.measureText("There‘s no signature yet, please go to image edit")
        val screenWidth = ScreenUtils.getScreenSize(mActivity)[0]
        val marLeft = dp2px(20f)
        val marRight = dp2px(20f)
//        Log.e(TAG, " pWidth=$pWidth  screenWidth=$screenWidth  marLeft=$marLeft  marRight=$marRight" )
        val breakLine = if (pWidth < (screenWidth - marLeft - marRight)) "" else "\n"
        tv.text = ("There‘s no signature yet, please go to $breakLine").setSpan(ColorSpan("#000000"))
                .addSpan("image", CenterImageSpan(mActivity, R.drawable.profile_ic_edit).setDrawableSize(dp2px(15f)).setMarginHorizontal(dp2px(2f)))
                .addSpan(" edit")
                .replaceSpan("image") {
                    HighlightSpan("#8B82DB") {
                        goEditProfile() }
                }.replaceSpan("edit") {
                    HighlightSpan("#8B82DB") {
                        goEditProfile()
                    }
                }
    }

    private var editMethodChannel: MethodChannel? = null

    private fun initFlutter() {
        editMethodChannel = FlutterProxyActivity.prepareFlutterEngine(mActivity, FlutterProxyActivity.ENGINE_ID_EDIT_PROFILE, FlutterProxyActivity.ROUTE_EDIT_PROFILE, FlutterProxyActivity.CHANNEL_SETTING, FlutterProxyActivity.SCENE_PROFILE_EDIT) { scene, method, call, result ->
            handleFlutterEvent(scene, method, call, result)
        }
    }

    private fun handleFlutterEvent(
        scene: Int, method: MethodChannel, call: MethodCall, result: MethodChannel.Result
    ) {
        if (call.method == "flutterToast") {
            val type = call.argument<Int>("type") ?: NotificationDialog.TYPE_SUCCESS
            val content = call.argument<String>("content")
            if (content?.isNotEmpty() == true) {
                ToastDialogHolder.getDialog()?.show(mActivity, type, content)
            }
            result.success("success")
        } else {
            FlutterProxyActivity.handleFlutterCommonEvent(requireActivity(), scene, method, call, result)
        }
    }

    private var isFirstTimeForEdit = true

    private fun goEditProfile() {
        if (isFirstTimeForEdit) {
            isFirstTimeForEdit = false
        } else {
            editMethodChannel?.let {
                FlutterProxyActivity.initParams(
                    FlutterProxyActivity.SCENE_PROFILE_EDIT,
                    it
                )
            }
        }
        FlutterProxyActivity.go(mActivity, FlutterProxyActivity.ENGINE_ID_EDIT_PROFILE)
    }


    private fun showUserInfo(_user: User?) {
        _user?.also { data ->
            this.user = data
            shouldRequestRecommand()
            hideOrShowAllIcons()

            if(!isMySelf){
                val txt  = "${getString(com.cyberflow.sparkle.R.string.you)} & ${user?.nick}"
                mDatabind.btnRight.setViewTxt(txt)
            }


            mDatabind.apply {
                btnName.text= data.nick

                if(data.label_list.isNullOrEmpty()){
                    layLabelAdd.isVisible = isMySelf
                    lvLabel.isVisible = false
                }else{
                    layLabelAdd.isVisible = false
                    lvLabel.isVisible = true
                    lvLabel.layoutManager = FlexboxLayoutManager(mActivity)
                    lvLabel.setup {
                        addType<String>(com.cyberflow.sparkle.R.layout.item_profile_label)
                    }.models = data.label_list
                }

                val twitter = data?.bind_list?.filter {
                    it.type == "Twitter"   // Discord
                }?.getOrNull(0)

                layTwitter.isVisible = false
                if(isMySelf){
                    if(twitter != null){
                        tvTwitter.text = "@ ${twitter.nick}"
                    }else{
//                        tvTwitter.paint.flags = Paint.ANTI_ALIAS_FLAG
//                        tvTwitter.paint.isAntiAlias = true
                        tvTwitter.text = Html.fromHtml("<u>Add Twitter</u>", Html.FROM_HTML_MODE_COMPACT)
                        layTwitter.setOnClickListener {
                            ConnectedAccountActivity.go(requireActivity())
                        }
                    }
                    layTwitter.isVisible = true
                }else{
                    if(twitter!=null){
                        tvTwitter.text = "@ ${twitter.nick}"
                        layTwitter.isVisible = true
                    }
                }

                if (data.signature.isNotEmpty()) {
                    tvContent.text = data.signature
                } else {
                    if (isMySelf) {
                        setSpan(tvContent)
                        initFlutter()
                    } else {
                        tvContent.text = "${if(data.gender == 1) "He" else "She"} has not yet constructed the bio."
                    }
                }

                if(user?.star_sign.isNullOrEmpty()){
//                    fragmentHoroscopeContainer.isVisible = false
                }else{
                    flutterAstroCode.isVisible = true
                    initAstroCodeFlutter()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        destroyFlutter()
    }

    private fun destroyFlutter(){
        FlutterFragment.withCachedEngine(FlutterProxyActivity.ENGINE_ID_ASTRO_CODE).destroyEngineWithFragment(true)
        FlutterFragment.withCachedEngine(FlutterProxyActivity.ENGINE_ID_SYNASTRY).destroyEngineWithFragment(true)
    }

    private fun initAstroCodeFlutter() {
       val methodChannel = FlutterProxyActivity.prepareFlutterEngine(requireActivity(), FlutterProxyActivity.ENGINE_ID_ASTRO_CODE, FlutterProxyActivity.ROUTE_ASTRO_CODEE, FlutterProxyActivity.CHANNEL_START_SIGN, FlutterProxyActivity.SCENE_ASTRO_CODE) { scene, method, call, result ->
           if (call.method == "flutterOpenFlutterVC") {
               result.success("success")
               val route = call.argument<String>("route")
               val params = call.argument<Map<String, Any>>("params")
               openFlutterVC(route, params)
           }else{
               FlutterProxyActivity.handleFlutterCommonEvent(requireActivity(), scene, method, call, result)
           }
        }

        val fragment = FlutterFragment.withCachedEngine(FlutterProxyActivity.ENGINE_ID_ASTRO_CODE)
            .renderMode(RenderMode.texture)
            .transparencyMode(TransparencyMode.transparent)
            .build<FlutterFragment>()

        requireActivity()
            .supportFragmentManager.beginTransaction()
            .add(com.cyberflow.sparkle.R.id.fragment_astro_code_container, fragment)
            .commit()
    }

    private fun initSyNastryFlutter() {
       val methodChannel = FlutterProxyActivity.prepareFlutterEngine(requireActivity(), FlutterProxyActivity.ENGINE_ID_SYNASTRY, FlutterProxyActivity.ROUTE_SYNASTRY, FlutterProxyActivity.CHANNEL_SYNASTRY, FlutterProxyActivity.SCENE_SYNASTRY) { scene, method, call, result ->
           if (call.method == "flutterInitalized") {
               result.success("success")
               FlutterProxyActivity.initParams(open_uid, method)
           }else if (call.method == "flutterOpenFlutterVC") {
               result.success("success")
               val route = call.argument<String>("route")
               val params = call.argument<Any>("params")
               openFlutterVC(route, params)
           } else{
               FlutterProxyActivity.handleFlutterCommonEvent(requireActivity(), scene, method, call, result)
           }
        }

        val fragment = FlutterFragment.withCachedEngine(FlutterProxyActivity.ENGINE_ID_SYNASTRY)
            .renderMode(RenderMode.texture)
            .transparencyMode(TransparencyMode.transparent)
            .build<FlutterFragment>()

        requireActivity()
            .supportFragmentManager.beginTransaction()
            .add(com.cyberflow.sparkle.R.id.fragment_synastry_container, fragment)
            .commit()
    }

    private fun openFlutterVC(route: String?, params: Any?) {
        if(route.isNullOrEmpty() || params == null){
            return
        }
        val editMethodChannel = FlutterProxyActivity.prepareFlutterEngine(
            requireContext(),
            FlutterProxyActivity.ENGINE_ID_COMMON,
            route.orEmpty(),
            FlutterProxyActivity.CHANNEL_COMMON,
            FlutterProxyActivity.SCENE_COMMON
        ) { scene, method, call, result ->
            if (call.method == "flutterInitalized") {
                result.success("success")
                FlutterProxyActivity.initParams(params, method)
            }else{
                FlutterProxyActivity.handleFlutterCommonEvent(requireActivity(), scene, method, call, result)
            }
        }
        FlutterProxyActivity.go(requireActivity(), FlutterProxyActivity.ENGINE_ID_COMMON)
    }

    private var dialog : SynastryDialog? = null

    private fun reavelRelation(){
        dialog = SynastryDialog(this, user, object : SynastryDialog.Callback{
            override fun onSelected(select: BondDetail?) {
                if(select!=null){
                    mDatabind.btnRight.isVisible = false
                    showBondDetailInfo(select)
                }
                dialog?.onDestroy()
            }
        })
        dialog?.show()
    }

    private fun requestSynastryDetail() {
        scopeNetLife {
            val data = Post<BondDetail>(Api.BOND_DETAIL) {
                json("open_uid" to open_uid)
            }.await()
            data?.let {
                withMain {
                    showBondDetailInfo(it)
                }
            }
        }
    }

    private var bondDetail: BondDetail? = null

    private fun showBondDetailInfo(detail: BondDetail) {
        bondDetail = detail
        Log.e(TAG, "showBondDetailInfo: $detail", )
        if(detail != null && detail.from_open_uid?.isNotEmpty() == true){
            Log.e(TAG, "showBondDetailInfo: --1--", )
            mDatabind.btnRight.isVisible = false
            mDatabind.flutterSynastry.isVisible = true
            val txt  = "${getString(com.cyberflow.sparkle.R.string.you)} & ${detail.to_nick}"
            mDatabind.tvFlutterSynastryTitle.text = txt
            initSyNastryFlutter()
        }else{
            Log.e(TAG, "showBondDetailInfo: --2--", )
            mDatabind.btnRight.isVisible = true
            mDatabind.flutterSynastry.isVisible = false
        }
        shouldRequestRecommand()
    }

    // no astro code && no bond detail
    private fun shouldRequestRecommand() {
        if(user == null) return
        if(bondDetail == null) return
        if(user!!.star_sign.isNullOrEmpty() && bondDetail!!.from_open_uid.isNullOrEmpty()){
            requestRecommandFriend()
        }
    }

    private fun requestRecommandFriend() {
        scopeNetLife {
            val data = Post<RecommandFriendList>(Api.RECOMMAND_FRIEND) {}.await()
            data?.let {
                withMain {
                    mDatabind.layRecommand.isVisible = true
                    val data = arrayListOf<Any>()
                    data.add("start")
                    data.addAll(it.friends.orEmpty())
                    data.add("end")
                    mDatabind.rvRecommand.models = data
                }
            }
        }
    }
}