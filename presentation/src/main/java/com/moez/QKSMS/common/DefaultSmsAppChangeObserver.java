package com.moez.QKSMS.common;

import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Telephony;
import android.util.Log;

import com.klinker.android.send_message.Utils;
import com.moez.QKSMS.common.util.Preferences;
import com.moez.QKSMS.common.util.SmsAnalytics;
import com.moez.QKSMS.common.util.Threads;
import com.moez.QKSMS.feature.guide.SetAsDefaultGuideActivity;

public class DefaultSmsAppChangeObserver extends ContentObserver {
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public DefaultSmsAppChangeObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Threads.postOnMainThreadDelayed(() -> {
            if (!Utils.isDefaultSmsApp(QKApplication.context)) {
                Log.d("DefaultSmsAppChange", "default sms cleared by " + Telephony.Sms.getDefaultSmsPackage(QKApplication.context));

                SmsAnalytics.logEvent("Default_Cleared", "by", Telephony.Sms.getDefaultSmsPackage(QKApplication.context));
                SetAsDefaultGuideActivity.startActivity(BaseApplication.getContext(), SetAsDefaultGuideActivity.DEFAULT_CHANGED);
            } else {
                Preferences.getDefault().doOnce(() -> SmsAnalytics.logEvent("Default_First_Set"), "pref_key_log_first_set_as_default");
                SmsAnalytics.logEvent("Default_Set");
            }
        }, 500);
    }
}
