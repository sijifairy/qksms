package com.moez.QKSMS.feature.plus;

import androidx.annotation.NonNull;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.Purchase.PurchaseState;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.moez.QKSMS.common.BaseApplication;
import com.moez.QKSMS.common.notificationcenter.GlobalNotificationCenter;
import com.moez.QKSMS.common.util.Preferences;
import com.moez.QKSMS.common.util.Threads;
import java.util.ArrayList;
import java.util.List;

public class PlusManager {

  public static final String PREF_KEY_USER_HAS_VERIFIED_SUCCESS = "PREF_KEY_USER_HAS_VERIFIED_SUCCESS";
  public static final String BILLING_VERIFY_SUCCESS = "billing.verify.success";

  private static class PlusHolder {

    private static PlusManager sInstance = new PlusManager();
  }

  public static PlusManager getInstance() {
    return PlusHolder.sInstance;
  }

  public static boolean isPremiumUser() {
    return isPremium;
  }

  public static boolean isPremium = hasUserEverVerifiedSuccessfully();

  public static boolean hasUserEverVerifiedSuccessfully() {
    return Preferences.getDefault().getBoolean(PREF_KEY_USER_HAS_VERIFIED_SUCCESS, false);
  }

  private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {

    }
  };

  private BillingClient billingClient = BillingClient.newBuilder(BaseApplication.getContext())
      .setListener(purchasesUpdatedListener)
      .enablePendingPurchases()
      .build();

  public PlusManager() {
    billingClient.startConnection(new BillingClientStateListener() {
      @Override
      public void onBillingSetupFinished(BillingResult billingResult) {
        Purchase.PurchasesResult purchasesResult = billingClient
            .queryPurchases(BillingClient.SkuType.INAPP);
        List<Purchase> purchases = purchasesResult.getPurchasesList();
        if (purchases != null && purchases.size() > 0) {
          for (Purchase purchase : purchases) {
            if ("remove_ads".equals(purchase.getSku()) && purchase.isAcknowledged()) {
              Threads.postOnMainThread(
                  () -> GlobalNotificationCenter.sendNotification(BILLING_VERIFY_SUCCESS));
              Preferences.getDefault().putBoolean(PREF_KEY_USER_HAS_VERIFIED_SUCCESS, true);
              isPremium = true;
            } else if ("remove_ads".equals(purchase.getSku())
                && purchase.getPurchaseState() == PurchaseState.PURCHASED) {
              AcknowledgePurchaseParams acknowledgePurchaseParams =
                  AcknowledgePurchaseParams.newBuilder()
                      .setPurchaseToken(purchase.getPurchaseToken())
                      .build();
              billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult1 -> {
                isPremium = true;
                Preferences.getDefault().putBoolean(PREF_KEY_USER_HAS_VERIFIED_SUCCESS, true);
              });
            }
          }
        } else {
          isPremium = false;
        }

        if (billingResult.getResponseCode() == BillingResponseCode.OK) {
          List<String> skuList = new ArrayList<>();
          skuList.add("remove_ads");
          SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
          params.setSkusList(skuList).setType(SkuType.INAPP);
          billingClient.querySkuDetailsAsync(params.build(),
              new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(BillingResult billingResult,
                    List<SkuDetails> skuDetailsList) {
                  // Process the result.
                }
              });
        }
      }

      @Override
      public void onBillingServiceDisconnected() {
        // Try to restart the connection on the next request to
        // Google Play by calling the startConnection() method.
      }
    });
  }
}

