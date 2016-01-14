package kaist.game.battlecar;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kaist.game.battlecar.util.Utils;

public class StoreActivity extends Activity {
    private static final String LOG_TAG = StoreActivity.class.getSimpleName();;

    public final static int DYNAMIC_IN_APP_BUY_BTN_ID = 0x8000;
    public ArrayList<String> inAppList;
    private IInAppBillingService mService;
    private ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

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

        // In App Billing
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    protected void onResume() {
        Utils.setCleanView(this, false);
        super.onResume();
    }

    public void getInAppItem() {
        ArrayList<String> skuList = new ArrayList<String> ();
        skuList.add("v_coins_01");
        skuList.add("v_coins_02");
        skuList.add("v_coins_03");
        skuList.add("v_coins_04");
        skuList.add("v_coins_05");
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

        Bundle skuDetails;
        try {
            skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);
            Log.i(LOG_TAG, "getSkuDetails() - success return Bundle");
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.w(LOG_TAG, "getSkuDetails() - fail!");
            return;
        }

        int response = skuDetails.getInt("RESPONSE_CODE");
        Log.i(LOG_TAG, "getSkuDetails() - \"RESPONSE_CODE\" return " + String.valueOf(response));

        if (response == 0) {
            inAppList = skuDetails.getStringArrayList("DETAILS_LIST");
            int idx = 0;
            for (String thisResponse : inAppList) {
                setLayoutItems(R.id.ll_v_coins, thisResponse, idx);
                idx++;
            }
        }
    }

    public void setLayoutItems(int layoutId, String itemDtails, int idx) {
        String sku, description, price;
        try {
            JSONObject object = new JSONObject(itemDtails);
            sku = object.getString("productId");
            description = object.getString("description");
            price = object.getString("price");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        LinearLayout sv = (LinearLayout)findViewById(layoutId);
        sv.setPadding(10, 10, 10, 10);

        LayoutInflater inflater =  (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View col = inflater.inflate(R.layout.list_store_item, null);

        TextView tv = (TextView)col.findViewById(R.id.tvDescription);
        tv.setText(description + "\n(" + price + ")\n");
        tv.setGravity(Gravity.CENTER);

        ImageButton ib = (ImageButton)col.findViewById(R.id.ibBuy);
        ib.setId(R.id.ibBuy + DYNAMIC_IN_APP_BUY_BTN_ID + idx);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int idx = v.getId() - (R.id.ibBuy + DYNAMIC_IN_APP_BUY_BTN_ID);
                try {
                    JSONObject object = new JSONObject(inAppList.get(idx));
                    purchase(object.getString("productId"));
//                    showToast("Clicked!" + v.getId() + " " + object.getString("productId"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
            }
        });

        sv.addView(col);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void purchase(String productId){
        if (mService == null) return;

        Bundle ownedItems;
        try {
            ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
            Log.i(LOG_TAG, "getPurchases() - success return Bundle");
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.w(LOG_TAG, "getPurchases() - fail!");
            return;
        }

        int response = ownedItems.getInt("RESPONSE_CODE");
        Log.i(LOG_TAG, "getPurchases() - \"RESPONSE_CODE\" return " + String.valueOf(response));

        if (response != 0) return;

        ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
        ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
        ArrayList<String> signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE");
        String continuationToken = ownedItems.getString("INAPP_CONTINUATION_TOKEN");

        // 구한 내역으로 화면 구성을 다시!!
        // 오류로 구매 완료가 안된 경우 처리 필요!!
//        Log.i(tag, "getPurchases() - \"INAPP_PURCHASE_ITEM_LIST\" return " + ownedSkus.toString());
//        Log.i(tag, "getPurchases() - \"INAPP_PURCHASE_DATA_LIST\" return " + purchaseDataList.toString());
//        Log.i(tag, "getPurchases() - \"INAPP_DATA_SIGNATURE\" return " + (signatureList != null ? signatureList.toString() : "null"));
//        Log.i(tag, "getPurchases() - \"INAPP_CONTINUATION_TOKEN\" return " + (continuationToken != null ? continuationToken : "null"));

        // TODO: management owned purchase

//        "android.test.purchased"
        try {
            Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), productId, "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
            startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
//                    alert("You have bought the " + sku + ". Excellent choice, adventurer!");
                }
                catch (JSONException e) {
//                    alert("Failed to parse purchase data.");
                    e.printStackTrace();
                }
            }
        }
    }
}
