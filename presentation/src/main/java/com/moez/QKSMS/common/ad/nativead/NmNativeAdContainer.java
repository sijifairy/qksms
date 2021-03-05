package com.moez.QKSMS.common.ad.nativead;

import android.view.View;
import android.view.ViewGroup;

public abstract class NmNativeAdContainer {

  public abstract void setContent(ViewGroup parent, View content);

  public abstract void setIconView(NmNativeAdIconView icon);

  public abstract void setTitle(View title);

  public abstract void setBody(View body);

  public abstract void setCTA(View cta);

  public abstract void setAdMediaView(NmNativeAdMediaView mediaView);

  public abstract void setAdChoiceView(View adChoiceView);

  public abstract void fillNativeAd(NmNativeAd nmNativeAd);
}
