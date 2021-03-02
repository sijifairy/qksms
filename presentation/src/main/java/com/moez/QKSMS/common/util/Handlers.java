package com.moez.QKSMS.common.util;

import android.os.Handler;
import android.os.Looper;

public class Handlers {

    private static Handler sharedHandler;

    public static final Handler sharedHandler() {
        if (sharedHandler == null) {
            sharedHandler = new Handler(Looper.getMainLooper());
        }
        return sharedHandler;
    }

}
