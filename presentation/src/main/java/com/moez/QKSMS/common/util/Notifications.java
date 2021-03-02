package com.moez.QKSMS.common.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.moez.QKSMS.common.BaseApplication;

import androidx.core.app.NotificationCompat;

public class Notifications {

    public static void buildAndNotifySafely(final int id, final NotificationCompat.Builder builder) {
        if (builder == null) {
            return;
        }

        Threads.postOnSingleThreadExecutor(new Runnable() {
            @Override public void run() {
                try {
                    NotificationManager notifyMgr = (NotificationManager)
                            BaseApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    notifyMgr.notify(id, builder.build());
                } catch (Exception e) {
                }
            }
        });
    }

    public static void notifySafely(final int id, final Notification notification) {
        if (notification == null) {
            return;
        }
        Runnable notifyRunnable = new Runnable() {
            @Override
            public void run() {
                NotificationManager notifyMgr = (NotificationManager)
                        BaseApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                try {
                    notifyMgr.notify(id, notification);
                } catch (Exception e) {
                }
            }
        };
        Threads.postOnSingleThreadExecutor(notifyRunnable); // Keep notifications in original order
    }

    public static void cancelSafely(final int id) {
        Runnable cancelRunnable = new Runnable() {
            @Override
            public void run() {
                NotificationManager notifyMgr = (NotificationManager)
                        BaseApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                try {
                    notifyMgr.cancel(id);
                } catch (Exception var3) {
                }
            }
        };
        Threads.postOnSingleThreadExecutor(cancelRunnable);
    }
}
