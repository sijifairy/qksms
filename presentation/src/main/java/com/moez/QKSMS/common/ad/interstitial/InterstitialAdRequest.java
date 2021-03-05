package com.moez.QKSMS.common.ad.interstitial;

import android.app.Activity;
import android.util.Log;
import com.moez.QKSMS.common.ad.NmAdListener;
import com.moez.QKSMS.common.ad.NmVendor;
import com.moez.QKSMS.common.ad.QkAdConfig;
import com.moez.QKSMS.common.ad.adapter.FacebookInterstitialAd;
import com.moez.QKSMS.common.ad.adapter.MopubInterstitialAd;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InterstitialAdRequest {

  private boolean isLoaded;
  public boolean isFailed;
  public NmAdListener listener;

  private NmInterstitialAd ad;
  private List<QkAdConfig> adConfigs;
  private int waterfallIndex = 0;
  private Activity activity;

  public InterstitialAdRequest(Activity activity, List<QkAdConfig> adConfigs) {
    this.adConfigs = adConfigs;
    this.activity = activity;

    Collections.sort(
        adConfigs,
        new Comparator<QkAdConfig>() {
          @Override
          public int compare(QkAdConfig t1, QkAdConfig t2) {
            return t1.index - t2.index;
          }
        });

    loadWaterfall();
  }

  public boolean isLoaded() {
    return isLoaded;
  }

  private void loadWaterfall() {
    QkAdConfig adConfig = adConfigs.get(waterfallIndex);
    Log.d(
        "InterstitialAdRequest",
        "load waterfall : "
            + waterfallIndex
            + ", "
            + adConfig.vendorName
            + ", "
            + adConfig.adUnitId);
    waterfallIndex++;
    NmAdListener nmAdListener =
        new NmAdListener() {
          @Override
          public void onAdClose() {
            if (listener != null) {
              listener.onAdClose();
            }
          }

          @Override
          public void onAdFailedToLoad(int var1) {
            if (waterfallIndex == adConfigs.size()) {
              isFailed = true;
              if (listener != null) {
                listener.onAdFailedToLoad(var1);
              }
            } else {
              loadWaterfall();
            }
            Log.d("InterstitialAdRequest", "failed to load");
          }

          @Override
          public void onAdOpened() {
            if (listener != null) {
              listener.onAdOpened();
            }
          }

          @Override
          public void onAdLoaded() {
            isLoaded = true;
            if (listener != null) {
              listener.onAdLoaded();
            }
            Log.d("InterstitialAdRequest", "on loaded");
          }

          @Override
          public void onAdClicked() {
            if (listener != null) {
              listener.onAdClicked();
            }
          }

          @Override
          public void onAdImpression() {
            if (listener != null) {
              listener.onAdImpression();
            }
            Log.d("InterstitialAdRequest", "on impression");
          }
        };
    if (NmVendor.VENDOR_FACEBOOK.equals(adConfig.vendorName)) {
      ad = new FacebookInterstitialAd(adConfig.adUnitId, nmAdListener);
    } else if (NmVendor.VENDOR_MOPUB.equals(adConfig.vendorName)) {
      ad = new MopubInterstitialAd(activity, adConfig.adUnitId, nmAdListener);
    } else {
      if (waterfallIndex == adConfigs.size()) {
        isFailed = true;
        if (listener != null) {
          listener.onAdFailedToLoad(0);
        }
      } else {
        loadWaterfall();
      }
    }
  }

  public void show() {
    if (isLoaded()) {
      if (ad != null) {
        ad.show();
      }
      isLoaded = false;
    }
  }

  public boolean isFacebookAd() {
    return ad != null && isLoaded() && ad instanceof FacebookInterstitialAd;
  }

  public void release() {
    listener = null;
  }
}
