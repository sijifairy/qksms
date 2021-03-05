package com.moez.QKSMS.common.ad.nativead;

import android.app.Activity;
import android.util.Log;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;
import com.moez.QKSMS.common.ad.AdEngine;
import com.moez.QKSMS.common.ad.NmVendor;
import com.moez.QKSMS.common.ad.QkAdConfig;
import com.moez.QKSMS.common.ad.adapter.FacebookNativeAd;
import com.moez.QKSMS.common.ad.adapter.FacebookNativeBannerAd;
import com.moez.QKSMS.common.ad.adapter.MopubNativeAd;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.ViewBinder;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NmNativeAdLoader {

  private static final String TAG = NmNativeAdLoader.class.getSimpleName();

  private List<QkAdConfig> configs;

  public boolean isLoaded;
  public boolean isFailed;
  public NmNativeAdListener listener;

  private NmNativeAd ad;

  private int waterfallIndex = 0;
  private Activity activity;
  private ViewBinder viewBinder;

  public NmNativeAdLoader(List<QkAdConfig> configs, NmNativeAdListener listener) {
    this(null, null, configs, listener);
  }

  public NmNativeAdLoader(
      Activity activity,
      ViewBinder viewBinder,
      List<QkAdConfig> configs,
      NmNativeAdListener listener) {
    this.activity = activity;
    this.viewBinder = viewBinder;
    this.configs = configs;
    this.listener = listener;

    Collections.sort(
        configs,
        new Comparator<QkAdConfig>() {
          @Override
          public int compare(QkAdConfig t1, QkAdConfig t2) {
            return t1.index - t2.index;
          }
        });

    loadWaterfall();
  }

  private void loadWaterfall() {
    final QkAdConfig adConfig = configs.get(waterfallIndex);
    Log.d(
        "NativeAd",
        "load waterfall : "
            + waterfallIndex
            + ", "
            + adConfig.vendorName
            + ", "
            + adConfig.adUnitId);
    waterfallIndex++;
    if (NmVendor.VENDOR_FACEBOOK.equals(adConfig.vendorName)) {
      final NativeAd nativeAd =
          new NativeAd(AdEngine.getInstance().applicationContext, adConfig.adUnitId);
      NativeAdListener nativeAdListener =
          new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
              // Native ad finished downloading all assets
              Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
              // Native ad failed to load
              Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());
              if (waterfallIndex == configs.size()) {
                isFailed = true;
                if (listener != null) {
                  listener.onAdFailedToLoad(0);
                }
              } else {
                loadWaterfall();
              }
            }

            @Override
            public void onAdLoaded(Ad ad) {
              // Native ad is loaded and ready to be displayed
              Log.d(TAG, "Native ad is loaded and ready to be displayed!");
              NmNativeAdLoader.this.ad = new FacebookNativeAd(nativeAd);
              isLoaded = true;
              if (listener != null) {
                listener.onAdLoaded(NmNativeAdLoader.this.ad);
              }
            }

            @Override
            public void onAdClicked(Ad ad) {
              // Native ad clicked
              Log.d(TAG, "Native ad clicked!");
              if (listener != null) {
                listener.onAdClicked();
              }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
              // Native ad impression
              Log.d(TAG, "Native ad impression logged!");
              if (listener != null) {
                listener.onAdImpression();
              }
            }
          };
      // Request an ad
      nativeAd.loadAd(nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build());
    } else if (NmVendor.VENDOR_FACEBOOK_NATIEVE_BANNER.equals(adConfig.vendorName)) {
      final NativeBannerAd nativeAd =
          new NativeBannerAd(AdEngine.getInstance().applicationContext, adConfig.adUnitId);
      NativeAdListener nativeAdListener =
          new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
              // Native ad finished downloading all assets
              Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
              // Native ad failed to load
              Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());
              if (waterfallIndex == configs.size()) {
                isFailed = true;
                if (listener != null) {
                  listener.onAdFailedToLoad(0);
                }
              } else {
                loadWaterfall();
              }
            }

            @Override
            public void onAdLoaded(Ad ad) {
              // Native ad is loaded and ready to be displayed
              Log.d(TAG, "Native ad is loaded and ready to be displayed!");
              NmNativeAdLoader.this.ad = new FacebookNativeBannerAd(nativeAd);
              isLoaded = true;
              if (listener != null) {
                listener.onAdLoaded(NmNativeAdLoader.this.ad);
              }
            }

            @Override
            public void onAdClicked(Ad ad) {
              // Native ad clicked
              Log.d(TAG, "Native ad clicked!");
              if (listener != null) {
                listener.onAdClicked();
              }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
              // Native ad impression
              Log.d(TAG, "Native ad impression logged!");
              if (listener != null) {
                listener.onAdImpression();
              }
            }
          };
      // Request an ad
      nativeAd.loadAd(nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build());
    } else if (NmVendor.VENDOR_MOPUB.equals(adConfig.vendorName)) {
      if (activity == null || viewBinder == null) {
        if (waterfallIndex == configs.size()) {
          isFailed = true;
          if (listener != null) {
            listener.onAdFailedToLoad(0);
          }
        } else {
          loadWaterfall();
        }
        return;
      }

      MoPubNative moPubNative =
          new MoPubNative(
              activity,
              adConfig.adUnitId,
              new MoPubNative.MoPubNativeNetworkListener() {
                @Override
                public void onNativeLoad(com.mopub.nativeads.NativeAd nativeAd) {
                  Log.d(TAG, "Native ad is loaded and ready to be displayed!");
                  NmNativeAdLoader.this.ad = new MopubNativeAd(nativeAd);
                  isLoaded = true;
                  if (listener != null) {
                    listener.onAdLoaded(NmNativeAdLoader.this.ad);
                  }
                }

                @Override
                public void onNativeFail(NativeErrorCode errorCode) {
                  Log.d(TAG, "Native ad failed to load!");
                  if (waterfallIndex == configs.size()) {
                    isFailed = true;
                    if (listener != null) {
                      listener.onAdFailedToLoad(0);
                    }
                  } else {
                    loadWaterfall();
                  }
                }
              });
      MoPubStaticNativeAdRenderer moPubStaticNativeAdRenderer =
          new MoPubStaticNativeAdRenderer(viewBinder);
      moPubNative.registerAdRenderer(moPubStaticNativeAdRenderer);
      moPubNative.makeRequest();
    }
  }
}
