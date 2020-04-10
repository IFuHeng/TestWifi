package com.changhong.testwifi.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import java.util.List;

public class WifiUtils {

    public static String clearSSID(String ssid) {
        if (TextUtils.isEmpty(ssid) || ssid.length() < 2)
            return ssid;
        if (ssid.charAt(0) == '\"' && ssid.charAt(ssid.length() - 1) == '\"')
            return ssid.substring(1, ssid.length() - 1);

        return ssid;
    }

    public static int turnArr2Ip(String[] IP) {
        int ip = 0;
        for (int i = 0; i < IP.length; i++) {
            int temp = Integer.parseInt(IP[i]);
            ip = (temp << (i * 8)) | ip;
        }
        return ip;
    }

    public static String turnInteger2Ip(int ip) {
        int[] temp = new int[4];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = ip >>> (8 * i);
            temp[i] &= 0xff;
        }
        return String.format("%d.%d.%d.%d", temp[0], temp[1], temp[2], temp[3]);
    }

    /**
     * 获取SSID
     *
     * @return WIFI 的SSID
     */
    public static String getWifiSSID(Context context, WifiManager wifiManager) {
        String ssid = "unknown id";

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {

            assert wifiManager != null;
            WifiInfo info = wifiManager.getConnectionInfo();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                ssid = info.getSSID();
            } else {
                ssid = info.getSSID().replace("\"", "");
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {

            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert connManager != null;
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo.isConnected()) {
                if (networkInfo.getExtraInfo() != null) {
                    ssid = networkInfo.getExtraInfo().replace("\"", "");
                }
            }
        }

        if (ssid == null || ssid.contains("unknown")) {
            int networkId = wifiManager.getConnectionInfo().getNetworkId();
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration wifiConfiguration : configuredNetworks) {
                if (wifiConfiguration.networkId == networkId) {
                    ssid = clearSSID(wifiConfiguration.SSID);
                    break;
                }
            }
        }

        return ssid;
    }
}
