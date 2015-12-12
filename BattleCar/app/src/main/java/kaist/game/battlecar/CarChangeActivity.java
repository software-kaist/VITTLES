package kaist.game.battlecar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaist.game.battlecar.adapter.CarApList;
import kaist.game.battlecar.adapter.CarApListAdapter;

public class CarChangeActivity extends Activity implements AdapterView.OnItemClickListener {
    private final static String TAG = CarChangeActivity.class.getSimpleName();
    private ScanResult scanResult;
    private WifiManager wm;
    private List apList;
    private ArrayList<CarApList> arrList = new ArrayList<CarApList>();
    private CarApListAdapter carListAdapter;
    private SharedPreferences setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_change);

        setting =  PreferenceManager.getDefaultSharedPreferences(this);

        searchVittles();
    }

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                searchWifi();
            }
        }
    };

    public void searchWifi() {
        apList = wm.getScanResults();
        if (wm.getScanResults() != null) {
            arrList.clear(); // 이전 리스트 삭제
            int size = apList.size();
            for (int i = 0; i < size; i++) {
                scanResult = (ScanResult) apList.get(i);
                Log.d(TAG, "onActivityResult " + scanResult);

                String apPrefix = setting.getString("vittles_ap_prefix", "");
                if (scanResult.SSID.toUpperCase().contains(apPrefix)) {
                    CarApList item = new CarApList(scanResult.SSID, scanResult.capabilities);
                    arrList.add(item);
                }
            }
        }

        if (arrList.size() > 0) {
            carListAdapter = new CarApListAdapter(this, R.layout.list_car_ap, arrList);
            ListView list = (ListView) findViewById(R.id.carListView);
            list.setAdapter(carListAdapter);
            list.setOnItemClickListener(this);
        } else {
            // todo : VITTLES 없으면 메세지 표시(재검색 또는 중지)
        }
    }

//    // ListView 안에 Item을 클릭시에 호출되는 Listener
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        String ssid = arrList.get(position).getCarApSSID();
        String capa = arrList.get(position).getCapabilities();

        Toast.makeText(this, ssid + capa, Toast.LENGTH_LONG).show();

        // Perference 에 저장
        SharedPreferences.Editor edit = setting.edit();
        edit.putString("my_vittles_ap", ssid);
        edit.commit();

        // wifi에 접속
        // Password 물어보고 접속
        WifiConfiguration wfc = new WifiConfiguration();
        wfc.SSID = "\"".concat(ssid).concat("\"");
        wfc.status = WifiConfiguration.Status.DISABLED;
        wfc.priority = 40;

        if (capa.toUpperCase().contains("Open")) {
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedAuthAlgorithms.clear();
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        } else if (capa.toUpperCase().contains("WEP")) {
            String password = "intintint";
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wfc.wepKeys[0] = "\"".concat(password).concat("\"");
            wfc.wepTxKeyIndex = 0;
        } else if (capa.toUpperCase().contains("WPA")) {
            String password = "intintint";
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wfc.preSharedKey = "\"".concat(password).concat("\"");
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();     //닫기
                }
            });
            alert.setMessage("Capabilites Error!!");
            alert.show();
        }

    };

    public void searchVittles() {
        wm = (WifiManager) getSystemService(WIFI_SERVICE); // WifiManager 초기화
        wm.startScan(); // 검색

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiReceiver, filter);
    }

    public void onSearchBtnClicked(View v) {
        searchVittles();
    }

    public void onHomeBtnClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

}
