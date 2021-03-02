package com.moez.QKSMS.feature.plus;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.notificationcenter.CommonBundle;
import com.moez.QKSMS.common.notificationcenter.GlobalNotificationCenter;
import com.moez.QKSMS.common.notificationcenter.INotificationObserver;
import com.moez.QKSMS.common.util.BackgroundDrawables;
import com.moez.QKSMS.common.util.Dimensions;
import com.moez.QKSMS.common.util.Preferences;
import java.util.ArrayList;
import java.util.List;

public class BillingActivity extends AppCompatActivity implements INotificationObserver,
    PurchasesUpdatedListener {

  private BillingClient billingClient;
  private SkuDetails product;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_billing);

    TextView currencyCodeTv = findViewById(R.id.currency_code_text_view);
    TextView priceTv = findViewById(R.id.price_text_view);
    TextView purchaseButton = findViewById(R.id.purchase_text_view);

    billingClient = BillingClient.newBuilder(this).setListener(this)
        .enablePendingPurchases().build();
    billingClient.startConnection(new BillingClientStateListener() {
      @Override
      public void onBillingSetupFinished(BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
          // The BillingClient is ready. You can query purchases here.
          List<String> skuList = new ArrayList<>();
          skuList.add("remove_ads");
          SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
          params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
          billingClient.querySkuDetailsAsync(params.build(),
              new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(BillingResult billingResult,
                    List<SkuDetails> skuDetailsList) {
                  // Process the result.
                  if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                      && skuDetailsList.size() > 0) {
                    product = skuDetailsList.get(0);

                    String price = product.getPrice();
                    int amountStartIndex = 0;

                    for (int i = 0, length = price.length(); i < length; i++) {
                      if (Character.isDigit(price.charAt(i))) {
                        amountStartIndex = i;
                        break;
                      }
                    }
                    currencyCodeTv.setText(price.substring(0, amountStartIndex));
                    priceTv.setText(price.substring(amountStartIndex));
                  }
                }
              });
        }
      }

      @Override
      public void onBillingServiceDisconnected() {
        // Try to restart the connection on the next request to
        // Google Play by calling the startConnection() method.
        Log.d("billing", "connect failed");
      }
    });

    purchaseButton.setBackground(BackgroundDrawables.
        createBackgroundDrawable(0xff1db255, Dimensions.pxFromDp(27), true));
    purchaseButton.setOnClickListener(v -> {
      if (product == null) {
        return;
      }

      // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
      BillingFlowParams flowParams = BillingFlowParams.newBuilder()
          .setSkuDetails(product)
          .build();
      BillingResult result = billingClient.launchBillingFlow(BillingActivity.this, flowParams);

    });

    ImageView closeActionImage = findViewById(R.id.action_close);
    closeActionImage.setOnClickListener(v -> finish());

    GlobalNotificationCenter.addObserver(PlusManager.BILLING_VERIFY_SUCCESS, this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
//        acbIAPProductRequest.cancel();
    GlobalNotificationCenter.removeObserver(this);
  }

  @Override
  public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
        && purchases != null) {
      for (Purchase purchase : purchases) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
          PlusManager.isPremium = true;

          Preferences.getDefault().putBoolean(PlusManager.PREF_KEY_USER_HAS_VERIFIED_SUCCESS, true);

          if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams acknowledgePurchaseParams =
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            billingClient.acknowledgePurchase(acknowledgePurchaseParams,
                new AcknowledgePurchaseResponseListener() {
                  @Override
                  public void onAcknowledgePurchaseResponse(BillingResult billingResult) {

                  }
                });
          }

          GlobalNotificationCenter.sendNotification(PlusManager.BILLING_VERIFY_SUCCESS);
        }
      }
    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
      // Handle an error caused by a user cancelling the purchase flow.
    } else {
      // Handle any other error codes.
    }
  }

  @Override
  public void onReceive(String s, CommonBundle bundle) {
    if (PlusManager.BILLING_VERIFY_SUCCESS.equals(s)) {
      finish();
    }
  }
}
