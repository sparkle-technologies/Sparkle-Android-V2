package com.cyberflow.sparkle.im.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.model.DetailResponseData
import com.cyberflow.base.net.Api
import com.cyberflow.base.net.GsonConverter
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.base.util.ConstantGlobal
import com.cyberflow.base.util.PageConst
import com.cyberflow.base.util.ToastUtil
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.util.click
import com.cyberflow.sparkle.DBComponent.loadImage
import com.cyberflow.sparkle.chat.DemoHelper
import com.cyberflow.sparkle.chat.R
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.chat.databinding.ActivityImChatBinding
import com.cyberflow.sparkle.chat.ui.fragment.ChatFragment
import com.cyberflow.sparkle.chat.ui.goPreview
import com.cyberflow.sparkle.chat.viewmodel.ChatViewModel
import com.cyberflow.sparkle.chat.viewmodel.IMDataManager
import com.cyberflow.sparkle.chat.viewmodel.MessageViewModel
import com.cyberflow.sparkle.chat.viewmodel.parseResource
import com.cyberflow.sparkle.flutter.FlutterProxyActivity
import com.cyberflow.sparkle.profile.view.ShareAct
import com.cyberflow.sparkle.widget.NotificationDialog
import com.cyberflow.sparkle.widget.ToastDialogHolder
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.net.utils.withMain
import com.google.android.material.snackbar.Snackbar
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMTextMessageBody
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.easeui.model.EaseEvent
import com.therouter.TheRouter
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.launch

/**
 * UI
 *  跑马灯  30个问题  跑马灯效果  1.点击问题  2.点击未读消息  3.普通点击   三种方式
 *  聊天界面  问题列表  5个一页  可刷新换问题显示  单个点击直接发送  只要发完就彻底隐藏  悬浮在 EaseChatInputMenu 上面
 *  发送消息后不能继续发送  需要等待IM回复才解锁  显示 typing
 *  结果分享   一条消息拆成俩份  第一个是图片  第二个是内容  右下角带一个分享按钮  点击出弹窗
 *
 * 数据逻辑
 *  每次发送消息  先调用IM接口  再调用后端接口  tarot/chat  期间不能继续发送消息了
 *  普通消息  校验消息   结果消息
 *      普通消息 直接显示
 *      校验消息 如果校验通过  跑 塔罗牌
 *      结果消息 显示到聊天界面上
 *
 * 要做到事情
 *  UI 画好  跑马灯  聊天界面的问题列表  typing  结果内容分享-最后面再做
 *  聊天界面改造   尤其是正常聊天界面和cora的不同之处
 *
 */
