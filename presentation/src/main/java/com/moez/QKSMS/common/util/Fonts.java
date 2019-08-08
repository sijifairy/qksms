package com.moez.QKSMS.common.util;

import android.graphics.Typeface;
import android.util.SparseArray;

import com.moez.QKSMS.common.BaseApplication;

public class Fonts {

    private static SparseArray<Typeface> sFontCache = new SparseArray<>(5);

    public static Typeface getTypeface(int resId) {
        Typeface typeface = sFontCache.get(resId);
        if (typeface != null) {
            return typeface;
        }
        try {
            typeface = Typeface.createFromAsset(BaseApplication.getContext().getAssets(),
                    "fonts/" + BaseApplication.getContext().getString(resId) + ".ttf");
        } catch (RuntimeException e) {
            try {
                typeface = Typeface.createFromAsset(BaseApplication.getContext().getAssets(),
                        "fonts/" + BaseApplication.getContext().getString(resId) + ".otf");
            } catch (RuntimeException ingored) {
                return null;
            }
        }
        sFontCache.put(resId, typeface);
        return typeface;
    }
}
