package com.moez.QKSMS.common.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.CallSuper;

public abstract class PostOnNextFrameReceiver extends BroadcastReceiver {

    @Override
    @CallSuper
    public void onReceive(final Context context, final Intent intent) {
        Threads.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                PostOnNextFrameReceiver.this.onPostReceive(context, intent);
            }
        });
    }

    protected abstract void onPostReceive(Context context, Intent intent);
}
