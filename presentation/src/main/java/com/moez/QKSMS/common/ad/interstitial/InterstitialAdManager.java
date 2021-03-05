package com.moez.QKSMS.common.ad.interstitial;

import android.app.Activity;
import com.moez.QKSMS.common.ad.QkAdConfig;
import java.util.HashMap;
import java.util.List;

public class InterstitialAdManager {

  private static HashMap<String, InterstitialAdRequest> sInterstitialAdMap = new HashMap<>();

  public static void preload(
      Activity activity, String placementName, List<QkAdConfig> adConfigList) {
    if (!sInterstitialAdMap.containsKey(placementName)) {
      InterstitialAdRequest interstitialAdRequest =
          new InterstitialAdRequest(activity, adConfigList);
      sInterstitialAdMap.put(placementName, interstitialAdRequest);
    } else {
      if (sInterstitialAdMap.get(placementName).isFailed) {
        sInterstitialAdMap.remove(placementName);
        InterstitialAdRequest interstitialAdRequest =
            new InterstitialAdRequest(activity, adConfigList);
        sInterstitialAdMap.put(placementName, interstitialAdRequest);
      }
    }
  }

  public static InterstitialAdRequest fetch(String placementName) {
    if (sInterstitialAdMap.containsKey(placementName)) {
      return sInterstitialAdMap.remove(placementName);
    } else {
      return null;
    }
  }
}
