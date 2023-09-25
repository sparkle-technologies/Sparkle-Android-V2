package com.cyberflow.sparkle.chat.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.cyberflow.base.util.bus.LiveDataBus;
import com.hyphenate.easeui.model.EaseEvent;

public class MessageViewModel extends AndroidViewModel {
    private LiveDataBus messageObservable;

    public MessageViewModel(@NonNull Application application) {
        super(application);
        messageObservable = LiveDataBus.get();
    }

    public void setMessageChange(EaseEvent change) {
        messageObservable.with(change.event).postValue(change);
    }

    public LiveDataBus getMessageChange() {
        return messageObservable;
    }

}