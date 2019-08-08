package com.moez.QKSMS.common.util;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.text.TextUtils;

import com.moez.QKSMS.common.BaseApplication;

@SuppressWarnings("WeakerAccess")
public class Packages {

    /**
     * @return Whether application with given package name is installed.
     * Defaults to {@code false} if error occurs when querying package manager.
     */
    public static boolean isPackageInstalled(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = BaseApplication.getContext().getPackageManager().getPackageInfo(packageName, 0);
        } catch (Exception e) {
            packageInfo = null;
        }
        return (packageInfo != null);
    }

    public static boolean isLaunchableApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(packageName);
        try {
            return null != BaseApplication.getContext().getPackageManager().resolveActivity(intent, 0);
        } catch (Exception e) {
            return false;
        }
    }
}
