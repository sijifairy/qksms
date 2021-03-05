package com.moez.QKSMS.common.ad

import com.google.gson.Gson
import com.moez.QKSMS.BuildConfig
import com.moez.QKSMS.common.util.RemoteConfig

object AdConfig {

    val detailWireAdConfig: List<QkAdConfig>
        get() = if (BuildConfig.DEBUG) {
            Gson().fromJson<List<QkAdConfig>>("[{\"vendorName\":\"mopub\", \"adUnitId\":\"24534e1901884e398f1253216226017e\", \"index\":0},{\"vendorName\":\"facebook\", \"adUnitId\":\"143325504253225_161996382386137\", \"index\":1}]")
        } else
            Gson().fromJson<List<QkAdConfig>>(RemoteConfig.instance.getString("AdConfigDetailWire")!!)

    val detailNativeAdConfig: List<QkAdConfig>
        get() = if (BuildConfig.DEBUG) {
            Gson().fromJson<List<QkAdConfig>>("[{\"vendorName\":\"mopub\", \"adUnitId\":\"11a17b188668469fb0412708c3d16813\", \"index\":2},{\"vendorName\":\"admob\", \"adUnitId\":\"ca-app-pub-3940256099942544/2247696110\", \"index\":1}]")
        } else
            Gson().fromJson<List<QkAdConfig>>(RemoteConfig.instance.getString("AdConfigDetailNative")!!)

    val homeNativeAdConfig: List<QkAdConfig>
        get() = if (BuildConfig.DEBUG) {
            Gson().fromJson<List<QkAdConfig>>("[{\"vendorName\":\"mopub\", \"adUnitId\":\"11a17b188668469fb0412708c3d16813\", \"index\":2},{\"vendorName\":\"facebooknativebanner\", \"adUnitId\":\"143325504253225_143326594253116\", \"index\":1}]")
        } else
            Gson().fromJson<List<QkAdConfig>>(RemoteConfig.instance.getString("AdConfigHomeNative")!!)
}
