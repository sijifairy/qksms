package com.moez.QKSMS.common.util;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.moez.QKSMS.common.BaseApplication;

import java.util.Set;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

@SuppressWarnings("unused")
public class Permissions {

    @SuppressLint("NewApi")
    public static boolean isFloatWindowAllowed(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
                || Compats.getHuaweiEmuiVersionCode() == Compats.EmuiBuild.VERSION_CODES.EMUI_4_1;
    }

    public static boolean hasPermission(String permission) {
        boolean granted = false;
        if (!TextUtils.isEmpty(permission)) {
            try {
                granted = ContextCompat.checkSelfPermission(BaseApplication.getContext(), permission)
                        == PackageManager.PERMISSION_GRANTED;
            } catch (RuntimeException e) {
            }
        }
        return granted;
    }

    public static boolean isUsageAccessGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }
        Context context = BaseApplication.getContext();
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            if (appOpsManager == null) {
                return false;
            }
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isNotificationAccessGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return false;
        }

        Set<String> enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(BaseApplication.getContext());
        return enabledPackages.contains(BaseApplication.getContext().getPackageName());
    }
}