class ChatActivity : BaseDBAct<ChatViewModel, ActivityImChatBinding>(),
    ChatFragment.OnFragmentInfoListener {

    private var isCora: Boolean = false
    private var conversationId: String = ""
    private var avatar: String = ""
    private var nickName: String = ""
    private var question: String = ""
    private val chatType: Int = 1

    private val msgViewModel: MessageViewModel by viewModels()

    companion object {

        private val NONE = "NONE"
        private val TYPING = "TYPING"

        fun launch(context: Context, conversationId: String, avatar: String, nickName:String, question: String?="") {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, conversationId)
            intent.putExtra(EaseConstant.EXTRA_CONVERSATION_AVATAR, avatar)
            intent.putExtra(EaseConstant.EXTRA_CONVERSATION_NICK_NAME, nickName)
            intent.putExtra(EaseConstant.EXTRA_CONVERSATION_CORA_QUESTION, question)
            context.startActivity(intent)
        }
    }

    private fun goMain(){
        TheRouter.build(PageConst.App.PAGE_MAIN).navigation()
    }

    override fun initView(savedInstanceState: Bundle?) {
        intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_ID)?.apply {
            conversationId = this.replace("-", "_")
            isCora = conversationId == ConstantGlobal.getCoraOpenUid().replace("-", "_")
            if(isCora){
                intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_CORA_QUESTION)?.apply {
                    question = this
                }
                if(question.isNullOrEmpty()){
                    showQuestionsList() // say hi cora, show question list
                }
                loadCoraInfo(conversationId)
                freshFlutter()
            }
        }
        intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_AVATAR)?.apply {
            avatar = this
        }
        intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_NICK_NAME)?.apply {
            nickName = this
        }

        if (conversationId.isNullOrEmpty()) {
            ToastUtil.show(this, "Invalid params")
            finish()
            return
        }

        mDataBinding.layBack.click {
            onBackPressed()
        }

        mDataBinding.ivAvatar.click {
            Log.e(TAG, "ivAvatar:  go chat detail or profile detail? ")
            goPreview(conversationId.replace("_", "-"))
        }

        mDataBinding.ivBtnRight.click {
            Log.e(TAG, "ivBtnRight:  waiting for designer to decide what to do")
        }
        initChatFragment()
    }

    var fragment: ChatFragment? = null

    private fun initChatFragment() {
        Bundle().apply {
            fragment = ChatFragment()
            fragment?.setOnFragmentInfoListener(this@ChatActivity)
            putString(EaseConstant.EXTRA_CONVERSATION_ID, conversationId)
            putInt(EaseConstant.EXTRA_CHAT_TYPE, chatType)
            putString(DemoConstant.HISTORY_MSG_ID, "")
            putBoolean(EaseConstant.EXTRA_IS_ROAM, DemoHelper.getInstance().model.isMsgRoaming)
            fragment?.arguments = this
            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_fragment, fragment!!, "chat").commit()
        }
    }

    override fun initData() {
        val conversation = EMClient.getInstance().chatManager().getConversation(conversationId)
        viewModel.deleteObservable.observe(this) {
            parseResource(it, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    finish()
                    val event =
                        EaseEvent.create(DemoConstant.CONVERSATION_DELETE, EaseEvent.TYPE.MESSAGE)
                    msgViewModel.setMessageChange(event)
                }
            })
        }
        msgViewModel.messageChange.with(DemoConstant.MESSAGE_FORWARD, EaseEvent::class.java).observe(this) {
            it?.apply {
                if (isMessageChange) {
                    message?.also { m->
                        showSnackBar(m)
                    }
                }
            }
        }
        msgViewModel.messageChange.with(DemoConstant.CONTACT_CHANGE, EaseEvent::class.java)
            .observe(this) {
                if (it == null) {
                    return@observe
                }
                if (conversation == null) {
                    finish()
                }
            }

        LiveDataBus.get().with(ToastDialogHolder.CHAT_ACTIVITY_NOTIFY, NotificationDialog.ToastBody::class.java).observe(this){
            myToast(it.type, it.content)
        }

        setDefaultTitle()
    }

    private fun showSnackBar(event: String) {
        Snackbar.make(mDataBinding.layTop, event, Snackbar.LENGTH_SHORT).show()
    }

    override fun onChatError(code: Int, errorMsg: String?) {
        if (errorMsg != null) {
            ToastUtil.show(this, errorMsg, false)
        }
    }

    override fun onOtherTyping(action: String?) {
        action?.also {
            if (it == TYPING) {
                mDataBinding.tvTitle.text = getString(R.string.alert_during_typing)
            }
            if (it == NONE) {
                setDefaultTitle()
            }
        }
    }

    private fun setDefaultTitle() {
        Log.e(TAG, "setDefaultTitle: avatar=${avatar}  nickName=${nickName}")
        mDataBinding.tvTitle.text = nickName
        loadImage(mDataBinding.ivAvatar, avatar)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (null != this.currentFocus) {
            val mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            return mInputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
        }
        return super.onTouchEvent(event)
    }

    override fun onBackPressed() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm != null && window.attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (currentFocus != null) {
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                goMain()
            } else {
                goMain()
            }
        } else {
            goMain()
        }
    }

    private fun loadCoraInfo(coraOpenUid: String) {
        val info = CacheUtil.getCoraInfo()
        if(info == null){   // update cora info here
            scopeNetLife {
                val data = Post<DetailResponseData>(Api.USER_DETAIL) {
                    json("open_uid" to coraOpenUid.replace("_", "-"))
                }.await()
                data?.let {
                    withMain {
                        CacheUtil.setCoraInfo(it)
                        avatar = it.user?.avatar.orEmpty()
                        loadImage(mDataBinding.ivAvatar, avatar)
                    }
                }
            }
        }else{   // use cache multi times
            avatar = info.user?.avatar.orEmpty()
            loadImage(mDataBinding.ivAvatar, avatar)
        }
    }


    private fun showQuestionsList(){
        // todo
    }

    override fun onReady() {
        fragment?.initCora(isCora, question)
    }

    override fun sendChatInfoToServer(msgId: String?, msg: String?) {
        onOtherTyping(TYPING)
        // cannot edit or send
        fragment?.cannotEditOrSend()
        scopeNetLife {
            Post<String>(Api.IM_CHAT) {
                json("msgId" to msgId, "msg" to msg)
            }.await()
            withMain {
                if(msg.equals(getString(com.cyberflow.base.resources.R.string.hi_cora))){
                    fragment?.showQuestions()
                    fragment?.hideHiCoraBtn()
                }else{
                    fragment?.hideQuestions()
                }
                onOtherTyping(NONE)
                fragment?.canEditOrSend()
            }
         }
    }

    // 这里逻辑和 com.hyphenate.easeui.widget.chatrow.EaseChatRowCustom.onSetUpView 一样
    // 只做 flutter 相关操作  不做UI显示
    override fun handleAIOMessage(message: EMMessage?) {
        Log.e(TAG, "handleAIOMessage: flutterReady=$flutterReady" )
        if(!flutterReady) return
        val txtBody = message?.body as? EMCustomMessageBody
        if (txtBody != null) {
            val event = txtBody.event()
            val customExt = txtBody.params
            val msgId = customExt["msgId"] // 透传客户端问题消息 i
            val msgType = customExt["msgType"] // 0-普通消息，1-校验消息，2-结果消息
            val isValid = customExt["isValid"] // 可选字段（只有校验消息才有这个字段），是否有效，1-有效，0-无效
            val content = customExt["content"] // 消息内容，结果消息且有结果的情况，是结构化消息；否则，是 aio 显示的消息
            val hasResult = customExt["hasResult"] // 可选字段（只有结果消息才有这个字段），是否有结果，1-有结果，0-无结果
            Log.e(TAG, "handleAIOMessage: ${GsonConverter.gson.toJson(customExt)}" )
            lifecycleScope.launch {
                if(msgType == "1" && isValid == "1"){
                    val questionMsg = DemoHelper.getInstance().chatManager.getMessage(msgId)
                    val questionStr = (questionMsg?.body as? EMTextMessageBody)?.message
                    FlutterProxyActivity.initTarotParams(msgId, questionStr, methodChannel)
                    withMain {
                        FlutterProxyActivity.go(this@ChatActivity, FlutterProxyActivity.ENGINE_ID_TAROT)
                    }
                }
                if(msgType == "2" ){
                    if(hasResult == "1" && content?.isNotEmpty() == true){
                        FlutterProxyActivity.nativeTarotResult(msgId, content, methodChannel)
                    }
                }
            }
        }
    }

    // 弹出分享弹窗
    override fun handleAIOShareAction(message: EMMessage?) {
        IMDataManager.instance.setShareMsg(message)
        ShareAct.go(this,  ShareAct.SHARE_FROM_CHAT, "")
    }

    private var methodChannel : MethodChannel? = null
    private var flutterReady = false
    private fun initFlutter() {
        lifecycleScope.launch {
            methodChannel = FlutterProxyActivity.prepareFlutterEngine(this@ChatActivity, FlutterProxyActivity.ENGINE_ID_TAROT, FlutterProxyActivity.ROUTE_TAROT, FlutterProxyActivity.CHANNEL_TAROT, FlutterProxyActivity.SCENE_TAROT) { scene, method, call, result ->
                Log.e(TAG, "initFlutter: call.method=$call.method" )
                if (call.method == "flutterInitalized") {  // 通知 native 已初始化
                    Log.e(TAG, "initFlutter: flutterReady=$flutterReady")
                    flutterReady = true
                }
                if (call.method == "flutterDestroy") {  // flutter 完成使命  通知 native 销毁
                    val isDrawCards = call.argument<Int>("isDrawCards")  // isDrawCards: 1-已抽卡，0-未抽卡（抽牌未完成中途退出）
                    Log.e(TAG, "initFlutter: isDrawCards=$isDrawCards"  )
                    if(isDrawCards == 0){
                        // 插入一条消息  告诉 抽卡未完成
                        val msgStr = getString(com.cyberflow.sparkle.R.string.aio_break)
                        val from = conversationId
                        val to = CacheUtil.getUserInfo()?.user?.open_uid.orEmpty().replace("-", "_")
                        fragment?.insertMsg(msgStr, from, to)
                    }
                    FlutterProxyActivity.handleFlutterCommonEvent(this@ChatActivity, scene, method, call, result)
                    freshFlutter()
                }
                if (call.method == "flutterDrawCards") {  // flutter 抽卡完成，通知 native   如果没有这个 则表示被中途打断的  需要插入一条消息
//                    freshFlutter()
                }
            }
        }
    }

    private fun freshFlutter(){
        methodChannel?.setMethodCallHandler(null)
        methodChannel = null
        flutterReady = false
        initFlutter()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(methodChannel!=null){
            FlutterEngineCache.getInstance().get(FlutterProxyActivity.ENGINE_ID_TAROT)?.destroy()
        }
    }
}