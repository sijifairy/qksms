package com.moez.QKSMS.common.ad;

public interface NmAdListener {

  void onAdClose();

  void onAdFailedToLoad(int var1);

  void onAdOpened();

  void onAdLoaded();

  void onAdClicked();

  void onAdImpression();
}
