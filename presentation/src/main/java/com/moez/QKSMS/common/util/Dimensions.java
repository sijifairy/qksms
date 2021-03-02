package com.moez.QKSMS.common.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.moez.QKSMS.common.BaseApplication;

import java.lang.reflect.Method;
import java.util.Locale;

import androidx.annotation.NonNull;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Dimensions {

    private static final String TAG = Dimensions.class.getSimpleName();

    public static final int DEFAULT_DEVICE_SCREEN_HEIGHT = 1920;
    public static final int DEFAULT_DEVICE_SCREEN_WIDTH = 1080;

    // Cache variables
    private static float sDensityRatio;
    private static int sNavigationBarHeight;

    public static int pxFromDp(float dp) {
        return Math.round(dp * getDensityRatio());
    }

    public static float getDensityRatio() {
        if (sDensityRatio > 0f) {
            return sDensityRatio;
        }
        Resources resources = BaseApplication.getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        sDensityRatio = (float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        return sDensityRatio;
    }

    public static int getPhoneWidth(Context context) {
        if (null == context) {
            return DEFAULT_DEVICE_SCREEN_WIDTH;
        }
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = DEFAULT_DEVICE_SCREEN_WIDTH;
        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            if (display != null) {
                display.getMetrics(dm);
                width = dm.widthPixels;
            }
        }
        return width;
    }

    /**
     * 返回手机屏幕高度
     */
    public static int getPhoneHeight(Context context) {
        if (null == context) {
            return DEFAULT_DEVICE_SCREEN_HEIGHT;
        }
        int height = context.getResources().getDisplayMetrics().heightPixels;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

            if (windowManager != null) {
                Point localPoint = new Point();
                windowManager.getDefaultDisplay().getRealSize(localPoint);
                Log.v(TAG, "height == " + height + ", w == " + localPoint.x + ", h == " + localPoint.y);
                if (localPoint.y > height) {
                    height = localPoint.y;
                }
            }
        } else {
            int navigationBarHeight = getNavigationBarHeight(context);
            Log.v(TAG, "Layout h == " + height + ", navigationBarHeight == " + navigationBarHeight);
            if (navigationBarHeight != 0 && height % 10 != 0) {
                if ((height + navigationBarHeight) % 10 == 0) {
                    height = (height + navigationBarHeight);
                }
            }
            Log.v(TAG, "height == " + height + ", navigationBarHeight == " + navigationBarHeight);
        }

        return height;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRtl() {
        Resources res = BaseApplication.getContext().getResources();
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                && (res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    /**
     * Get {@link Locale} on {@link Resources} obtained from given {@link Context}. Compatibility handled for Nougat.
     * Returns value of {@link Locale#getDefault()} when no current locale is set on given context.
     */
    @SuppressWarnings("deprecation")
    public static @NonNull
    Locale getLocale(@NonNull Context context) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return locale;
    }

    public static int getNavigationBarHeight(Context context) {
        if (null == context) {
            return 0;
        }
        if (sNavigationBarHeight > 0) {
            return sNavigationBarHeight;
        }
        if (context instanceof Activity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Activity activityContext = (Activity) context;
            DisplayMetrics metrics = new DisplayMetrics();
            activityContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            activityContext.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight) {
                sNavigationBarHeight = realHeight - usableHeight;
            }
            return sNavigationBarHeight;
        }
        sNavigationBarHeight = getNavigationBarHeightUnconcerned(context);
        return sNavigationBarHeight;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @SuppressLint("PrivateApi")
    public static boolean hasNavBar(Context paramContext) {
        boolean bool = true;
        String sNavBarOverride;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Class klass = Class.forName("android.os.SystemProperties");
                //noinspection unchecked
                Object localObject = klass.getDeclaredMethod("get", String.class);
                ((Method) localObject).setAccessible(true);
                sNavBarOverride = (String) ((Method) localObject).invoke(null, "qemu.hw.mainkeys");
                localObject = paramContext.getResources();
                int i = ((Resources) localObject).getIdentifier(
                        "config_showNavigationBar", "bool", "android");
                if (i != 0) {
                    bool = ((Resources) localObject).getBoolean(i);
                    if ("1".equals(sNavBarOverride)) {
                        return false;
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        if (!ViewConfiguration.get(paramContext).hasPermanentMenuKey()) {
            return bool;
        }

        return false;
    }

    private static int getNavigationBarHeightUnconcerned(Context context) {
        if (null == context) {
            return 0;
        }
        Resources localResources = context.getResources();
        if (!hasNavBar(context)) {
            return 0;
        }
        int i = localResources.getIdentifier(
                "navigation_bar_height", "dimen", "android");
        if (i > 0) {
            return localResources.getDimensionPixelSize(i);
        }
        i = localResources.getIdentifier(
                "navigation_bar_height_landscape", "dimen", "android");
        if (i > 0) {
            return localResources.getDimensionPixelSize(i);
        }
        return 0;
    }

    /**
     * @return Status bar (top bar) height. Note that this height remains fixed even when status bar is hidden.
     */
    public static int getStatusBarHeight(Context context) {
        if (null == context) {
            return 0;
        }
        int height = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = context.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }

    public static int getStatusBarInset(Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? getStatusBarHeight(context) : 0;
    }
}
