package com.cyberflow.sparkle.profile.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.beust.klaxon.Klaxon
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.model.DetailResponseData
import com.cyberflow.base.model.IMSearchData
import com.cyberflow.base.model.ManyImageData
import com.cyberflow.base.model.User
import com.cyberflow.base.net.Api
import com.cyberflow.base.net.GsonConverter
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.PageConst
import com.cyberflow.base.util.ToastUtil
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.util.bus.SparkleEvent
import com.cyberflow.base.util.dp2px
import com.cyberflow.sparkle.DBComponent.loadImageWithHolder
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
import com.cyberflow.sparkle.databinding.ActivityProfileBinding
import com.cyberflow.sparkle.im.view.ChatActivity
import com.cyberflow.sparkle.im.view.IMAddFriendAct
import com.cyberflow.sparkle.profile.viewmodel.ProfileViewModel
import com.cyberflow.sparkle.profile.widget.Tag
import com.cyberflow.sparkle.setting.view.SettingsActivity
import com.cyberflow.sparkle.widget.ShadowImgButton
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.spannable.addSpan
import com.drake.spannable.movement.ClickableMovementMethod
import com.drake.spannable.replaceSpan
import com.drake.spannable.setSpan
import com.drake.spannable.span.CenterImageSpan
import com.drake.spannable.span.ColorSpan
import com.drake.spannable.span.HighlightSpan
import com.hjq.language.LocaleContract
import com.hjq.language.MultiLanguages
import com.therouter.router.Route
import dev.pinkroom.walletconnectkit.core.chains.toJson
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.FlutterActivityLaunchConfigs
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import me.jessyan.autosize.utils.ScreenUtils

@Route(path = PageConst.App.PAGE_PROFILE)
class ProfileAct : BaseDBAct<ProfileViewModel, ActivityProfileBinding>() {

    companion object {
        const val OPEN_UID = "open_uid"
        const val FRIEND_STATUS = "friend_status"

        const val CHAT = 0
        const val ADD_FRIEND = 1
        const val ACCEPT_FRIEND = 2

        fun go(context: Context, openUid: String, friendStatus: Int = 0) {
            val intent = Intent(context, ProfileAct::class.java)
            intent.putExtra(OPEN_UID, openUid)
            intent.putExtra(FRIEND_STATUS, friendStatus)
            context.startActivity(intent)
        }
    }

    private fun hideOrShowAllIcons() {

        mDataBinding.apply {

            btnSetting.isVisible =  isMySelf
            btnSharePurple.isVisible = !isMySelf

            anchor.isVisible = isMySelf
            anchorBg.isVisible = isMySelf
            btnHoroscope.isVisible = isMySelf
            btnStar.isVisible = isMySelf
            btnShareBlue.isVisible = isMySelf

            btnProfileAction.isVisible = !isMySelf
            btnProfileAction.setViewTxt(txt[action])

        }
    }

    private var txt = arrayListOf<String>()

