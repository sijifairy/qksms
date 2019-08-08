package com.moez.QKSMS.common.notificationcenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import com.moez.QKSMS.common.BaseApplication;

public class GlobalNotificationCenter {
    private static final NotificationCenter defaultNotificationCenter = new NotificationCenter();
    private static final NotificationCenter frameworkNotificationCenter = new NotificationCenter();
    private static BroadcastReceiver receiver;

    private GlobalNotificationCenter() {
    }

    public static synchronized void addObserver(String notificationName, INotificationObserver observer) {
        if (!"hs.commons.config.CONFIG_CHANGED".equals(notificationName) && !"hs.commons.config.CONFIG_LOAD_FINISHED".equals(notificationName) && !"hs.app.session.SESSION_START".equals(notificationName) && !"hs.app.session.SESSION_END".equals(notificationName) && !"hs.diverse.session.SESSION_START".equals(notificationName) && !"hs.diverse.session.SESSION_END".equals(notificationName)) {
            defaultNotificationCenter.addObserver(notificationName, observer);
        } else {
            if (null == receiver) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("hs.commons.config.CONFIG_CHANGED");
                intentFilter.addAction("hs.commons.config.CONFIG_LOAD_FINISHED");
                intentFilter.addAction("hs.app.session.SESSION_START");
                intentFilter.addAction("hs.app.session.SESSION_END");
                intentFilter.addAction("hs.diverse.session.SESSION_START");
                intentFilter.addAction("hs.diverse.session.SESSION_END");
                receiver = new BroadcastReceiver() {
                    public void onReceive(Context context, Intent intent) {
                        CommonBundle bundle = null;
                        if (!"hs.app.session.SESSION_START".equals(intent.getAction()) && !"hs.app.session.SESSION_END".equals(intent.getAction())) {
                            if ("hs.commons.config.CONFIG_LOAD_FINISHED".equals(intent.getAction())) {
                                bundle = new CommonBundle();
                                bundle.putBoolean("hs.IS_SUCCESS", intent.getBooleanExtra("hs.IS_SUCCESS", false));
                            }
                        } else {
                            bundle = new CommonBundle();
                            bundle.putInt("hs.app.session.SESSION_ID", intent.getIntExtra("hs.app.session.SESSION_ID", 0));
                        }

                        GlobalNotificationCenter.frameworkNotificationCenter.sendNotification(intent.getAction(), bundle);
                    }
                };
                BaseApplication.getContext().registerReceiver(receiver, intentFilter, "", (Handler)null);
            }

            frameworkNotificationCenter.addObserver(notificationName, observer);
        }
    }

    public static synchronized void removeObserver(String notificationName, INotificationObserver observer) {
        defaultNotificationCenter.removeObserver(notificationName, observer);
        frameworkNotificationCenter.removeObserver(notificationName, observer);
        if (null != receiver && frameworkNotificationCenter.isEmpty()) {
            try {
                BaseApplication.getContext().unregisterReceiver(receiver);
                receiver = null;
            } catch (IllegalArgumentException var3) {
                ;
            }
        }

    }

    public static synchronized void removeObserver(INotificationObserver observer) {
        defaultNotificationCenter.removeObserver(observer);
        frameworkNotificationCenter.removeObserver(observer);
        if (null != receiver && frameworkNotificationCenter.isEmpty()) {
            try {
                BaseApplication.getContext().unregisterReceiver(receiver);
                receiver = null;
            } catch (IllegalArgumentException var2) {
                ;
            }
        }

    }

    public static void sendNotification(String notificationName) {
        defaultNotificationCenter.sendNotification(notificationName);
    }

    public static void sendNotification(String notificationName, CommonBundle bundle) {
        defaultNotificationCenter.sendNotification(notificationName, bundle);
    }

    public static void sendNotificationOnMainThread(String notificationName) {
        defaultNotificationCenter.sendNotificationOnMainLooper(notificationName);
    }

    public static void sendNotificationOnMainThread(String notificationName, CommonBundle bundle) {
        defaultNotificationCenter.sendNotificationOnMainLooper(notificationName, bundle);
    }
}
