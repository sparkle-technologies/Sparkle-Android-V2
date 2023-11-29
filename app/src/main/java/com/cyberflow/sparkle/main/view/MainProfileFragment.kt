package com.cyberflow.sparkle.main.view

import android.graphics.Paint
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.model.DetailResponseData
import com.cyberflow.base.model.IMSearchData
import com.cyberflow.base.model.ManyImageData
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
import com.cyberflow.sparkle.im.view.ChatActivity
import com.cyberflow.sparkle.im.view.IMAddFriendAct
import com.cyberflow.sparkle.mainv2.view.MainActivityV2
import com.cyberflow.sparkle.profile.view.ProfileAct.Companion.ACCEPT_FRIEND
import com.cyberflow.sparkle.profile.view.ProfileAct.Companion.ADD_FRIEND
import com.cyberflow.sparkle.profile.view.ProfileAct.Companion.CHAT
import com.cyberflow.sparkle.profile.view.ShareAct
import com.cyberflow.sparkle.profile.viewmodel.ProfileViewModel
import com.cyberflow.sparkle.setting.view.SettingsActivity
import com.cyberflow.sparkle.widget.NotificationDialog
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.cyberflow.sparkle.widget.ToastDialogHolder
import com.drake.brv.utils.setup
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
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
import me.jessyan.autosize.utils.ScreenUtils

class MainProfileFragment : BaseDBFragment<ProfileViewModel, FragmentMainProfileBinding>() {

    private val TAG = "MainProfileFragment"

