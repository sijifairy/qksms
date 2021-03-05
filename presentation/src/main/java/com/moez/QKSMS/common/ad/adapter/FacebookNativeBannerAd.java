package com.moez.QKSMS.common.ad.adapter;

import android.graphics.drawable.Drawable;
import com.facebook.ads.NativeBannerAd;
import com.moez.QKSMS.common.ad.nativead.NmNativeAd;
import com.moez.QKSMS.common.ad.nativead.NmNativeAdContainer;

public class FacebookNativeBannerAd extends NmNativeAd {

  public NativeBannerAd facebookNativeBannerAd;

  public FacebookNativeBannerAd(NativeBannerAd facebookNativeBannerAd) {
    this.facebookNativeBannerAd = facebookNativeBannerAd;
  }

  @Override
  public String getTitle() {
    return facebookNativeBannerAd.getAdvertiserName();
  }

  @Override
  public String getBody() {
    return facebookNativeBannerAd.getAdBodyText();
  }

  @Override
  public Drawable getIcon() {
    return null;
  }

  @Override
  public String getCTA() {
    return facebookNativeBannerAd.getAdCallToAction();
  }

  @Override
  public NmNativeAdContainer getContainer() {
    return new FacebookNativeBannerAdContainer(facebookNativeBannerAd);
  }
}
