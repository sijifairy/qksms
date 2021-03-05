package com.moez.QKSMS.common.ad.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeBannerAd;
import com.moez.QKSMS.common.ad.nativead.NmNativeAd;
import com.moez.QKSMS.common.ad.nativead.NmNativeAdContainer;
import com.moez.QKSMS.common.ad.nativead.NmNativeAdIconView;
import com.moez.QKSMS.common.ad.nativead.NmNativeAdMediaView;
import java.util.ArrayList;
import java.util.List;

public class FacebookNativeBannerAdContainer extends NmNativeAdContainer {

  private NativeAdLayout nativeAdLayout;
  private ViewGroup adView;
  private MediaView adIconView;
  private View adTitle;
  private View adCTA;
  private NativeBannerAd ad;

  public FacebookNativeBannerAdContainer(NativeBannerAd ad) {
    this.ad = ad;
  }

  @Override
  public void setContent(ViewGroup parent, View content) {
    adView = (ViewGroup) content;
    nativeAdLayout = new NativeAdLayout(content.getContext());
    nativeAdLayout.addView(
        content,
        new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    parent.removeAllViews();
    parent.addView(
        nativeAdLayout,
        new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
  }

  @Override
  public void setIconView(NmNativeAdIconView nmNativeAdIconView) {
    adIconView = new MediaView(nmNativeAdIconView.getContext());
    nmNativeAdIconView.removeAllViews();
    nmNativeAdIconView.addView(adIconView);
  }

  @Override
  public void setTitle(View title) {
    ((TextView) title).setText(ad.getAdvertiserName());
    adTitle = title;
  }

  @Override
  public void setBody(View body) {
    ((TextView) body).setText(ad.getAdBodyText());
  }

  @Override
  public void setCTA(View cta) {
    ((TextView) cta).setText(ad.getAdCallToAction());
    adCTA = cta;
  }

  @Override
  public void setAdMediaView(NmNativeAdMediaView mediaViewContainer) {}

  @Override
  public void setAdChoiceView(View adChoiceView) {
    AdOptionsView adOptionsView = new AdOptionsView(adChoiceView.getContext(), ad, nativeAdLayout);
    ((ViewGroup) adChoiceView).removeAllViews();
    ((ViewGroup) adChoiceView)
        .addView(
            adOptionsView,
            new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
  }

  @Override
  public void fillNativeAd(NmNativeAd nmNativeAd) {
    List<View> clickableViews = new ArrayList<>();
    clickableViews.add(adTitle);
    clickableViews.add(adCTA);

    ad.unregisterView();
    ad.registerViewForInteraction(adView, adIconView, clickableViews);
  }
}