    private fun hideOrShowAllIcons() {
        mDatabind.apply {
            btnSetting.isVisible = isMySelf
            btnWallet.isVisible = isMySelf
            btnSharePurple.isVisible = !isMySelf

            btnRight.isVisible = true

            if(isMySelf){
                btnLeft.setViewTxt("Go To Closet")
                btnRight.setViewTxt("Share")
            }else{
                when(action){
                    CHAT -> {
                        btnLeft.setViewTxt(getString(R.string.chat))
                        btnRight.isVisible = false
                    }
                    ADD_FRIEND -> {
                        btnLeft.setViewTxt(getString(R.string.add_friend))
                        btnRight.setViewTxt("You&Harry...")
                    }
                    ACCEPT_FRIEND -> {
                        btnLeft.setViewTxt(getString(R.string.accept_friend))
                        btnRight.setViewTxt("You&Harry...")
                    }
                }
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        Log.e(TAG, "initView: ")

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
                                ChatActivity.launch(
                                    mActivity,
                                    open_uid.replace("-", "_"),
                                    it.avatar,
                                    it.nick,
                                    1
                                )
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
                            viewModel.acceptFriend(IMDataManager.instance.getEmMessage())
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
                    ToastUtil.show(mActivity, "显示么子动画")
                }
            }
        })
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
        Log.e(TAG, "initData: ")

        initHoroscopeFlutter()

        if (open_uid.isNullOrEmpty()) {  // 加载自己的主页
            open_uid = CacheUtil.getUserInfo()?.user?.open_uid.orEmpty()
        }

        loadProfile()

        viewModel.acceptFriendObservable.observe(this) {
            if (!it.isNullOrEmpty()) {
                action = CHAT
            }
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
        val user = CacheUtil.getUserInfo()?.user

        isMySelf = open_uid == user?.open_uid && open_uid.isNotEmpty()

        Log.e(TAG, "loadProfile: isMySelf=$isMySelf")

        hideOrShowAllIcons()
        mDatabind.llBack.isVisible = !isMySelf

        if (isMySelf) {  // if user is me  - img logic
            val cache = CacheUtil.getString(CacheUtil.AVATAR_BIG)
            Log.e(TAG, "loadProfile: cache img: $cache")
            if (cache.isNullOrEmpty()) {
                requestImg()
            } else {
                loadBigImg(cache)
                requestImg()
            }
            showUserInfo(user)
        }else{
            requestDetail()  // first, load data, then load img, cause default holder is up to gender
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
        val marLeft = dp2px(38f)
        val marRight = dp2px(33f)
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
        editMethodChannel = FlutterProxyActivity.prepareFlutterEngine(
            mActivity,
            FlutterProxyActivity.ENGINE_ID_EDIT_PROFILE,
            FlutterProxyActivity.ROUTE_EDIT_PROFILE,
            FlutterProxyActivity.CHANNEL_SETTING,
            FlutterProxyActivity.SCENE_PROFILE_EDIT
        ) { scene, method, call, result ->
            handleFlutterEvent(scene, method, call, result)
        }
    }

    private fun handleFlutterEvent(
        scene: Int, method: MethodChannel, call: MethodCall, result: MethodChannel.Result
    ) {
        if (call.method == "flutterToast") {
            val type = call.argument<String>("type")
            val content = call.argument<String>("content")
            if (content?.isNotEmpty() == true) {
                val t = when (type) {
                    "success" -> NotificationDialog.TYPE_SUCCESS
                    "error" -> NotificationDialog.TYPE_ERROR
                    else -> NotificationDialog.TYPE_WARN
                }
                ToastDialogHolder.getDialog()?.show(mActivity, t, content)
            }
            result.success("success")
        } else {
            FlutterProxyActivity.handleFlutterCommonEvent(scene, method, call, result)
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

                layTwitter.isVisible = true
                if(isMySelf){
                    if(twitter!=null){
                        tvTwitter.text = twitter.nick
                    }else{
                        tvTwitter.paint.flags = Paint.ANTI_ALIAS_FLAG
                        tvTwitter.paint.isAntiAlias = true
                        tvTwitter.text = Html.fromHtml("<u>Add Twitter</u>", Html.FROM_HTML_MODE_COMPACT)
                        layTwitter.setOnClickListener {
                            // todo   go twitter
                            ToastUtil.show(mActivity, "跳转绑定推特 还没做")
                        }
                    }
                }else{
                    if(twitter!=null){
                        tvTwitter.text = twitter.nick
                    }else{
                        layTwitter.isVisible = false
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

                if(!isMySelf){
                    layRecommand.isVisible = true
                    rvRecommand.setup {
                        addType<FriendMessageInfo>(com.cyberflow.sparkle.R.layout.item_profile_friend_recommand)
                    }.models = arrayListOf(FriendMessageInfo(), FriendMessageInfo(),FriendMessageInfo(), FriendMessageInfo(),FriendMessageInfo(), FriendMessageInfo())
                }

                if(user?.star_sign.isNullOrEmpty()){
                    fragmentHoroscopeContainer.isVisible = false
                }else{

                }
            }
        }
    }


    private var methodChannel : MethodChannel? = null

    private fun initHoroscopeFlutter() {
        methodChannel = FlutterProxyActivity.prepareFlutterEngine(requireActivity(), FlutterProxyActivity.ENGINE_ID_EDIT_PROFILE, FlutterProxyActivity.ROUTE_EDIT_PROFILE, FlutterProxyActivity.CHANNEL_SETTING, FlutterProxyActivity.SCENE_SETTING_EDIT) { scene, method, call, result ->
            FlutterProxyActivity.handleFlutterCommonEvent(scene, method, call, result)
        }
        /*methodChannel = FlutterProxyActivity.prepareFlutterEngine(requireActivity(), FlutterProxyActivity.ENGINE_ID_HOROSCOPE, FlutterProxyActivity.ROUTE_HOROSCOPE, FlutterProxyActivity.CHANNEL_HOROSCOPE, FlutterProxyActivity.SCENE_HOROSCOPE) { scene, method, call, result ->
            FlutterProxyActivity.handleFlutterCommonEvent(scene, method, call, result)
        }*/
        val fragment = FlutterFragment.withCachedEngine(FlutterProxyActivity.ENGINE_ID_EDIT_PROFILE)
            .renderMode(RenderMode.texture)
            .transparencyMode(TransparencyMode.transparent)
            .build<FlutterFragment>()
        requireActivity()
            .supportFragmentManager.beginTransaction()
            .add(com.cyberflow.sparkle.R.id.fragment_horoscope_container, fragment)
            .commit()
    }
}