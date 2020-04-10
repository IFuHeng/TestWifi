package com.changhong.wifimng.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.preference.KeyConfig;

public class WifiInfoDataBaseHelper extends SQLiteOpenHelper implements KeyConfig {

    private final static String DB_NAME = "wifi_database.db";
    private final static int VERSION = 1;
    public final static String ROUTER_TABLE = "router_admin_password";

    public WifiInfoDataBaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ROUTER_TABLE + "(" +
                "_ID" + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_SSID + " TEXT NOT NULL," +
                KEY_ROUTER_PASSWORD + " TEXT NOT NULL," +
                KEY_DEVICE_MAC + " TEXT" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ROUTER_TABLE);
        onCreate(db);
    }

    public BaseBeen<String, String> getRouterInfoBySSID(String ssid) {
        Cursor cursor = getReadableDatabase().query(ROUTER_TABLE, new String[]{KEY_ROUTER_PASSWORD, KEY_DEVICE_MAC}, "select", new String[]{ssid}, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.move(i);//移动到指定记录
                String password = cursor.getString(cursor.getColumnIndex(KEY_ROUTER_PASSWORD));
                String mac = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MAC));
                return new BaseBeen<>(password, mac);
            }
        }
        return null;
    }

     public BaseBeen<String, String> saveRouterInfo(String ssid) {
        Cursor cursor = getReadableDatabase().query(ROUTER_TABLE, new String[]{KEY_ROUTER_PASSWORD, KEY_DEVICE_MAC}, "select", new String[]{ssid}, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.move(i);//移动到指定记录
                String password = cursor.getString(cursor.getColumnIndex(KEY_ROUTER_PASSWORD));
                String mac = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MAC));
                return new BaseBeen<>(password, mac);
            }
        }
        return null;
    }


}
