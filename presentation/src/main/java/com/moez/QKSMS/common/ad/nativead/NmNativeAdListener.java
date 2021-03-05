package com.moez.QKSMS.common.ad.nativead;

import com.moez.QKSMS.common.ad.NmAdListener;

public interface NmNativeAdListener extends NmAdListener {

  void onAdLoaded(NmNativeAd ad);
}
