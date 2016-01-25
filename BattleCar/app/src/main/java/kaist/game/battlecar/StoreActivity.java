package kaist.game.battlecar;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kaist.game.battlecar.adapter.ItemInfo;
import kaist.game.battlecar.util.Utils;

public class StoreActivity extends Activity {
    private static final String LOG_TAG = StoreActivity.class.getSimpleName();;

    private final static int DYNAMIC_IN_APP_BUY_BTN_ID = 0x8000;
    private final static int DYNAMIC_ITEM_BUY_BTN_ID = 0x8100;

    private ArrayList<String> inAppList;
    private ArrayList<ItemInfo> itemValueList = new ArrayList<ItemInfo> ();
    private Context mContext;
    private int myCoins = 0;
    private SharedPreferences setting;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_store);

        mContext = this;
        setting =  PreferenceManager.getDefaultSharedPreferences(this);

        displayMyCoins();

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
        getMyItem();
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (tabId.equals("MyItem")) {
                    getMyItem();
                } else if (tabId.equals("Attack")) {
                    getAttackItem();
                } else if (tabId.equals("Defense")) {
                    getDefenseItem();
                } else if (tabId.equals("VCoins")) {
                    getInAppItem();
                }
            }
        });

        // todo: 아이템 서버 및 구글플레이에 접속하기 위해 인터넷에 연결 필요! 비틀즈와 연결 해제 후
        // todo: 스토어화면 나갈 때 다시 비틀즈로 접속하게 수정 필요!
        setAttackButton();
        setDefenseButton();

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

    public void displayMyCoins() {
        //        myCoins = setting.getInt("my_coins", 0);
        myCoins = Integer.parseInt(setting.getString("my_coins", "0"));
        TextView text = (TextView)findViewById(R.id.textViewName);
        text.setText("My VITTLES Coins: " + myCoins);
        text.setTextSize(20);
        text.setTextColor(Color.YELLOW);
        text.setGravity(Gravity.CENTER);
    }

    public void getMyItem() {
        LinearLayout sv = (LinearLayout)findViewById(R.id.ll_info);
        sv.removeAllViewsInLayout();
        insertMyItem(R.id.ll_info, R.mipmap.shield_01, "Shield", setting.getString("shield", "10"), "");
        insertMyItem(R.id.ll_info, R.mipmap.magazine_01, "Magazine", setting.getString("magazine", "50"), "");
        insertMyItem(R.id.ll_info, R.mipmap.reload_02, "Reload SEC", setting.getString("reload_system", "10"), "");
        insertMyItem(R.id.ll_info, R.mipmap.emp_02, "EMP", setting.getString("emp", "0"), "");
        insertMyItem(R.id.ll_info, R.mipmap.healing_03, "Healing", setting.getString("healing", "0"), "");
    }

    public void insertMyItem(int layoutId, int imgId, String title, String val, String measure) {
        LinearLayout sv = (LinearLayout)findViewById(layoutId);
        sv.setPadding(10, 10, 10, 10);

        LayoutInflater inflater =  (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View col = inflater.inflate(R.layout.list_my_item, null);

        ImageView iv = (ImageView)col.findViewById(R.id.itemImg);
        iv.setBackgroundResource(imgId);
        // todo: 화면크기에 대응하도록 수정 필요!
        ViewGroup.LayoutParams param= iv.getLayoutParams();
        param.width = 150;
        param.height = 150;
        iv.setLayoutParams(param);

        TextView tv = (TextView)col.findViewById(R.id.tvItemName);
        tv.setText(title);
        tv.setGravity(Gravity.CENTER);

        tv = (TextView)col.findViewById(R.id.tvItemValue);
        tv.setText(val);
        tv.setGravity(Gravity.CENTER);

        sv.addView(col);
    }

    public void getAttackItem() {
        // todo: 서버에서 Attack Items을 받아서 보여주게 수정
        LinearLayout sv = (LinearLayout)findViewById(R.id.ll_attack);
        sv.removeAllViewsInLayout();
        itemValueList.clear();

        insertItemInfo(R.id.ll_attack, R.mipmap.magazine_01, "Magazines", 15, 1000, 0);
        insertItemInfo(R.id.ll_attack, R.mipmap.magazine_01, "Magazines", 20, 2000, 1);
        insertItemInfo(R.id.ll_attack, R.mipmap.magazine_01, "Magazines", 25, 3000, 2);
        insertItemInfo(R.id.ll_attack, R.mipmap.magazine_01, "Magazines", 30, 4000, 3);
        insertItemInfo(R.id.ll_attack, R.mipmap.magazine_01, "Magazines", 40, 5000, 4);
    }

    public void insertItemInfo(int layoutId, int imgId, String description, int itemVal, int itemPrice, int idx) {
        itemValueList.add(new ItemInfo(description, itemVal, itemPrice));
        LinearLayout sv = (LinearLayout)findViewById(layoutId);
        sv.setPadding(10, 10, 10, 10);

        LayoutInflater inflater =  (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View col = inflater.inflate(R.layout.list_store_item, null);

        ImageView iv = (ImageView)col.findViewById(R.id.itemImg);
        iv.setBackgroundResource(imgId);
        // todo: 화면크기에 대응하도록 수정 필요!
        ViewGroup.LayoutParams param= iv.getLayoutParams();
        param.width = 100;
        param.height = 100;
        iv.setLayoutParams(param);

        TextView tv = (TextView)col.findViewById(R.id.tvDescription);
        tv.setText(description + " " + itemVal + "\n(VC " + itemPrice + ")\n");
        tv.setGravity(Gravity.CENTER);

        ImageButton ib = (ImageButton)col.findViewById(R.id.ibBuy);
        ib.setId(R.id.ibBuy + DYNAMIC_ITEM_BUY_BTN_ID + idx);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int idx = v.getId() - (R.id.ibBuy + DYNAMIC_ITEM_BUY_BTN_ID);
                ItemInfo itemInfo = itemValueList.get(idx);
                String name = itemInfo.getItemName();
                int val = itemInfo.getItemValue();
                int price = itemInfo.getItemPrice();

//                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                builder.setTitle("종료 확인 대화 상자")
//                        .setMessage("아이템(" + name + " " + val + " - VC" + price + ")을 구매하시겠습니까?")
//                        .setCancelable(false)
//                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                finish();
//                            }
//                        })
//                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                dialog.cancel();
//                            }
//                        });
//                AlertDialog dialog = builder.create();
//                dialog.show();

                showToast("아이템(" + name + " " + val + " - VC" + price + ")을 구매하시겠습니까?");

                if (myCoins < price) {
                    showToast("VITTLES Coin이 부족합니다.");
                    return;
                }

                myCoins -= itemInfo.getItemPrice();
                SharedPreferences.Editor edit = setting.edit();

                if (name.equals("Magazines")) {
                    edit.putString("magazine", String.format("%d", val));
//                    edit.putInt("magazine", val);
                } else if (name.equals("Reload System")) {
                    edit.putString("reload_system", String.format("%d", val));
//                    edit.putInt("reload_system", val);
                } else if (name.equals("EMP")) {
                    int sVal = Integer.parseInt(setting.getString("emp", "0"));
                    edit.putString("emp", String.format("%d", sVal + val));
//                    int sVal = setting.getInt("emp", 0);
//                    edit.putInt("emp", sVal + val);
                } else if (name.equals("Shield")) {
                    int sVal = Integer.parseInt(setting.getString("shield", "10"));
                    edit.putString("shield", String.format("%d", sVal + val));
//                    int sVal = setting.getInt("shield", 10);
//                    edit.putInt("shield", sVal + val);
                } else if (name.equals("Healing")) {
                    int sVal = Integer.parseInt(setting.getString("healing", "0"));
                    edit.putString("healing", String.format("%d", sVal + val));
//                    int sVal = setting.getInt("healing", 0);
//                    edit.putInt("healing", sVal + val);
                } else {
                    myCoins += itemInfo.getItemPrice();
                }

                edit.putString("my_coins", String.format("%d", myCoins));
//                edit.putInt("my_coins", myCoins);
                edit.commit();
                displayMyCoins();
            }
        });

        sv.addView(col);
    }

    public void setAttackButton() {
        // ADD Magazien, Reload System, EMP Button
        LinearLayout linear = (LinearLayout) findViewById(R.id.ll_attack_btn);

        Button btnMagazine = new Button(this);
        btnMagazine.setText("Magazine");
        btnMagazine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout sv = (LinearLayout)findViewById(R.id.ll_attack);
                sv.removeAllViewsInLayout();
                itemValueList.clear();
                insertItemInfo(R.id.ll_attack, R.mipmap.magazine_01, "Magazines", 15, 1000, 0);
                insertItemInfo(R.id.ll_attack, R.mipmap.magazine_01, "Magazines", 20, 2000, 1);
                insertItemInfo(R.id.ll_attack, R.mipmap.magazine_01, "Magazines", 25, 3000, 2);
                insertItemInfo(R.id.ll_attack, R.mipmap.magazine_01, "Magazines", 30, 4000, 3);
                insertItemInfo(R.id.ll_attack, R.mipmap.magazine_01, "Magazines", 40, 5000, 4);
            }
        });
        linear.addView(btnMagazine);

        Button btnReloadSystem = new Button(this);
        btnReloadSystem.setText("Reload System");
        btnReloadSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout sv = (LinearLayout)findViewById(R.id.ll_attack);
                sv.removeAllViewsInLayout();
                itemValueList.clear();
                insertItemInfo(R.id.ll_attack, R.mipmap.reload_02, "Reload System", 7, 1000, 0);
                insertItemInfo(R.id.ll_attack, R.mipmap.reload_02, "Reload System", 5, 2000, 1);
                insertItemInfo(R.id.ll_attack, R.mipmap.reload_02, "Reload System", 3, 3000, 2);
                insertItemInfo(R.id.ll_attack, R.mipmap.reload_02, "Reload System", 1, 4000, 3);
            }
        });
        linear.addView(btnReloadSystem);

        Button btnEmp = new Button(this);
        btnEmp.setText("EMP");
        btnEmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout sv = (LinearLayout)findViewById(R.id.ll_attack);
                sv.removeAllViewsInLayout();
                itemValueList.clear();
                insertItemInfo(R.id.ll_attack, R.mipmap.emp_02, "EMP", 5, 1000, 0);
                insertItemInfo(R.id.ll_attack, R.mipmap.emp_02, "EMP", 10, 2000, 1);
                insertItemInfo(R.id.ll_attack, R.mipmap.emp_02, "EMP", 15, 3000, 2);
                insertItemInfo(R.id.ll_attack, R.mipmap.emp_02, "EMP", 20, 4000, 3);
            }
        });
        linear.addView(btnEmp);
    }

    public void getDefenseItem() {
        // todo: 서버에서 Defense Items을 받아서 보여주게 수정
        LinearLayout sv = (LinearLayout)findViewById(R.id.ll_defense);
        sv.removeAllViewsInLayout();
        itemValueList.clear();
        insertItemInfo(R.id.ll_defense, R.mipmap.shield_01, "Shield", 1, 1000, 0);
        insertItemInfo(R.id.ll_defense, R.mipmap.shield_01, "Shield", 3, 2000, 1);
        insertItemInfo(R.id.ll_defense, R.mipmap.shield_01, "Shield", 5, 3000, 2);
        insertItemInfo(R.id.ll_defense, R.mipmap.shield_01, "Shield", 7, 4000, 3);
        insertItemInfo(R.id.ll_defense, R.mipmap.shield_01, "Shield", 10, 5000, 4);
    }

    public void setDefenseButton() {
        // ADD Shield, Healing Button
        LinearLayout linear = (LinearLayout) findViewById(R.id.ll_defense_btn);

        Button btnShield = new Button(this);
        btnShield.setText("Shield");
        btnShield.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout sv = (LinearLayout)findViewById(R.id.ll_defense);
                sv.removeAllViewsInLayout();
                itemValueList.clear();
                // todo: MAX 20
                insertItemInfo(R.id.ll_defense, R.mipmap.shield_01, "Shield", 1, 1000, 0);
                insertItemInfo(R.id.ll_defense, R.mipmap.shield_01, "Shield", 3, 2000, 1);
                insertItemInfo(R.id.ll_defense, R.mipmap.shield_01, "Shield", 5, 3000, 2);
                insertItemInfo(R.id.ll_defense, R.mipmap.shield_01, "Shield", 7, 4000, 3);
                insertItemInfo(R.id.ll_defense, R.mipmap.shield_01, "Shield", 10, 5000, 4);
            }
        });
        linear.addView(btnShield);

        Button btnHealing = new Button(this);
        btnHealing.setText("Healing");
        btnHealing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout sv = (LinearLayout)findViewById(R.id.ll_defense);
                sv.removeAllViewsInLayout();
                itemValueList.clear();
                insertItemInfo(R.id.ll_defense, R.mipmap.healing_03, "Healing", 1, 1000, 0);
                insertItemInfo(R.id.ll_defense, R.mipmap.healing_03, "Healing", 3, 2000, 1);
                insertItemInfo(R.id.ll_defense, R.mipmap.healing_03, "Healing", 5, 3000, 2);
                insertItemInfo(R.id.ll_defense, R.mipmap.healing_03, "Healing", 7, 4000, 3);
                insertItemInfo(R.id.ll_defense, R.mipmap.healing_03, "Healing", 10, 5000, 4);
            }
        });
        linear.addView(btnHealing);
    }

    public void getInAppItem() {
        LinearLayout sv = (LinearLayout)findViewById(R.id.ll_v_coins);
        sv.removeAllViewsInLayout();

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
                insertInAppItem(R.id.ll_v_coins, R.mipmap.v_coin_1, thisResponse, idx);
                idx++;
            }
        }
    }

    public void insertInAppItem(int layoutId, int imgId, String itemDtails, int idx) {
        String description, price;
        try {
            JSONObject object = new JSONObject(itemDtails);
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

        ImageView iv = (ImageView)col.findViewById(R.id.itemImg);
        iv.setBackgroundResource(imgId);
        ViewGroup.LayoutParams param= iv.getLayoutParams();
        param.width = 150;
        param.height = 150;
        iv.setLayoutParams(param);

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
