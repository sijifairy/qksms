package com.moez.QKSMS.common.ad.adapter;

import android.app.Activity;
import com.moez.QKSMS.common.ad.NmAdListener;
import com.moez.QKSMS.common.ad.interstitial.NmInterstitialAd;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;

public class MopubInterstitialAd extends NmInterstitialAd {

  private MoPubInterstitial ad;
  private Activity activity;

  public MopubInterstitialAd(Activity activity, String adUnitId, NmAdListener listener) {
    super(adUnitId, listener);
    this.activity = activity;
  }

  @Override
  public void loadAd() {
    ad = new MoPubInterstitial(activity, adUnitId);
    ad.setInterstitialAdListener(
        new MoPubInterstitial.InterstitialAdListener() {
          @Override
          public void onInterstitialLoaded(MoPubInterstitial interstitial) {
            listener.onAdLoaded();
          }

          @Override
          public void onInterstitialFailed(
              MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
            listener.onAdFailedToLoad(0);
          }

          @Override
          public void onInterstitialShown(MoPubInterstitial interstitial) {
            listener.onAdOpened();
          }

          @Override
          public void onInterstitialClicked(MoPubInterstitial interstitial) {
            listener.onAdClicked();
          }

          @Override
          public void onInterstitialDismissed(MoPubInterstitial interstitial) {
            listener.onAdClose();
          }
        });
    ad.load();
  }

  @Override
  public void show() {
    ad.show();
  }
}
