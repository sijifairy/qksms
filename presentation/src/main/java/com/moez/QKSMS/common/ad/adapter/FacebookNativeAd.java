package com.moez.QKSMS.common.ad.adapter;

import android.graphics.drawable.Drawable;
import com.facebook.ads.NativeAd;
import com.moez.QKSMS.common.ad.nativead.NmNativeAd;
import com.moez.QKSMS.common.ad.nativead.NmNativeAdContainer;

public class FacebookNativeAd extends NmNativeAd {

  public NativeAd facebookNativeAd;

  public FacebookNativeAd(NativeAd facebookNativeAd) {
    this.facebookNativeAd = facebookNativeAd;
  }

  @Override
  public String getTitle() {
    return facebookNativeAd.getAdvertiserName();
  }

  @Override
  public String getBody() {
    return facebookNativeAd.getAdBodyText();
  }

  @Override
  public Drawable getIcon() {
    return null;
  }

  @Override
  public String getCTA() {
    return facebookNativeAd.getAdCallToAction();
  }

  @Override
  public NmNativeAdContainer getContainer() {
    return new FacebookNativeAdContainer(facebookNativeAd);
  }
}