    override fun initView(savedInstanceState: Bundle?) {
        txt = arrayListOf(
            getString(com.cyberflow.base.resources.R.string.chat),
            getString(com.cyberflow.base.resources.R.string.add_friend),
            getString(com.cyberflow.base.resources.R.string.accept_friend))

        mDataBinding.llBack.setOnClickListener {
            onBackPressed()
        }

        mDataBinding.btnSetting.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                 SettingsActivity.go(this@ProfileAct)
            }
        })

        mDataBinding.btnSharePurple.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
               goShare()
            }
        })

        mDataBinding.btnTopStar.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                ToastUtil.show(this@ProfileAct, "coming soon...0")
            }
        })

        mDataBinding.btnHoroscope.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                ToastUtil.show(this@ProfileAct, "coming soon...1")
            }
        })

        mDataBinding.btnStar.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                ToastUtil.show(this@ProfileAct, "coming soon...2")
            }
        })

        mDataBinding.btnShareBlue.setClickListener(object : ShadowImgButton.ShadowClickListener {
            override fun clicked() {
                goShare()
            }
        })

        mDataBinding.btnProfileAction.setClickListener(object : ShadowTxtButton.ShadowClickListener{
            override fun clicked(disable: Boolean) {

                Log.e(TAG, "clicked: action=$action   user=$user  " )
                Log.e(TAG, "clicked: openUid=${user?.open_uid}   user.name=${user?.nick}  " )

                 when(action){
                     CHAT -> {   // go chatActivity   avatar nickName
                         user?.also {
                             ChatActivity.launch(this@ProfileAct, open_uid.replace("-", "_"), it.avatar, it.nick, 1)
                         }
                     }
                     ADD_FRIEND -> {  // go add friend activity  -- IMSearchData
                         user?.also {
                             IMSearchData(nick = it.nick, avatar = it.avatar, gender = it.gender, open_uid = it.open_uid, ca_wallet = it.ca_wallet, wallet_address = it.wallet_address).apply {
                                 IMAddFriendAct.go(this@ProfileAct, this)
                             }
                         }
                     }
                     ACCEPT_FRIEND -> {  // EMMessage
                         viewModel.acceptFriend(IMDataManager.instance.getEmMessage())
                     }
                 }
            }
        })
    }

    private fun goShare(){
        user?.also {
            IMDataManager.instance.setUser(it)
            ShareAct.go(this@ProfileAct, serverImageUrl)
        }
    }

    private var user: User? = null

    private var action = CHAT

    override fun initData() {
        intent.getIntExtra(FRIEND_STATUS, CHAT).apply {
            action = this
            Log.e(TAG, "initData: action=$action" )
        }
        intent.getStringExtra(OPEN_UID)?.apply {
            open_uid = this.replace("_", "-")
            loadProfile()
        }

        viewModel.acceptFriendObservable.observe(this){
            if(!it.isNullOrEmpty()){
                action = CHAT
                mDataBinding.btnProfileAction.setViewTxt(txt[action])
            }
        }

        LiveDataBus.get().apply {
            with(SparkleEvent.PROFILE_CHANGED, String::class.java).observe(this@ProfileAct, this@ProfileAct::profileDataChanged)
        }
    }


    private fun profileDataChanged(s: String){
        loadProfile()
    }

    private var open_uid = ""
    private var isMySelf = false

    private fun loadProfile() {
        val user = CacheUtil.getUserInfo()?.user
        isMySelf = open_uid == user?.open_uid && open_uid.isNotEmpty()

        Log.e(TAG, "loadProfile: isMySelf=$isMySelf" )

        hideOrShowAllIcons()

        if(isMySelf){  // if user is me  - img logic
            val cache = CacheUtil.getString(CacheUtil.AVATAR_BIG)
            Log.e(TAG, "loadProfile: cache img: $cache" )
            if(cache.isNullOrEmpty()){
               requestImg()
            }else{
                loadBigImg(cache)
                requestImg()
            }
        }else{
            requestImg()
        }
        if(isMySelf){   // if user is me  - the rest UI logic
            showUserInfo(user)
        }else{
            requestDetail()
        }
    }

    private fun requestImg(){
        scopeNetLife {
            val data = Post<ManyImageData>(Api.GET_IMAGE_URLS) {
                json("open_uid" to open_uid)
            }.await()
            data?.let {
                it.image_list?.profile_native?.apply {
                    if(isMySelf){
                        CacheUtil.savaString(CacheUtil.AVATAR_BIG, this)
                    }
                    serverImageUrl = this
                    loadBigImg(this)
                }
            }
        }
    }

    private var serverImageUrl : String?  = null

    private fun loadBigImg(url:String){
        val holder = ResourcesCompat.getDrawable(resources, com.cyberflow.sparkle.R.drawable.profile_default_avatar,null)
        loadImageWithHolder(mDataBinding.ivAvatar, url, holder, 24)
    }

    private fun requestDetail(){
        scopeNetLife {
            val data = Post<DetailResponseData>(Api.USER_DETAIL) {
                json("open_uid" to open_uid)
            }.await()

            data?.let {
                showUserInfo(it.user)
            }
        }
    }

    private fun setSpan(tv: TextView) {
        tv.movementMethod = ClickableMovementMethod.getInstance()
        val measurePaint = tv.paint
        val pWidth = measurePaint.measureText("There‘s no signature yet, please go to image edit")
        val screenWidth = ScreenUtils.getScreenSize(this)[0]
        val marLeft = dp2px(38f)
        val marRight = dp2px(33f)
//        Log.e(TAG, " pWidth=$pWidth  screenWidth=$screenWidth  marLeft=$marLeft  marRight=$marRight" )
        val breakLine = if(pWidth < (screenWidth - marLeft - marRight)) "" else "\n"
        tv.text = ("There‘s no signature yet, please go to $breakLine" ).setSpan(ColorSpan("#000000"))
            .addSpan("image", CenterImageSpan(this, com.cyberflow.base.resources.R.drawable.profile_ic_edit).setDrawableSize(dp2px(15f)).setMarginHorizontal(dp2px(2f)) )
            .addSpan(" edit")
            .replaceSpan("image"){
                HighlightSpan("#8B82DB"){
//                    ToastUtil.show(this, "click img, go flutter page ")
                    goFlutter()
                }
            }
            .replaceSpan("edit"){
                HighlightSpan("#8B82DB"){
//                    ToastUtil.show(this, "click txt, go flutter page ")
                    goFlutter()
                }
            }
    }

    private val ENGINE_ID_EDIT_PROFILE = "eidt_profile"
    lateinit var flutterEngine_edit_profile: FlutterEngine
    private var methodChannel: MethodChannel? = null

    private fun initFlutter(){
        flutterEngine_edit_profile = FlutterEngine(this)
        flutterEngine_edit_profile.navigationChannel.setInitialRoute("/profile/edit")
        flutterEngine_edit_profile.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
        FlutterEngineCache.getInstance().put(ENGINE_ID_EDIT_PROFILE, flutterEngine_edit_profile)
        methodChannel = MethodChannel(flutterEngine_edit_profile.dartExecutor.binaryMessenger, "settingChannel")
        methodChannel?.setMethodCallHandler { call, result ->
            // handle flutter caller
            Log.e(TAG, "handle flutter event   method: ${call.method}" )

            if(call.method == "flutterDestroy"){
//                go(this@ProfileAct)
                result.success("success")
                recreate()
            }

            if(call.method == "flutterInitalized"){
                result.success("success")
                callFlutter()
            }

            if (call.method == "saveProfileSuccess") {
                val userStr = call.argument<HashMap<String, String>>("user")
                result.success("success")

                Log.e("flutter", "android receive form:${userStr.toJson()} ")
                val user = GsonConverter.gson.fromJson(userStr.toJson(), User::class.java)
                Log.e("flutter", "android receive form:$user ")

                CacheUtil.getUserInfo()?.also {
                    it.user?.apply {
                        user?.also { new->
                            birth_time = new.birth_time
                            birthdate = new.birthdate
                            birthplace_info = new.birthplace_info
                            location_info = new.location_info
                            nick = new.nick
                            signature = new.signature
                            profile_permission = new.profile_permission
                            gender = new.gender

                            CacheUtil.setUserInfo(it)
                            LiveDataBus.get().with(SparkleEvent.PROFILE_CHANGED).postValue("time:${System.currentTimeMillis()}")
                        }
                    }
                }
            }
        }
    }
    private fun goFlutter(){
        startActivity(FlutterActivity.withCachedEngine(ENGINE_ID_EDIT_PROFILE).backgroundMode(
            FlutterActivityLaunchConfigs.BackgroundMode.transparent).build(this))
    }

    private fun callFlutter() {
        var local = "zh-Hans-CN"
        val current = MultiLanguages.getAppLanguage()
        if(current.language.equals(LocaleContract.getEnglishLocale().language)){
            local = "en_US"
        }
        CacheUtil.getUserInfo()?.apply {
            val openUid = user?.open_uid.orEmpty()
            val token = token
            var map = mutableMapOf<String, Any>()

            val jsonString = GsonConverter.gson.toJson(user)
            val userMap = Klaxon().parse<Map<String, String>>(jsonString).orEmpty()
            map["token"] = token
            map["user"] =  userMap
            map["editBio"] = 1
            map["localeLanguage"] = local
            map["openuid"] = openUid
            val params = GsonConverter.gson.toJson(map)
            Log.e(TAG, "callFlutter:  params: $params" )
            methodChannel?.invokeMethod("nativeShareParams", map, object : MethodChannel.Result {
                override fun success(result: Any?) {
                    Log.e(TAG, "callFlutter success: ")
                }

                override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                    Log.e(TAG, "callFlutter errorCode: ")
                }

                override fun notImplemented() {
                    Log.e(TAG, "callFlutter notImplemented: ")
                }
            })
        }
    }


    private fun getIconByBindType(type: String) : Int{
        return when(type){
            "Twitter" -> com.cyberflow.base.resources.R.drawable.profile_ic_twitter
            "Discord" -> com.cyberflow.base.resources.R.drawable.profile_ic_discord
            else
                -> 0
        }
    }

    private fun showUserInfo(_user: User?) {
         _user?.also { data->
             this.user = data
             mDataBinding.apply {
                 btnName.setViewTxt(data.nick)

                 data?.bind_list?.map {
                     Tag().apply {
                         isChecked = true
                         title = " @${it.nick}"
                         leftDrawableResId = getIconByBindType(it.type)
                     }
                 }?.also {
                     if(it.isNotEmpty())
                        tlvTagView.tags = it
                 }

                 if(data.signature.isNotEmpty()){
                     tvContent.text = data.signature
                 }else{
                     if(isMySelf){
                         setSpan(tvContent)
                         initFlutter()
                     }else{
                         tvContent.text = "He has not yet constructed the bio."
                     }
                 }

                 if(!data.star_sign.isNullOrEmpty() && !isMySelf){
                     btnTopStar.isVisible = true
                 }
             }
         }
    }
}
