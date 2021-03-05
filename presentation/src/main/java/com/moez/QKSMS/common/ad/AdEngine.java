package com.moez.QKSMS.common.ad;

import android.content.Context;

public class AdEngine {

  private static class AdEngineHolder {
    private static AdEngine instance = new AdEngine();
  }

  public static AdEngine getInstance() {
    return AdEngineHolder.instance;
  }

  public Context applicationContext;

  public void setApplicationContext(Context applicationContext) {
    this.applicationContext = applicationContext;
    //    AppLovinSdk.initializeSdk(applicationContext);
  }

  public String unityGameId;

  public void setUnityGameId(String gameId) {
    unityGameId = gameId;
  }
}
