package com.moez.QKSMS.common;

import android.content.Context;

import com.ihs.app.framework.HSApplication;

public class BaseApplication extends HSApplication {

    private static Context context;

    @Override public void onCreate() {
        super.onCreate();
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        context = this;
    }

    public static Context getContext() {
        return context;
    }
}
