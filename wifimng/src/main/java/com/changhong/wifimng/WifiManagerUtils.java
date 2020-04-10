package com.changhong.wifimng;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.been.sys.SettingResponseAllBeen;
import com.changhong.wifimng.function.AddChildDeviceEntrance;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.ui.activity.BaseActivtiy;
import com.changhong.wifimng.ui.activity.WifiHomeActivity;

import java.util.Observable;
import java.util.Observer;

public class WifiManagerUtils {
    /**
     * @param context
     * @param clazz      com.changhong.wifimng.ui.activity.BaseActivtiy 的子类
     * @param user       用户账号，登录返回字段里的username
     * @param token      令牌
     * @param userUuid   用户uuid
     * @param deviceUuid 设备唯一标识
     * @param mac        设备mac
     * @param deviceType 设备类型
     * @param name       设备名称
     */
    public static void startActivity(@NonNull Context context, @NonNull Class<? extends BaseActivtiy> clazz,
                                     @NonNull String user, @NonNull String token, @NonNull String userUuid,
                                     String deviceUuid, String mac, String deviceType, String name) {
        if (deviceType == null) {
            AlertDialog dialog = new AlertDialog.Builder(context).setMessage(R.string.notice_data_error).setPositiveButton(R.string.confirm, null)
                    .create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            return;
        }
        DeviceType type = DeviceType.getDeviceTypeByCloudCode(deviceType);
        if (type == null) {
            AlertDialog dialog = new AlertDialog.Builder(context).setMessage(R.string.notice_device_not_supported).setPositiveButton(R.string.confirm, null).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            return;
        }
        Intent intent = new Intent(context, clazz);
        Whole2LocalBeen whole2LocalBeen = new Whole2LocalBeen();
        whole2LocalBeen.setUser(user);
        whole2LocalBeen.setToken(token);
        whole2LocalBeen.setUserUuid(userUuid);
        whole2LocalBeen.setDevcieUuid(deviceUuid);
        whole2LocalBeen.setMac(mac);
        whole2LocalBeen.setDeviceType(type.getName());
        whole2LocalBeen.setDeviceName(name);
        intent.putExtra(KeyConfig.KEY_INFO_FROM_APP, whole2LocalBeen);
        context.startActivity(intent);
    }

    /**
     * @param activity
     * @param clazz
     * @param user
     * @param token
     * @param userUuid
     * @param deviceUuid
     * @param mac
     * @param name        设备名称
     * @param deviceType
     * @param requestCode
     */
    public static void startActivityForResult(@NonNull Activity activity, @NonNull Class<? extends BaseActivtiy> clazz,
                                              @NonNull String user, @NonNull String token, @NonNull String userUuid,
                                              String deviceUuid, String mac, String deviceType, String name, int requestCode) {
        if (deviceType == null) {
            AlertDialog dialog = new AlertDialog.Builder(activity).setMessage(R.string.notice_data_error).setPositiveButton(R.string.confirm, null).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            return;
        }
        DeviceType type = DeviceType.getDeviceTypeByCloudCode(deviceType);
        if (type == null) {
            AlertDialog dialog = new AlertDialog.Builder(activity).setMessage(R.string.notice_device_not_supported).setPositiveButton(R.string.confirm, null).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            return;
        }
        Intent intent = new Intent(activity, clazz);
        Whole2LocalBeen whole2LocalBeen = new Whole2LocalBeen();
        whole2LocalBeen.setUser(user);
        whole2LocalBeen.setToken(token);
        whole2LocalBeen.setUserUuid(userUuid);
        whole2LocalBeen.setDevcieUuid(deviceUuid);
        whole2LocalBeen.setMac(mac);
        whole2LocalBeen.setDeviceType(type.getName());
        whole2LocalBeen.setDeviceName(name);
        intent.putExtra(KeyConfig.KEY_INFO_FROM_APP, whole2LocalBeen);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * @param activity
     * @param mac
     * @param deviceType
     * @param requestCode
     */
    public static void startAddChildDeviceResult(@NonNull final FragmentActivity activity, final String name, final String mac, String deviceType, final int requestCode) {
        if (deviceType == null) {
            AlertDialog dialog = new AlertDialog.Builder(activity).setMessage(R.string.notice_data_error).setPositiveButton(R.string.confirm, null)
                    .create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            return;
        }
        final DeviceType type = DeviceType.getDeviceTypeByCloudCode(deviceType);
        if (type == null) {
            AlertDialog dialog = new AlertDialog.Builder(activity).setMessage(R.string.notice_device_not_supported).setPositiveButton(R.string.confirm, null).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            return;
        }

        new AddChildDeviceEntrance(activity, type, mac, name, new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                Intent intent = new Intent(activity, WifiHomeActivity.class);
                Whole2LocalBeen whole2LocalBeen = new Whole2LocalBeen();
                whole2LocalBeen.setMac(mac);
                whole2LocalBeen.setDevcieUuid(name);
                whole2LocalBeen.setDeviceType(type.getName());
                if (arg != null && arg instanceof SettingResponseAllBeen) {

                }
                intent.putExtra(KeyConfig.KEY_INFO_FROM_APP, whole2LocalBeen);
                intent.putExtra(KeyConfig.KEY_IS_ADD_CHILD, true);
                activity.startActivityForResult(intent, requestCode);
            }
        }).start();

    }
}
