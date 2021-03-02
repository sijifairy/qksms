package com.moez.QKSMS.feature.guide;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.moez.QKSMS.common.util.Navigations;
import com.moez.QKSMS.common.util.PostOnNextFrameReceiver;

public class SettingLauncherPadActivity extends Activity {

    private static final String TAG = SettingLauncherPadActivity.class.getSimpleName();
    public static final String START_TO = "START_TO";
    public static final String START_NOTIFICATION_LISTENING = "START_NOTIFICATION_LISTENING";
    public static final String BACK_TO_APPLOCK_HOME = "BACK_TO_APPLOCK_HOME";

    private static final int REQUEST_CODE_ACCESSIBILITY = 1;
    private static final int REQUEST_CODE_USAGE = 2;
    private static final int REQUEST_CODE_NOTIFICATION = 3;
    public static final int NOTIFICATION_ACCESS_PERMISSION = 1;
    public static final int ACCESSIBILITY_SETTINGS_PERMISSION = 2;
    public static final int CLOSE_SYSTEM_SETTINGS = 3;
    public static final int USAGE_ACCESS_PERMISSION = 4;

    private BroadcastReceiver mCloseSystemDialogsReceiver = new PostOnNextFrameReceiver() {
        @Override
        public void onPostReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra("reason");
                Log.d(TAG, "CloseSystemDialogsReceiver onReceive reason = " + reason);
                if ("homekey".equals(reason) || "recentapps".equals(reason) || "voiceinteraction".equals(reason)
                        || "lock".equals(reason) || "assist".equals(reason)) {
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(mCloseSystemDialogsReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        handleIntentEvent(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntentEvent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        unregisterReceiver(mCloseSystemDialogsReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode = " + requestCode + " resultCode = " + resultCode);
        UsageUtils.stopObservingUsageAccessPermission();
        if (requestCode == REQUEST_CODE_USAGE) {
            if (getIntent() != null && getIntent().getBooleanExtra(BACK_TO_APPLOCK_HOME, false)) {
//                Intent intent = new Intent(this, ApplockHomeActivity.class);
//                intent.putExtra(ApplockSettingsBaseActivity.EXTRA_SHOULD_PROTECT, false);
//                intent.putExtra(ApplockHomeActivity.EXTRA_FROM_USAGE_GRANTED_SUCCESS, true);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
//                Navigations.startActivitySafely(this, intent);
            }
        }
        finish();
    }

    private void handleIntentEvent(Intent intent) {
        if (null == intent) {
            intent = getIntent();
        }

        if (intent != null) {
            int startTo = intent.getIntExtra(START_TO, NOTIFICATION_ACCESS_PERMISSION);
            Log.d(TAG, "handleIntentEvent startTo = " + startTo);
            switch (startTo) {
                // Intent do not addFlags Intent.FLAG_ACTIVITY_NEW_TASK, it will call back onActivityResult when start Activity
                case NOTIFICATION_ACCESS_PERMISSION:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        Intent startToIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                        Navigations.startActivityForResultSafely(this, startToIntent, REQUEST_CODE_NOTIFICATION);
                    }
                    break;
                case ACCESSIBILITY_SETTINGS_PERMISSION:
                    Intent startToIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    Navigations.startActivityForResultSafely(this, startToIntent, REQUEST_CODE_ACCESSIBILITY);
                    break;
                case USAGE_ACCESS_PERMISSION:
                    startToIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    Navigations.startActivityForResultSafely(this, startToIntent, REQUEST_CODE_USAGE);
                    break;
                case CLOSE_SYSTEM_SETTINGS:
//                    FloatWindowManager.getInstance().removeFloatButton();
//                    FloatWindowManager.getInstance().hideTopTip();
//                    Utils.stopObservingUsageAccessPermission();
//                    Utils.stopObservingNotificationPermission(null);
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

    public static void startSystemSettingsActivity(Context context, int startTo, boolean backToApplockHome) {
        Log.d(TAG, "startSystemSettingsActivity startTo = " + startTo);
        Intent intent = new Intent(context, SettingLauncherPadActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SettingLauncherPadActivity.START_TO, startTo);
        intent.putExtra(BACK_TO_APPLOCK_HOME, backToApplockHome);
        Navigations.startActivitySafely(context, intent);
    }

    public static void closeSettingsActivity(Context context) {
        Intent intent = new Intent(context, SettingLauncherPadActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SettingLauncherPadActivity.START_TO, CLOSE_SYSTEM_SETTINGS);
        Navigations.startActivitySafely(context, intent);
    }

    private static Runnable sSuccessRunnable = null;

    public static void closeSettingsActivityAndStartRequestNotificationListening(Context context, Runnable successRunnable) {
        sSuccessRunnable = successRunnable;
        Intent intent = new Intent(context, SettingLauncherPadActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SettingLauncherPadActivity.START_TO, CLOSE_SYSTEM_SETTINGS);
        intent.putExtra(SettingLauncherPadActivity.START_NOTIFICATION_LISTENING, true);
        Navigations.startActivitySafely(context, intent);
    }
}
