package com.moez.QKSMS.common.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.moez.QKSMS.common.BaseApplication;

import androidx.annotation.StringRes;

/**
 * This toast utility is better than directly calling {@link Toast#makeText(Context, CharSequence, int)} in two ways:
 *
 * (1) Consecutive calls would result in updated toast text (not new toasts).
 * (2) This utility forces usage of application context to avoid memory leak with activity context caused by an internal
 * bug of {@link Toast}.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Toasts {

    private static Toast sToast;

    public static void showToast(@StringRes int msgResId) {
        showToast(msgResId, Toast.LENGTH_SHORT);
    }

    public static void showToast(@StringRes int msgResId, int length) {
        showToast(BaseApplication.getContext().getString(msgResId), length);
    }

    public static void showToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    public static void showToast(final String msg, final int length) {
        @SuppressLint("ShowToast")
        Runnable show = new Runnable() {
            @Override
            public void run() {
                if (sToast == null) {
                    sToast = Toast.makeText(BaseApplication.getContext(), msg, length);
                    TextView v = sToast.getView().findViewById(android.R.id.message);
                    if (v != null) {
                        v.setGravity(Gravity.CENTER);
                    }
                }
                sToast.setText(msg);
                try {
                    sToast.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Threads.runOnMainThread(show);
    }
}
