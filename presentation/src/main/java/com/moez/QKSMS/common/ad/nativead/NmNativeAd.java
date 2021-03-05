package com.moez.QKSMS.common.ad.nativead;

import android.graphics.drawable.Drawable;

public abstract class NmNativeAd {

  public abstract String getTitle();

  public abstract String getBody();

  public abstract Drawable getIcon();

  public abstract String getCTA();

  public abstract NmNativeAdContainer getContainer();
}
