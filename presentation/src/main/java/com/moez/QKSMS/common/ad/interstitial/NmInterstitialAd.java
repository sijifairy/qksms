package com.moez.QKSMS.common.ad.interstitial;

import com.moez.QKSMS.common.ad.NmAdListener;
import com.moez.QKSMS.common.util.Threads;

public abstract class NmInterstitialAd {

  public String adUnitId;
  public NmAdListener listener;

  public NmInterstitialAd(String adUnitId, NmAdListener listener) {
    this.adUnitId = adUnitId;
    this.listener = listener;

    Threads.postOnMainThread(
        new Runnable() {
          @Override
          public void run() {
            loadAd();
          }
        });
  }

  public abstract void loadAd();

  public abstract void show();
}
