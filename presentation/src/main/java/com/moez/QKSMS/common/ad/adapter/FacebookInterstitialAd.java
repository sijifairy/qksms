package com.moez.QKSMS.common.ad.adapter;

import android.util.Log;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.moez.QKSMS.common.ad.AdEngine;
import com.moez.QKSMS.common.ad.NmAdListener;
import com.moez.QKSMS.common.ad.interstitial.NmInterstitialAd;

public class FacebookInterstitialAd extends NmInterstitialAd {

  private static final String TAG = FacebookInterstitialAd.class.getSimpleName();

  private InterstitialAd ad;

  public FacebookInterstitialAd(String adUnitId, NmAdListener listener) {
    super(adUnitId, listener);
  }

  @Override
  public void loadAd() {
    Log.d(TAG, "request facebook interstitial ad " + adUnitId);
    ad = new InterstitialAd(AdEngine.getInstance().applicationContext, adUnitId);
    InterstitialAdListener interstitialAdListener =
        new InterstitialAdListener() {
          @Override
          public void onInterstitialDisplayed(Ad ad) {
            // Interstitial ad displayed callback
            Log.e(TAG, "Interstitial ad displayed.");

            listener.onAdOpened();
          }

          @Override
          public void onInterstitialDismissed(Ad ad) {
            // Interstitial dismissed callback
            Log.e(TAG, "Interstitial ad dismissed.");
            listener.onAdClose();
          }

          @Override
          public void onError(Ad ad, AdError adError) {
            // Ad error callback
            Log.e(TAG, "Interstitial ad failed to load: " + adError.getErrorMessage());
            listener.onAdFailedToLoad(0);
          }

          @Override
          public void onAdLoaded(Ad ad) {
            // Interstitial ad is loaded and ready to be displayed
            Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!");
            listener.onAdLoaded();
          }

          @Override
          public void onAdClicked(Ad ad) {
            // Ad clicked callback
            Log.d(TAG, "Interstitial ad clicked!");
            listener.onAdClicked();
          }

          @Override
          public void onLoggingImpression(Ad ad) {
            // Ad impression logged callback
            Log.d(TAG, "Interstitial ad impression logged!");
            listener.onAdImpression();
          }
        };
    ad.loadAd(ad.buildLoadAdConfig().withAdListener(interstitialAdListener).build());
  }

  @Override
  public void show() {
    ad.show();
  }
}
