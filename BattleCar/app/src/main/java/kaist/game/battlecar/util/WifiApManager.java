package kaist.game.battlecar.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.Method;

/**
 * Created by songhochan on 2015. 12. 28..
 */

public class WifiApManager {

    //check whether wifi hotspot on or off
    public static boolean isApOn(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        }
        catch (Throwable ignored) {}
        return false;
    }

    // toggle wifi hotspot on or off
    public static boolean configApState(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiConfiguration wificonfiguration = null;
        try {
            // if WiFi is on, turn it off
            if(isApOn(context)) {
                wifimanager.setWifiEnabled(false);
            }
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, wificonfiguration, !isApOn(context));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean setWifiApEnabled(Context context, boolean value) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        boolean result;

        try {
            if(value) {
                wifiManager.setWifiEnabled(false);
            }
            Method enableWifi = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            String ssid = "hochan97"; // tag on the RC car
            String pass = "hochan180907";
            WifiConfiguration myConfig = new WifiConfiguration();
            myConfig.SSID = ssid;
            myConfig.preSharedKey = pass;
            myConfig.status = WifiConfiguration.Status.ENABLED;
            myConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            myConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            myConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            myConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            myConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            myConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            myConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            result = (Boolean) enableWifi.invoke(wifiManager, myConfig, value);
            if(!value) {
                wifiManager.setWifiEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

}
