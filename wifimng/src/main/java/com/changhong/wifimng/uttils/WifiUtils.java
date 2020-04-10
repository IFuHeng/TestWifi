package com.changhong.wifimng.uttils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import java.util.List;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class WifiUtils {

    public static final String PATTERN_IPV4 = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$";

    private static final String TAG = "WifiMgr";

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

    public static int turnIpv4Str2Ip(String IP) {
        int ip = 0;
        String[] tempArr = IP.split(".");
        for (int i = 0; i < tempArr.length; i++) {
            int temp = Integer.parseInt(tempArr[i]);
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
            if (configuredNetworks == null)
                return ssid;
            for (WifiConfiguration wifiConfiguration : configuredNetworks) {
                if (wifiConfiguration.networkId == networkId) {
                    ssid = clearSSID(wifiConfiguration.SSID);
                    break;
                }
            }
        }

        return ssid;
    }


    public static WifiConfiguration initWifiConfiguration(String ssid, String password, PskType pskType) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = convertToQuotedString(ssid);

        if (pskType == null || pskType == PskType.UNKNOWN || password == null) {
            wifiConfig.allowedAuthAlgorithms.clear();
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (pskType == PskType.WPA) {
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        } else if (pskType == PskType.WPA2) {
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        }

        wifiConfig.priority = Integer.MAX_VALUE;
        wifiConfig.status = WifiConfiguration.Status.ENABLED;
        if (password != null)
            wifiConfig.preSharedKey = convertToQuotedString(password);
        // this.mConfig.SSID = """+ssid+""";
        // 　　this.mConfig.preSharedKey = """+password+""";
        // int netId = this.mWifiManager.addNetwork(this.mConfig);
        // this.mWifiManager.enableNetwork(netId, false);
        return wifiConfig;
    }

    public static void connectNetwork(WifiManager wifiManager, String ssid, String password, PskType type) {

        int netId = isExsits(wifiManager, ssid);
        if (netId > 0) {
            Log.d(TAG, "exists  ssid = " + ssid);
//            WifiConfiguration wifiConfig = initWifiConfiguration(ssid, password, type);
//            Log.d(TAG, "update  config  ssid = " + ssid + "  pwd " + password);
//            wifiManager.updateNetwork(wifiConfig);
//            wifiManager.disconnect();
//            boolean enableNet = wifiManager.enableNetwork(netId, true);
//            // this.mWifiManager.reconnect();
////            Toast.makeText(context, ssid + " enable net " + enableNet, Toast.LENGTH_SHORT).show();
//            if (enableNet) {
//                return;
//            }
            wifiManager.removeNetwork(netId);
        }
        Log.d(TAG, "exec initWifiConfiguration and  connection ssid = " + ssid + " pwd " + password);

        WifiConfiguration wifiConfig = initWifiConfiguration(ssid, password, type);
        netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.saveConfiguration();
        boolean isConn = wifiManager.reconnect();
        if (isConn) {
//            Toast.makeText(context, ssid + " connect succeed.", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "ssid  " + ssid + " isConn " + isConn);
    }

    public static boolean remove(WifiManager mWifiManager, String ssid) {
        int netId = isExsits(mWifiManager, ssid);
        if (netId > 0) {
            return mWifiManager.removeNetwork(netId);
        }
        // this.mWifiManager.disconnect();
        return false;
    }

    private static int isExsits(WifiManager mWifiManager, String ssid) {
        List<WifiConfiguration> existingConfigs = mWifiManager
                .getConfiguredNetworks();
        if (null != existingConfigs) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                String password = existingConfig.preSharedKey;
                if (existingConfig.SSID.equals(convertToQuotedString(ssid))) {
                    return existingConfig.networkId;
                }
            }
        }

        return -1;
    }

    static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    /**
     * @param ip 正确的ipv4地址
     * @return 判断ipv4地址末尾是否为0 或者255
     */
    public static boolean isBroadcastOrNetworkAddress(String ip) {
        String temp = ip.substring(ip.lastIndexOf('.') + 1);
        int value = Integer.parseInt(temp);
        return value == 0 || value == 255;
    }
}
