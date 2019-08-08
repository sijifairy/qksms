package com.moez.QKSMS.common.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.moez.QKSMS.common.BaseApplication;

/**
 * Utility class for navigating through activities.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Navigations {

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    /**
     * Start an activity on the given context, {@link Intent#FLAG_ACTIVITY_NEW_TASK} will be added to intent when the
     * given context is not an activity context.
     */
    public static void startActivity(Context context, Class<?> klass) {
        Intent intent = new Intent(context, klass);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * Start an activity with a minor delay, avoid blocking animation playing on the previous activity
     * (eg. material ripple effect above Lollipop).
     */
    public static void startActivityMainThreadFriendly(final Context context, Class<?> klass) {
        startActivityMainThreadFriendly(context, new Intent(context, klass));
    }

    public static void startActivityMainThreadFriendly(final Context context, final Intent intent) {
        sMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                context.startActivity(intent);
            }
        }, 300);
    }

    public static void startActivitySafelyAndClearTask(Context context, Intent intent) {
        try {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (RuntimeException e) {
//            Toasts.showToast(R.string.setting_device_not_support_message);
            Log.e("StartActivity", "Cannot start activity: " + intent);
        }
    }

    public static void startActivitySafely(Context context, Intent intent) {
        try {
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (RuntimeException e) {
//            Toasts.showToast(R.string.setting_device_not_support_message);
            Log.e("StartActivity", "Cannot start activity: " + intent);
        }
    }

    public static void startActivitiesSafely(Context context, Intent[] intents) {
        try {
            context.startActivities(intents);
        } catch (RuntimeException e) {
//            Toasts.showToast(R.string.setting_device_not_support_message);
            Log.e("StartActivity", "Cannot start activity: " + intents);
        }
    }

    public static void startActivityForResultSafely(Activity activity, Intent intent, int requestCode) {
        startActivityForResultSafely(activity, intent, requestCode, 0);
    }

    public static void startActivityForResultSafely(
            Activity activity, Intent intent, int requestCode, int errorMessageId) {
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            if (errorMessageId > 0) {
                Toasts.showToast(errorMessageId);
            }
        } catch (SecurityException e) {
            if (errorMessageId > 0) {
                Toasts.showToast(errorMessageId);
            }
            Log.e("StartActivity", "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity. Error: " + e);
        }
    }

    public static void openBrowser(Context context, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivitySafely(context, browserIntent);
    }

    /**
     * Start a system settings page with the given action.
     *
     * @param action            Action to start. Eg. {@link Settings#ACTION_DISPLAY_SETTINGS}.
     * @param attachPackageName If package name of this launcher should be attached by {@link Intent#setData(Uri)}.
     */
    public static void startSystemSetting(Context activityContext, String action, boolean attachPackageName) {
        Intent intent = new Intent(action);
        if (attachPackageName) {
            intent.setData(Uri.parse("package:" + activityContext.getPackageName()));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivitySafely(activityContext, intent);
    }

    public static void startSystemDataUsageSetting(Context context) {
        startSystemDataUsageSetting(context, false);
    }

    @SuppressWarnings("SameParameterValue")
    public static void startSystemDataUsageSetting(Context context, boolean attachNewTaskFlag) {
        Intent dataUsageIntent = new Intent();
        dataUsageIntent.setComponent(new ComponentName(
                "com.android.settings",
                "com.android.settings.Settings$DataUsageSummaryActivity"));
        Intent intent;
        if (dataUsageIntent.resolveActivity(context.getPackageManager()) != null) {
            intent = dataUsageIntent;
        } else {
            intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        }
        if (attachNewTaskFlag) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivitySafely(context, intent);
    }

    public static void bringActivityToFront(Class activity) {
        Intent intent = new Intent(BaseApplication.getContext(), activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        BaseApplication.getContext().startActivity(intent);
    }

    public static void startSystemAppInfoActivity(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName) || null == context) {
            return;
        }
        try {
            // Open the specific App Info page:
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            // Open the generic Apps page:
            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
            }
        }
    }}
