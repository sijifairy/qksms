package com.moez.QKSMS.common.ad.adapter;

import android.view.View;
import android.view.ViewGroup;
import com.moez.QKSMS.common.ad.nativead.NmNativeAd;
import com.moez.QKSMS.common.ad.nativead.NmNativeAdContainer;
import com.moez.QKSMS.common.ad.nativead.NmNativeAdIconView;
import com.moez.QKSMS.common.ad.nativead.NmNativeAdMediaView;
import com.mopub.nativeads.AdapterHelper;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.ViewBinder;

public class MopubNativeAdContainer extends NmNativeAdContainer {

  private NativeAd ad;

  public MopubNativeAdContainer(NativeAd ad) {
    this.ad = ad;
  }

  @Override
  public void setContent(ViewGroup parent, View content) {
    View v =
        new AdapterHelper(parent.getContext(), 0, 3)
            .getAdView(null, parent, ad, new ViewBinder.Builder(0).build());
    parent.addView(v);
  }

  @Override
  public void setIconView(NmNativeAdIconView nmNativeAdIconView) {}

  @Override
  public void setTitle(View title) {}

  @Override
  public void setBody(View body) {}

  @Override
  public void setCTA(View cta) {}

  @Override
  public void setAdMediaView(NmNativeAdMediaView mediaViewContainer) {}

  @Override
  public void setAdChoiceView(View adChoiceView) {}

  @Override
  public void fillNativeAd(NmNativeAd nmNativeAd) {}
}
