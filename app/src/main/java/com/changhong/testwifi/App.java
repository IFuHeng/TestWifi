package com.changhong.testwifi;

import android.app.Application;
import android.content.pm.PackageInfo;

public class App extends Application {

    public int sVersionCode;
    public String sVersionName;

    /**
     * 确保ssid没有前后的双引号（"）
     */
    private String sCurrentWifi;
    public boolean sIs5g;

    public static App getInstance() {
        return sInstance;
    }

    private static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        try {
            PackageInfo pi = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            // versionName = pi.versionName;
            sVersionCode = pi.versionCode;
            sVersionName = pi.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置ssid
     *
     * @param wifi 可以包含前后的双引号，会自动清理掉
     */
    public void setCurrentWifi(String wifi, boolean is5g) {
        if (wifi == null) {
            sCurrentWifi = null;
            sIs5g = false;
            return;
        }

        if (wifi.charAt(0) == '\"' && wifi.charAt(wifi.length() - 1) == '\"')
            sCurrentWifi = wifi.substring(1, wifi.length() - 1);
        else
            sCurrentWifi = wifi;

        sIs5g = is5g;
    }

    /**
     * 比较ssid
     *
     * @param wifi
     */
    public boolean compareSSID(String wifi) {
        if (wifi == null || sCurrentWifi == null)
            return false;
        return wifi.compareTo(sCurrentWifi) == 0;
    }

    public String getCurrentWifi() {
        return sCurrentWifi;
    }


}
