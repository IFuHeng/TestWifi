package com.changhong.wifimng.database;

import android.content.Context;

import com.changhong.wifimng.preference.KeyConfig;

public class WifiUseDataManager implements KeyConfig {

    private WifiInfoDataBaseHelper mDbHelper;

    public WifiUseDataManager(Context context) {
        this.mDbHelper = new WifiInfoDataBaseHelper(context);
    }

    public void saveRouterPassword(String ssid,String password,String mac){

    }

}
