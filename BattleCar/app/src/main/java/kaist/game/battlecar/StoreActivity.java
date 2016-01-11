package kaist.game.battlecar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;

import java.util.ArrayList;
import java.util.List;

import kaist.game.battlecar.util.Utils;

public class StoreActivity extends Activity {
    // SAMPLE APP CONSTANTS
    private static final String ACTIVITY_NUMBER = "activity_num";
    private static final String LOG_TAG = "iabv3";

    // PRODUCT & SUBSCRIPTION IDS
    private static final String PRODUCT_ID = "com.vittles.android.v2";
    private static final String SUBSCRIPTION_ID = "com.anjlab.test.iab.subs1";
    private static final String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyZRV65VN99UECFgXR7ghf71Rx44nAdnR8kMwVVa+eQGQJrvaN5Lku0rvArClTcEgofYVQtqmf/yanJf2TZrFnDgWwUSvU9xOcG/eQ5jAczg67Y0fDZ+nhNLT2u/8i6uE1pwxdOLKl2QntTi+2KBX6LZoxpT4hsIdg8UZM6dfalY0WRfdaqRKT01vIzOEIBDs2YuCeFjvNUxcubH/MQgNmVxje38CzwmSDo9+LVeBD1AzZytvkEkh9FBBFw6KLLlXixwIwgrSAiHuS5DGcTZ+g8S74g3vSWc2dgPhO5oWl8AhUvCn2UvOgMtxBd/okNVDfmf2aV8ed5R8r1Y01k9xLQIDAQAB"; // PUT YOUR MERCHANT KEY HERE;
    // put your Google merchant id here (as stated in public profile of your Payments Merchant Center)
    // if filled library will provide protection against Freedom alike Play Market simulators
    private static final String MERCHANT_ID="04775518919030000842";

    private BillingProcessor bp;
    private boolean readyToPurchase = false;

    //    private TextView Vcoin;
    private int coin=1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_store);

//        AdView mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

//        Vcoin= (TextView)findViewById(R.id.vcoin);

        Button back = (Button)findViewById(R.id.button_back);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        TabHost tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        // Tab1 Setting
        TabHost.TabSpec tabSpec1 = tabHost.newTabSpec("MyItem");
        tabSpec1.setIndicator("My Item"); // Tab Subject
        tabSpec1.setContent(R.id.tab_view_info); // Tab Content
        tabHost.addTab(tabSpec1);

        // Tab2 Setting
        TabHost.TabSpec tabSpec2 = tabHost.newTabSpec("Attack");
        tabSpec2.setIndicator("Attack"); // Tab Subject
        tabSpec2.setContent(R.id.tab_view_attack); // Tab Content
        tabHost.addTab(tabSpec2);

        // Tab3 Setting
        TabHost.TabSpec tabSpec3 = tabHost.newTabSpec("Defense");
        tabSpec3.setIndicator("Defense"); // Tab Subject
        tabSpec3.setContent(R.id.tab_view_defense); // Tab Content
        tabHost.addTab(tabSpec3);

        // Tab3 Setting
        TabHost.TabSpec tabSpec4 = tabHost.newTabSpec("VCoins");
        tabSpec4.setIndicator("V-Coins"); // Tab Subject
        tabSpec4.setContent(R.id.tab_view_v_coins); // Tab Content
        tabHost.addTab(tabSpec4);

        // show First Tab Content
        tabHost.setCurrentTab(0);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                showToast("Tab: " + tabId);
                if (tabId.equals("MyItem")) {
                    // Get My Item Information from APP Server
                } else if (tabId.equals("Attack")) {
                    //destroy mars
                } else if (tabId.equals("Defense")) {
                    //destroy mars
                } else if (tabId.equals("VCoins")) {
                    getInAppItem();
                }
            }
        });

        if(!BillingProcessor.isIabServiceAvailable(this)) {
            showToast("In-app billing service is unavailable, please upgrade Android Market/Play to version >= 3.9.16");
        }

        bp = new BillingProcessor(this, LICENSE_KEY, MERCHANT_ID, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(String productId, TransactionDetails details) {
                showToast("onProductPurchased: " + productId);
            }
            @Override
            public void onBillingError(int errorCode, Throwable error) {
                showToast("onBillingError: " + Integer.toString(errorCode));
            }
            @Override
            public void onBillingInitialized() {
                showToast("onBillingInitialized");
                readyToPurchase = true;
            }
            @Override
            public void onPurchaseHistoryRestored() {
                showToast("onPurchaseHistoryRestored");
                for(String sku : bp.listOwnedProducts())
                    Log.d(LOG_TAG, "Owned Managed Product: " + sku);
                for(String sku : bp.listOwnedSubscriptions())
                    Log.d(LOG_TAG, "Owned Subscription: " + sku);
            }
        });
    }

    @Override
    protected void onResume() {
        Utils.setCleanView(this, false);
        super.onResume();
    }

    public void getInAppItem() {
        ArrayList<String> arrayListOfProductIds = new ArrayList<String> ();
        arrayListOfProductIds.add("v_coins_01");
        arrayListOfProductIds.add("v_coins_02");
        List<SkuDetails> retList = bp.getPurchaseListingDetails(arrayListOfProductIds);

        for(SkuDetails tmp : retList) {
            setLayoutItems(R.id.ll_v_coins, tmp);
        }
    }

    public void setLayoutItems(int layoutId, SkuDetails item) {
        LinearLayout.LayoutParams lpMW;
        lpMW = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout sv = (LinearLayout)findViewById(layoutId);
        sv.setPadding(10, 10, 10, 10);

        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(lpMW);
        ll.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams layparam = (LinearLayout.LayoutParams) ll.getLayoutParams();
        layparam.rightMargin = 1;
        layparam.leftMargin = 1;
        ll.setLayoutParams(layparam);
        ll.setBackgroundResource(R.drawable.border);
        ll.setOrientation(LinearLayout.VERTICAL);
        sv.addView(ll);

        ImageView iv = new ImageView(this);
        iv.setLayoutParams(lpMW);
        iv.setBackgroundResource(R.mipmap.v_coin_1);
        ll.addView(iv);

        TextView tv = new TextView(this);
        tv.setLayoutParams(lpMW);
        tv.setText(item.description + "\n(" + item.priceText + ")\n");
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(15);
        ll.addView(tv);

        ImageButton ib = new ImageButton(this);
        ib.setBackgroundResource(R.mipmap.buy);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Clicked!");
            }
        });
        ll.addView(ib);
    }

     private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
