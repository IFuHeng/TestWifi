package com.changhong.wifimng.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.view.DefaultDialog;
import com.changhong.wifimng.ui.view.DefaultProgressDialog;
import com.changhong.wifimng.uttils.CommUtil;
import com.changhong.wifimng.uttils.WifiUtils;

import org.json.JSONException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018/1/15.
 */

public class BaseActivtiy extends AppCompatActivity {
    private static final int REQUEST_CODE_WIFI = 9527;
    private DefaultDialog alertDilaog;
    private Toast mToast;
    protected DefaultProgressDialog mProgressDialog;

    protected BaseFragment mCurFragment;

    private WifiManager mWifiManager;

    private HashMap<String, GenericTask> mHashTask;

    /**
     * 从app传来的信息
     */
    protected Whole2LocalBeen mInfoFromApp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestContactPermission();
        mHashTask = new HashMap<>();
        CommUtil.transparencyBar(this);
        if (getIntent() != null) {
            mInfoFromApp = getIntent().getParcelableExtra(KeyConfig.KEY_INFO_FROM_APP);
        }
        Log.d(getClass().getSimpleName(), "====~Language = " + getResources().getConfiguration().locale.getLanguage());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(getClass().getSimpleName(), "====~ onResume");
    }

    @Override
    protected void onPause() {
        Log.d(getClass().getSimpleName(), "====~ onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopAllTask();
        super.onDestroy();
    }

    protected void showProgressDialog(CharSequence cs, boolean cancelable, DialogInterface.OnCancelListener listener) {
        if (mProgressDialog == null) {
//            mProgressDialog = ProgressDialog.show(this, null, cs, true, cancelable, listener);
            mProgressDialog = new DefaultProgressDialog(this, cs);
        } else
            mProgressDialog.setMessage(cs);

        mProgressDialog.setCancelable(cancelable);
        mProgressDialog.setCanceledOnTouchOutside(cancelable);
        mProgressDialog.setOnCancelListener(listener);

        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    protected void hideProgressDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    protected void showToast(CharSequence cs) {
        if (mToast == null)
            mToast = Toast.makeText(this, cs, Toast.LENGTH_SHORT);
        else
            mToast.setText(cs);
        mToast.show();
    }

    protected void showTaskError(@NonNull GenericTask task, @NonNull int stringResId) {
        showToast(getTaskError(task, stringResId));
    }

    protected String getTaskError(@NonNull GenericTask task, @NonNull int stringResId) {
        Log.d(getClass().getSimpleName(), "====~ task message = " + task.getException());
        if (task.getException() == null
                || TextUtils.isEmpty(task.getException().getMessage()))
            return getString(stringResId);

        String message = task.getException().getMessage();

        if (task.getException() instanceof JSONException ||
                task.getException() instanceof org.json.JSONException)
            return getString(R.string.data_error);

        if (!"zh".equals(getResources().getConfiguration().locale.getLanguage()))//非中文直接反馈英文
            return message;

        //以下转义反馈
        if (task.getException() instanceof java.net.SocketTimeoutException) {
            try {
                String ragex = ("(\\d+\\.\\d+\\.\\d+\\.\\d+)");
                Pattern p = Pattern.compile(ragex);
                Matcher m = p.matcher(message);
                if (m.find()) {
                    message = m.group();
                    return String.format(getString(R.string.connection_timeout), message);
                } else
                    return getString(R.string.timeout);

            } catch (Exception e) {
                e.printStackTrace();
                return getString(R.string.timeout);
            }
        } else if (task.getException() instanceof java.net.UnknownHostException) {
            try {
                int start = message.indexOf('\"');
                int end = -1;
                if (start != -1) {
                    end = message.indexOf('\"', start + 1);
                }
                if (end != -1)
                    message = message.substring(start + 1, end);
                return String.format(getString(R.string.known_host), message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (task.getException() instanceof java.net.SocketException) {
            return getString(stringResId);
        } else if (task.getException() instanceof java.net.ConnectException) {
            try {
                String ragex = ("(\\d+\\.\\d+\\.\\d+\\.\\d+)\\:(\\d+)");
                Pattern p = Pattern.compile(ragex);
                Matcher m = p.matcher(message);
                if (m.find()) {
                    message = m.group();
                }
                message = String.format(getString(R.string.connection_refused), message);
                Intent intent = new Intent(BaseWifiActivtiy.ACTION_CONNECT_REFUSED);
                intent.putExtra(Intent.EXTRA_TEXT, message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                return message;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            return message;

        return message;
    }

    protected void showToast(int resId) {
        if (mToast == null)
            mToast = Toast.makeText(this, resId, Toast.LENGTH_SHORT);
        else
            mToast.setText(resId);
        mToast.show();
    }


    protected void showAlert(CharSequence charSequence, CharSequence txBtn, DialogInterface.OnClickListener listener, boolean cancelEnable) {
        if (alertDilaog != null) {
            alertDilaog.setMessage(charSequence);
            alertDilaog.setButton(AlertDialog.BUTTON_POSITIVE, txBtn, listener);
        } else
//            alertDilaog = new AlertDialog.Builder(this).setMessage(charSequence).setPositiveButton(txBtn, listener).setCancelable(cancelEnable).create();
            alertDilaog = new DefaultDialog(this, getString(R.string.warning), android.R.drawable.ic_dialog_alert, charSequence, txBtn, listener, null, null);
        alertDilaog.setCancelable(cancelEnable);
//        if (alertDilaog.isShowing())
//            alertDilaog.dismiss();
        if (!alertDilaog.isShowing())
            alertDilaog.show();
    }


    protected boolean isDialogShown() {

        if (mProgressDialog != null && mProgressDialog.isShowing())
            return true;

        if (alertDilaog != null && alertDilaog.isShowing())
            return true;

        return false;
    }

    protected String getCookie() {
        return Preferences.getInstance(getApplicationContext()).getString(KeyConfig.KEY_COOKIE_SSID);
//        Map<String, String> cookies = new HashMap<>();
//        cookies.put("ssid", Preferences.getInstance(getApplicationContext()).getString(KeyConfig.KEY_COOKIE_SSID));
//        return cookies;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mCurFragment != null) {
            if (mCurFragment.onKeyDown(keyCode, event))
                return true;
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                backFragment();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mCurFragment != null && mCurFragment.onKeyUp(keyCode, event))
            return true;

        return super.onKeyUp(keyCode, event);
    }

    protected void startFragment(BaseFragment fragment) {
        if (fragment == null)
            return;
        if (mCurFragment != null && mCurFragment.getClass() == fragment.getClass())
            return;
        getSupportFragmentManager().beginTransaction().add(R.id.layout_container, fragment).addToBackStack(null).commit();
        if (mCurFragment != null && !mCurFragment.isHidden()) {
            getSupportFragmentManager().beginTransaction().hide(mCurFragment).commit();
        }
        mCurFragment = fragment;
    }

    protected void backFragment() {
        if (mCurFragment != null) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                List<Fragment> list = getSupportFragmentManager().getFragments();
                mCurFragment = (BaseFragment) list.get(list.size() - 2);
                if (mCurFragment != null && mCurFragment.isHidden())
                    getSupportFragmentManager().beginTransaction().show(mCurFragment).commit();
                getSupportFragmentManager().popBackStack();
            } else
                finish();
        } else finish();
    }

    protected WifiManager getWifiManager() {
        if (mWifiManager == null)
            mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return mWifiManager;
    }

    protected String getGateway() {
        return WifiUtils.turnInteger2Ip(getWifiManager().getDhcpInfo().gateway);
    }

    protected void addTask(@NonNull GenericTask task) {
        if (mHashTask.containsKey(task.getClass())) {
            GenericTask obj = mHashTask.remove(task.getClass().getName());
            obj.cancel(true);
            mHashTask.put(task.getClass().getName(), task);
        }
    }

    protected void stopAllTask() {
        if (!mHashTask.isEmpty()) {
            Iterator<Map.Entry<String, GenericTask>> iterator = mHashTask.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, GenericTask> item = iterator.next();
                mHashTask.remove(item.getKey()).cancel(true);
            }
        }
    }

    private void requestContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permission_wifi_state = checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE);
            int permission_network_state = checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE);
            int permission_coarse_location = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            Log.d(getClass().getSimpleName(), "====~ permission_wifi_state = " + permission_network_state + " , permission_network_state =" + permission_network_state);
            if (permission_wifi_state != PackageManager.PERMISSION_GRANTED
                    || permission_network_state != PackageManager.PERMISSION_GRANTED
                    || permission_coarse_location != PackageManager.PERMISSION_GRANTED)
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_COARSE_LOCATION}
                        , REQUEST_CODE_WIFI);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_WIFI:
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            finish();
                        }
                    }
                }
                break;
        }

        Log.d(getClass().getSimpleName(), "====~ onRequestPermissionsResult : requestCode = " + requestCode
                + " , permissions = " + Arrays.toString(permissions)
                + " , grantResult = " + Arrays.toString(grantResults));
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
