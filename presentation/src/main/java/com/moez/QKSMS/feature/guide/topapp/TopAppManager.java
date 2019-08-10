package com.moez.QKSMS.feature.guide.topapp;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.moez.QKSMS.common.BaseApplication;
import com.moez.QKSMS.common.util.SmsAnalytics;
import com.moez.QKSMS.common.util.TransitionUtils;
import com.moez.QKSMS.feature.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class TopAppManager {

    private static final int EVENT_CHECKING_TOP_PROCESS_TO_PROTECT = 100;
    private static final int EVENT_STOP_CHECKING_TOP_PROCESS = 101;

    private Handler workerHandler;

    private ForegroundAppReporter foregroundAppReporter = new ForegroundAppReporter();


    public static TopAppManager getInstance() {
        return AppLockManagerHolder.instance;
    }

    private static class AppLockManagerHolder {
        private static final TopAppManager instance = new TopAppManager();
    }

    private TopAppManager() {
        HandlerThread worker = new HandlerThread("TopAppManager", Thread.MAX_PRIORITY);
        worker.start();

        workerHandler = new Handler(worker.getLooper()) {

            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {

                    case EVENT_CHECKING_TOP_PROCESS_TO_PROTECT:
                        checkTopProcessToProtect((String) msg.obj);
                        break;

                    case EVENT_STOP_CHECKING_TOP_PROCESS:
                        workerHandler.removeMessages(EVENT_CHECKING_TOP_PROCESS_TO_PROTECT);
                        break;

                    default:
                        break;
                }
            }
        };

        workerHandler.post(new Runnable() {

            @Override
            public void run() {

            }
        });

//        INotificationObserver notificationObserver = new INotificationObserver() {
//
//            @Override public void onReceive(String s, CommonBundle hsBundle) {
//                switch (s) {
//                    case ScreenStatusManager.EXTRA_VALUE_SCREEN_OFF:
//                        workerHandler.sendEmptyMessage(EVENT_STOP_CHECKING_TOP_PROCESS);
//                        break;
//                    case ScreenStatusManager.EXTRA_VALUE_SCREEN_ON:
//                        workerHandler.sendEmptyMessage(EVENT_CHECKING_TOP_PROCESS_TO_PROTECT);
//                        break;
//                    case ScreenStatusManager.EXTRA_VALUE_USER_PRESENT:
//                        workerHandler.sendEmptyMessage(EVENT_CHECKING_TOP_PROCESS_TO_PROTECT);
//                        break;
//                }
//            }
//        };
//        HSGlobalNotificationCenter.addObserver(ScreenStatusManager.EXTRA_VALUE_SCREEN_OFF, notificationObserver);
//        HSGlobalNotificationCenter.addObserver(ScreenStatusManager.EXTRA_VALUE_SCREEN_ON, notificationObserver);
//        HSGlobalNotificationCenter.addObserver(ScreenStatusManager.EXTRA_VALUE_USER_PRESENT, notificationObserver);
    }

    public void startPollingTask() {
        Log.i("AppGuard", "startPollingTask()");

        workerHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!workerHandler.hasMessages(EVENT_CHECKING_TOP_PROCESS_TO_PROTECT)) {
                    workerHandler.sendEmptyMessage(EVENT_CHECKING_TOP_PROCESS_TO_PROTECT);
                }
            }
        });
    }

    private static List<String> sPackages = new ArrayList<>();

    static {
        sPackages.add("com.samsung.android.messaging");
        sPackages.add("com.android.mms");
        sPackages.add("com.google.android.apps.messaging");
        sPackages.add("com.motorola.messaging");
        sPackages.add("com.sonyericsson.conversations");
        sPackages.add("com.htc.sense.mms");
        sPackages.add("com.asus.message");
        sPackages.add("com.lge.message");
    }

    private void checkTopProcessToProtect(String lastPackageName) {
        workerHandler.removeMessages(EVENT_CHECKING_TOP_PROCESS_TO_PROTECT);

        long startTime = System.nanoTime();

        final ForegroundAppReporter.ForegroundAppInfo foregroundAppInfo = foregroundAppReporter.getForegroundPackageName();
        long delayDuration = 150;
        String packageName = null;

        try {
            packageName = null == foregroundAppInfo ? null : foregroundAppInfo.packageName;
            if (!TextUtils.isEmpty(packageName) && sPackages.contains(packageName)) {
                Intent intent = new Intent(BaseApplication.getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                SmsAnalytics.logEvent("AutoStart_Main");
                BaseApplication.getContext().startActivity(intent,
                        TransitionUtils.getNoAnimationBundle(BaseApplication.getContext()));
            }
        } finally {
            if (!workerHandler.hasMessages(EVENT_STOP_CHECKING_TOP_PROCESS)) {
                workerHandler.sendMessageDelayed(Message.obtain(workerHandler, EVENT_CHECKING_TOP_PROCESS_TO_PROTECT, packageName), delayDuration);
            }
        }
    }
}
