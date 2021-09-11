package com.lahiriproductions.lambrk_messenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class BillingActivity extends AppCompatActivity {

    private Context mContext;

    private static final String TAG = BillingActivity.class.getSimpleName();
    private BillingClient billingClient;
    private PurchasesUpdatedListener purchasesUpdatedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        mContext = BillingActivity.this;

        purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                // To be implemented in a later section.
                Log.e(TAG, "onPurchasesUpdated: " + billingResult.getDebugMessage() + " " + purchases);
            }
        };

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();




        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                billingClient.startConnection(new BillingClientStateListener() {
                    @Override
                    public void onBillingSetupFinished(BillingResult billingResult) {
                        if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                            List<String> skuList = new ArrayList<>();
                            skuList.add("test_01");
                            SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                            params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                            billingClient.querySkuDetailsAsync(params.build(),
                                    new SkuDetailsResponseListener() {
                                        @Override
                                        public void onSkuDetailsResponse(BillingResult billingResult,
                                                                         List<SkuDetails> skuDetailsList) {
                                            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                                    .setSkuDetails(skuDetailsList.get(0))
                                                    .build();
                                            int responseCode = billingClient.launchBillingFlow(BillingActivity.this, billingFlowParams).getResponseCode();
                                            Log.e(TAG, "onSkuDetailsResponse: " + String.valueOf(responseCode));
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
        });

    }

}