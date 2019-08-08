package com.moez.QKSMS.feature.guide;

import android.content.Context;
import android.database.ContentObserver;
import android.util.Log;

import com.moez.QKSMS.common.BaseApplication;
import com.moez.QKSMS.common.util.Navigations;
import com.moez.QKSMS.common.util.Permissions;
import com.moez.QKSMS.common.util.Threads;

import java.util.Timer;
import java.util.TimerTask;

public class UsageUtils {

    private static final int MAXIMUM_OBSERVE_TIME_SECONDS = 60;

    private static ContentObserver sCurrentObserver;
    private static Timer sUsageAccessTimer;


    public static void requestUsageAccessPermission(final Context context,
                                                    Runnable monitorRunnable,
                                                    boolean showStepIndicator,
                                                    boolean backToApplockHome,
                                                    boolean isFromSecurityLevel) {
        SettingLauncherPadActivity.startSystemSettingsActivity(BaseApplication.getContext(), SettingLauncherPadActivity.USAGE_ACCESS_PERMISSION, backToApplockHome);
        Threads.postOnMainThreadDelayed(new Runnable() {
            @Override public void run() {
                Navigations.startActivity(context, NotificationAccessDialogActivity.class);
            }
        }, 100);

        if (monitorRunnable != null) {
            startObservingUsageAccessPermission(monitorRunnable);
        }
    }

    public static void startObservingUsageAccessPermission(final Runnable grantedAction) {
        sUsageAccessTimer = new Timer();
        sUsageAccessTimer.schedule(new TimerTask() {
            private static final int MAXIMUM_POLL_TIMES = MAXIMUM_OBSERVE_TIME_SECONDS;

            private int mPollTimes;

            @Override
            public void run() {
                mPollTimes++;
                if (mPollTimes > MAXIMUM_POLL_TIMES) {
                    cancel();
                    return;
                }
                boolean granted = Permissions.isUsageAccessGranted();
                Log.d("Permission.UsageAccess", "Poll once, granted: " + granted);
                if (granted) {
                    cancel();
                    grantedAction.run();
                }
            }
        }, 0, 1000);
    }

    public static void stopObservingUsageAccessPermission() {
        if (sUsageAccessTimer != null) {
            sUsageAccessTimer.cancel();
            sUsageAccessTimer = null;
        }
    }
}
