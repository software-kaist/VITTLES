package kaist.game.battlecar.util;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import kaist.game.battlecar.adapter.CarApList;

/**
 * Created by SUNgHOOn on 2015-12-13.
 */
public class VittlesConnector {
    private final static String TAG = VittlesConnector.class.getSimpleName();
    private Context ctx;
    private WifiManager wm;
    private List apList;
    private ArrayList<CarApList> arrList = new ArrayList<CarApList>();
    private ScanResult scanResult;
    private String vittlesPrefix;

    public VittlesConnector(Context ctx, String prefix) {
        this.ctx = ctx;
        this.vittlesPrefix = prefix;
        init();
    }

    public void init() {
        wm = (WifiManager) ctx.getSystemService(ctx.WIFI_SERVICE); // WifiManager 초기화
        wm.startScan(); // 검색
    }

    public ArrayList<CarApList> searchWifi() {
        apList = wm.getScanResults();
        if (wm.getScanResults() != null) {
            arrList.clear(); // 이전 리스트 삭제
            int size = apList.size();
            for (int i = 0; i < size; i++) {
                scanResult = (ScanResult) apList.get(i);
                Log.d(TAG, "onActivityResult " + scanResult);

                if (scanResult.SSID.toUpperCase().contains(vittlesPrefix)) {
                    CarApList item = new CarApList(scanResult.SSID, scanResult.capabilities);
                    arrList.add(item);
                }
            }
        }

        return arrList;
    }

    public int Connecting(String ssid, String capa, String password) {
        // wifi에 접속
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
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wfc.wepKeys[0] = password;
            wfc.wepTxKeyIndex = 0;
        } else if (capa.toUpperCase().contains("WPA")) {
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wfc.preSharedKey = "\"".concat(password).concat("\"");
        } else {
            return -1;
        }

        int networkId = wm.addNetwork(wfc);
        if (networkId != -1) {
            wm.enableNetwork(networkId, true);
        }

        return networkId;
    }
}
