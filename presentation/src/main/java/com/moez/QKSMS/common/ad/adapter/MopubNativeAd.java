package com.moez.QKSMS.common.ad.adapter;

import android.graphics.drawable.Drawable;
import com.moez.QKSMS.common.ad.nativead.NmNativeAd;
import com.moez.QKSMS.common.ad.nativead.NmNativeAdContainer;
import com.mopub.nativeads.NativeAd;

public class MopubNativeAd extends NmNativeAd {

  public NativeAd mopubNativeAd;

  public MopubNativeAd(NativeAd admobNativeAd) {
    this.mopubNativeAd = admobNativeAd;
  }

  @Override
  public String getTitle() {
    return "";
  }

  @Override
  public String getBody() {
    return "";
  }

  @Override
  public Drawable getIcon() {
    return null;
  }

  @Override
  public String getCTA() {
    return "";
  }

  @Override
  public NmNativeAdContainer getContainer() {
    return new MopubNativeAdContainer(mopubNativeAd);
  }
}
