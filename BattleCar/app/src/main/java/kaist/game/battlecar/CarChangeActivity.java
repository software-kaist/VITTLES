package kaist.game.battlecar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import kaist.game.battlecar.adapter.CarApList;
import kaist.game.battlecar.adapter.CarApListAdapter;
import kaist.game.battlecar.util.Utils;
import kaist.game.battlecar.util.VittlesConnector;

public class CarChangeActivity extends Activity implements AdapterView.OnItemClickListener {
    private final static String TAG = CarChangeActivity.class.getSimpleName();
    private ArrayList<CarApList> arrList = new ArrayList<CarApList>();
    private CarApListAdapter carListAdapter;
    private SharedPreferences setting;
    private VittlesConnector wifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_change);

        setting =  PreferenceManager.getDefaultSharedPreferences(this);
        wifi = new VittlesConnector(this, setting.getString("vittles_ap_prefix", "VITTLES"));

        searchVittles();
    }

    @Override
    protected void onResume() {
        Utils.setCleanView(this, false);
        super.onResume();
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
        arrList = wifi.searchWifi();

        if (arrList.size() > 0) {
            carListAdapter = new CarApListAdapter(this, R.layout.list_car_ap, arrList);
            ListView list = (ListView) findViewById(R.id.carListView);
            list.setAdapter(carListAdapter);
            list.setOnItemClickListener(this);
        } else {
            // todo : VITTLES 없으면 메세지 표시(재검색 또는 중지)
            Toast.makeText(this, "VITTLES가 검색되지 않았습니다.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        String ssid = arrList.get(position).getCarApSSID();
        String capa = arrList.get(position).getCapabilities();

        Toast.makeText(this, ssid + capa, Toast.LENGTH_LONG).show();

        // Perference 에 저장
        SharedPreferences.Editor edit = setting.edit();
        edit.putString("my_vittles_ap", ssid);
        edit.commit();

        int ret = wifi.Connecting(ssid, capa, "intintint");
        if (ret == -1) {
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

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void searchVittles() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiReceiver, filter);
    }

    public void onSearchBtnClicked(View v) {
        searchVittles();
        Toast.makeText(this, "Searching...", Toast.LENGTH_LONG).show();
    }

    public void onHomeBtnClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
