package com.hyphenate.easeui.widget.chatrow;

import android.content.Context;
import android.text.Spannable;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cyberflow.base.model.AIOResult;
import com.cyberflow.base.model.TarotCard;
import com.cyberflow.base.net.GsonConverter;
import com.cyberflow.sparkle.widget.ShadowImgButton;
import com.hyphenate.chat.EMCustomMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.manager.EaseDingMessageHelper;
import com.hyphenate.easeui.utils.EaseSmileUtils;

import java.util.List;
import java.util.Map;

public class EaseChatRowCustom extends EaseChatRow {

    private TextView contentView;

    private ImageView iv1, iv2, iv3;
    private TextView tv1, tv2, tv3;
    private TextView tv1_bottom, tv2_bottom, tv3_bottom;

    private ShadowImgButton btnShare;
    private View layResult;


    public EaseChatRowCustom(Context context, boolean isSender) {
        super(context, isSender);
    }

    public EaseChatRowCustom(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(R.layout.ease_row_received_message_custom, this);
    }

    @Override
    protected void onFindViewById() {
        contentView = findViewById(R.id.tv_chatcontent);

        iv1 =  findViewById(R.id.iv1);
        iv2 =  findViewById(R.id.iv2);
        iv3 =  findViewById(R.id.iv3);

        tv1 =  findViewById(R.id.tv1);
        tv2 =  findViewById(R.id.tv2);
        tv3 =  findViewById(R.id.tv3);

        tv1_bottom =  findViewById(R.id.tv1_bottom);
        tv2_bottom =  findViewById(R.id.tv2_bottom);
        tv3_bottom =  findViewById(R.id.tv3_bottom);

        btnShare =  findViewById(R.id.btn_share);

        layResult =  findViewById(R.id.lay_result);
    }

    @Override
    public void onSetUpView() {
        EMCustomMessageBody txtBody = (EMCustomMessageBody) message.getBody();
        if (txtBody != null) {
            String event = txtBody.event();
            Map<String, String> customExt = txtBody.getParams();
            String msgId = customExt.get("msgId");          // 透传客户端问题消息 i
            String msgType = customExt.get("msgType");      // 0-普通消息，1-校验消息，2-结果消息
            String isValid = customExt.get("isValid");      // 可选字段（只有校验消息才有这个字段），是否有效，1-有效，0-无效
            String content = customExt.get("content");      // 消息内容，结果消息且有结果的情况，是结构化消息；否则，是 aio 显示的消息
            String hasResult = customExt.get("hasResult");  // 可选字段（只有结果消息才有这个字段），是否有结果，1-有结果，0-无结果

            String str = GsonConverter.Companion.getGson().toJson(customExt);
            Log.e(TAG, "EMCustomMessageBody: " + str );

            if(msgType.equals("2")){
                try{
                    AIOResult result = GsonConverter.Companion.getGson().fromJson(content, AIOResult.class);
                    if(hasResult.equals("1")){
                        showAIOResult(result);
                    }else{
                        layResult.setVisibility(View.GONE);
                        btnShare.setVisibility(View.GONE);
                        Spannable span = EaseSmileUtils.getSmiledText(context, result.getResult());
                        contentView.setText(span, TextView.BufferType.SPANNABLE);
                    }
                }catch (Exception e){  // 万一解析挂了  直接显示内容
                    layResult.setVisibility(View.GONE);
                    btnShare.setVisibility(View.GONE);
                    Spannable span = EaseSmileUtils.getSmiledText(context, content);
                    contentView.setText(span, TextView.BufferType.SPANNABLE);
                }
            }else{
                layResult.setVisibility(View.GONE);
                btnShare.setVisibility(View.GONE);
                Spannable span = EaseSmileUtils.getSmiledText(context, content);
                contentView.setText(span, TextView.BufferType.SPANNABLE);
            }
        }
    }


    private void showAIOResult(AIOResult result) {
        layResult.setVisibility(View.VISIBLE);
        btnShare.setVisibility(View.VISIBLE);
        List<TarotCard> list = result.getTarotCards();

        if(list.size() > 0)
            showItem(list.get(0), tv1, iv1, tv1_bottom);
        if(list.size() > 1)
            showItem(list.get(1), tv2, iv2, tv2_bottom);
        if(list.size() > 2)
            showItem(list.get(2), tv3, iv3, tv3_bottom);

        Spannable span = EaseSmileUtils.getSmiledText(context, result.getResult());
        contentView.setText(span, TextView.BufferType.SPANNABLE);
        btnShare.setClickListener(() -> itemClickListener.onAIOResultClick(message));
    }

    private void showItem(TarotCard tarotCard, TextView tv, ImageView iv, TextView tvBottom) {
        String name = tarotCard.getName();
        String url = tarotCard.getImgUrl();
        String uprightOrReversed = tarotCard.getUprightOrReversed();
        tv.setText(name);
        Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(false).into(iv);
        if(tarotCard.isReversed() == 1){
            iv.setRotation(180);
        }
        tvBottom.setText(uprightOrReversed);
    }

    public void onAckUserUpdate(final int count) {
        if (ackedView != null && isSender()) {
            ackedView.post(new Runnable() {
                @Override
                public void run() {
                    ackedView.setVisibility(VISIBLE);
                    ackedView.setText(String.format(getContext().getString(R.string.group_ack_read_count), count));
                }
            });
        }
    }

    @Override
    protected void onMessageCreate() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (statusView != null) {
            statusView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onMessageSuccess() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (statusView != null) {
            statusView.setVisibility(View.GONE);
        }

        // Show "1 Read" if this msg is a ding-type msg.
        if (isSender() && EaseDingMessageHelper.get().isDingMessage(message) && ackedView != null) {
            ackedView.setVisibility(VISIBLE);
            int count = message.groupAckCount();
            ackedView.setText(String.format(getContext().getString(R.string.group_ack_read_count), count));
        }

        // Set ack-user list change listener.
        EaseDingMessageHelper.get().setUserUpdateListener(message, userUpdateListener);
    }

    @Override
    protected void onMessageError() {
        super.onMessageError();
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (statusView != null) {
            statusView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onMessageInProgress() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (statusView != null) {
            statusView.setVisibility(View.GONE);
        }
    }

    private EaseDingMessageHelper.IAckUserUpdateListener userUpdateListener =
            new EaseDingMessageHelper.IAckUserUpdateListener() {
                @Override
                public void onUpdate(List<String> list) {
                    onAckUserUpdate(list.size());
                }
            };
}
