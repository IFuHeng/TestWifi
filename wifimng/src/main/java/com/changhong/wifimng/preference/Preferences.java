package com.changhong.wifimng.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.changhong.wifimng.been.BaseBeen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Preferences implements KeyConfig {

    private SharedPreferences sharedPreferences;
    private static Preferences sIntance;

    private Preferences(Context context) {
        sIntance = this;
        sharedPreferences = context.getSharedPreferences("Cache", Context.MODE_PRIVATE);
    }

    public static synchronized final Preferences getInstance(Context context) {
        if (sIntance == null)
            sIntance = new Preferences(context);

        return sIntance;
    }

    public void saveString(String key, String value) {
        if (TextUtils.isEmpty(key))
            return;

        if (value == null && sharedPreferences.contains(key))
            sharedPreferences.edit().remove(key).commit();
        else
            sharedPreferences.edit().putString(key, value).commit();
    }

    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defValue) {
        if (TextUtils.isEmpty(key))
            return defValue;
        return sharedPreferences.getString(key, defValue);
    }

    public void saveInt(String key, int value) {
        if (TextUtils.isEmpty(key))
            return;

        if (value == 0 && sharedPreferences.contains(key))
            sharedPreferences.edit().remove(key).commit();
        else
            sharedPreferences.edit().putInt(key, value).commit();
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defValue) {
        if (TextUtils.isEmpty(key))
            return defValue;
        return sharedPreferences.getInt(key, defValue);
    }

    public void saveBoolean(String key, boolean value) {
        if (TextUtils.isEmpty(key))
            return;

        if (value == false && sharedPreferences.contains(key))
            sharedPreferences.edit().remove(key).commit();
        else
            sharedPreferences.edit().putBoolean(key, value).commit();
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defValue) {
        if (TextUtils.isEmpty(key))
            return defValue;
        return sharedPreferences.getBoolean(key, defValue);
    }

    public void remove(String key) {
        if (sharedPreferences.contains(key)) {
            sharedPreferences.edit().remove(key).commit();
        }
    }

    public void clear() {
        sharedPreferences.edit().clear().commit();
    }

}
