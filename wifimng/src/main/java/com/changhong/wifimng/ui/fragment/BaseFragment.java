package com.changhong.wifimng.ui.fragment;

import android.accounts.AuthenticatorException;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.activity.WifiHomeActivity;
import com.changhong.wifimng.uttils.CommUtil;
import com.changhong.wifimng.uttils.WifiUtils;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseFragment<T> extends Fragment {
    private Toast mToast;
    protected FragmentActivity mActivity;
    private SimpleDateFormat mSimDateFormat;
    protected OnFragmentLifeListener<T> onFragmentLifeListener;
    private ProgressDialog mProgressDialog;
    private WifiManager mWifiManager;

    private AlertDialog alertDilaog;

    private HashMap<String, GenericTask> mHashTask;
    /**
     * 从app传来的信息
     */
    protected Whole2LocalBeen mInfoFromApp;

    private PopupWindow mPopupWindow;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mHashTask = new HashMap<>();
        if (getArguments() != null) {
            mInfoFromApp = getArguments().getParcelable(KeyConfig.KEY_INFO_FROM_APP);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getClass().getSimpleName(), "====~ onResume");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.d(getClass().getSimpleName(), "====~ onHiddenChanged(" + hidden + ")");
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onPause() {
        Log.d(getClass().getSimpleName(), "====~ onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        CommUtil.closeIME(mActivity);
        Log.d(getClass().getSimpleName(), "====~ onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        CommUtil.closeIME(mActivity);
        Log.d(getClass().getSimpleName(), "====~ onDestroy");
        if (alertDilaog != null)
            alertDilaog.hide();
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
        stopAllTask();
        super.onDestroy();
    }

    protected void showToast(CharSequence charSequence) {
        if (mToast == null)
            mToast = Toast.makeText(mActivity, charSequence, Toast.LENGTH_SHORT);
        else
            mToast.setText(charSequence);
        mToast.show();
    }

    protected void showToast(int resid) {
        if (mToast == null)
            mToast = Toast.makeText(mActivity, resid, Toast.LENGTH_SHORT);
        else
            mToast.setText(resid);
        mToast.show();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
                return true;
            }
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    protected String getCurrentTime() {
        if (mSimDateFormat == null)
            mSimDateFormat = new SimpleDateFormat("HH:mm:ss");

        return mSimDateFormat.format(new Date()) + ':' + (System.currentTimeMillis() % 1000);
    }

    public void setOnFragmentLifeListener(OnFragmentLifeListener<T> onFragmentLifeListener) {
        this.onFragmentLifeListener = onFragmentLifeListener;
    }

    protected void showProgressDialog(CharSequence cs, boolean cancelable, DialogInterface.OnCancelListener listener) {
        if (mProgressDialog == null) {
//            mProgressDialog = ProgressDialog.show(this, null, cs, true, cancelable, listener);
            mProgressDialog = new ProgressDialog(mActivity);
        }
        mProgressDialog.setMessage(cs);
        mProgressDialog.setCancelable(cancelable);
        mProgressDialog.setOnCancelListener(listener);
        mProgressDialog.setCanceledOnTouchOutside(cancelable);
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();

    }

    protected void hideProgressDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    protected void showPopWinChoose(View view, String[] choices, final DialogInterface.OnClickListener listener) {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }

        ListView listview = new ListView(mActivity);
        listview.setBackgroundResource(R.drawable.shape_bg_round_rect_white);
        listview.setAdapter(new ArrayAdapter(mActivity, android.R.layout.simple_list_item_1, choices));

        TextView textview = (TextView) LayoutInflater.from(mActivity).inflate(android.R.layout.simple_list_item_1, null, false);
        Paint paint = textview.getPaint();
        int width = 0;
        Rect rect = new Rect();
        for (String choice : choices) {
            paint.getTextBounds(choice, 0, choice.length(), rect);
            width = Math.max(width, rect.width());
        }
        width += textview.getPaddingLeft() + textview.getPaddingRight();
        width += listview.getPaddingLeft() + listview.getPaddingRight();
        width += CommUtil.dip2px(mActivity, 20);

        mPopupWindow = new PopupWindow(listview, width, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.showAsDropDown(view);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onClick(null, position);
                mPopupWindow.dismiss();
            }
        });
    }

    protected void showDialogChoose(CharSequence[] choices, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(mActivity).setItems(choices, listener).create().show();
    }

    /**
     * @param et      密码显示的输入框
     * @param isShown 是否显示
     */
    protected void isShowPassword(EditText et, boolean isShown) {
        if (isShown)
            et.setTransformationMethod(PasswordTransformationMethod.getInstance());
        else
            et.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

        if (et.getText().length() > 0) {
            et.setSelection(et.getText().length());
        }
    }

    protected String getCookie() {
        if (!Preferences.getInstance(mActivity).contains(KeyConfig.KEY_COOKIE_SSID))
            return null;
        String ssid = Preferences.getInstance(mActivity).getString(KeyConfig.KEY_COOKIE_SSID);
//        if (TextUtils.isEmpty(ssid)) {
//            return null;
//        }
//        Map<String, String> cookies = new HashMap<>();
//        cookies.put("ssid", ssid);
//        return cookies;
        return ssid;
    }

    protected WifiManager getWifiManager() {
        if (mWifiManager == null)
            mWifiManager = (WifiManager) mActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return mWifiManager;
    }

    protected String getGateway() {
        return WifiUtils.turnInteger2Ip(getWifiManager().getDhcpInfo().gateway);
    }

    /**
     * 切换输入框的隐藏与显示
     *
     * @param et
     * @param isVisible
     */
    protected void togglePasswordState(EditText et, boolean isVisible) {
        if (isVisible) {
            et.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            et.setSingleLine();
            et.setMaxLines(1);
        } else
            et.setTransformationMethod(PasswordTransformationMethod.getInstance());

        if (et.getText().length() > 0) {
            et.setSelection(et.getText().length());
        }
    }

    protected void showAlert(CharSequence charSequence, CharSequence txBtn1, DialogInterface.OnClickListener listener1, CharSequence txBtn2, DialogInterface.OnClickListener listener2, boolean cancelEnable) {
        showAlert(charSequence, txBtn1, listener1, txBtn2, listener2, cancelEnable, null);
    }

    protected void showAlert(CharSequence charSequence,
                             CharSequence txBtn1, DialogInterface.OnClickListener listener1,
                             CharSequence txBtn2, DialogInterface.OnClickListener listener2,
                             boolean cancelEnable, DialogInterface.OnCancelListener cancelListener) {
//        if (alertDilaog != null) {
//            alertDilaog.setMessage(charSequence);
//            alertDilaog.setButton(AlertDialog.BUTTON_POSITIVE, txBtn1, listener1);
//            alertDilaog.setButton(AlertDialog.BUTTON_NEGATIVE, txBtn2, listener2);
//            alertDilaog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
//        } else
        alertDilaog = new AlertDialog.Builder(mActivity).setMessage(charSequence)
                .setPositiveButton(txBtn1, listener1).
                        setNegativeButton(txBtn2, listener2)
                .setCancelable(cancelEnable)
                .setCancelable(cancelListener != null)
                .setOnCancelListener(cancelListener).create();
        alertDilaog.setCanceledOnTouchOutside(cancelEnable);
        alertDilaog.show();
    }

    protected void showAlert(CharSequence charSequence, CharSequence txBtn,
                             DialogInterface.OnClickListener listener, boolean cancelEnable,
                             DialogInterface.OnCancelListener cancelListener) {
//        if (alertDilaog != null) {
//            alertDilaog.setMessage(charSequence);
//            alertDilaog.setButton(AlertDialog.BUTTON_POSITIVE, txBtn, listener);
//            alertDilaog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
//        } else
        alertDilaog = new AlertDialog.Builder(mActivity).setMessage(charSequence).setPositiveButton(txBtn, listener)
                .setCancelable(cancelEnable)
                .setOnCancelListener(cancelListener).create();
        alertDilaog.setCanceledOnTouchOutside(cancelEnable);
        alertDilaog.show();
    }

    protected void showAlert(CharSequence charSequence, CharSequence txBtn, DialogInterface.OnClickListener listener) {
        showAlert(charSequence, txBtn, listener, false, null);
    }

    protected void hideAlert() {
        if (alertDilaog != null)
            alertDilaog.dismiss();
    }

    protected void addTask(@NonNull GenericTask task) {
        if (mHashTask.containsKey(task.getClass())) {
            GenericTask obj = mHashTask.remove(task.getClass().getName());
            obj.cancel(true);
            mHashTask.put(task.getClass().getName(), task);
        }
    }

    protected void showTaskError(@NonNull GenericTask task, @NonNull int stringResId) {
        showToast(getTaskError(task, stringResId));
    }

    protected String getTaskError(@NonNull GenericTask task, @NonNull int stringResId) {
        Log.d(getClass().getSimpleName(), "====~ task message = " + task.getException());
        if (task.getException() == null
                || TextUtils.isEmpty(task.getException().getMessage()))
            return _getString(stringResId);

        String message = task.getException().getMessage();

        if (task.getException() instanceof JSONException ||
                task.getException() instanceof org.json.JSONException)
            return _getString(R.string.data_error);

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
                }
                return String.format(_getString(R.string.connection_timeout), message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (task.getException() instanceof java.net.UnknownHostException) {
            try {
                int start = message.indexOf('\"');
                int end = -1;
                if (start != -1) {
                    end = message.indexOf('\"', start + 1);
                }
                if (end != -1) {
                    message = message.substring(start + 1, end);
                    return String.format(_getString(R.string.known_host), message);
                } else
                    return _getString(R.string.timeout);

            } catch (Exception e) {
                e.printStackTrace();
                return _getString(R.string.timeout);
            }
        } else if (task.getException() instanceof java.net.SocketException) {
            return _getString(stringResId);
        } else if (task.getException() instanceof AuthenticatorException) {
            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(BaseWifiActivtiy.ACTION_UNAUTHORIZED));
        } else if (task.getException() instanceof java.net.ConnectException) {
            try {
                String ragex = ("(\\d+\\.\\d+\\.\\d+\\.\\d+)\\:(\\d+)");
                Pattern p = Pattern.compile(ragex);
                Matcher m = p.matcher(message);
                if (m.find()) {
                    message = m.group();
                }
                message = String.format(_getString(R.string.connection_refused), message);
                Intent intent = new Intent(BaseWifiActivtiy.ACTION_CONNECT_REFUSED);
                intent.putExtra(Intent.EXTRA_TEXT, message);
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);
                return message;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            return message;

        return message;
    }

    //java.lang.Exception: Unable to resolve host "test.chwliot.com": No address associated with hostname
    protected void stopAllTask() {
        if (!mHashTask.isEmpty()) {
            Iterator<Map.Entry<String, GenericTask>> iterator = mHashTask.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, GenericTask> item = iterator.next();
                mHashTask.remove(item.getKey()).cancel(true);
            }
        }
    }

    /**
     * @param clazz 线程终止
     */
    protected void stopTask(Class<? extends GenericTask> clazz) {
        if (!mHashTask.isEmpty()) {
            Iterator<Map.Entry<String, GenericTask>> iterator = mHashTask.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, GenericTask> item = iterator.next();
                if (item.getKey().equals(clazz.getName()))
                    mHashTask.remove(item.getKey()).cancel(true);
            }
        }
    }

    protected static final DialogInterface.OnClickListener DEFAULT_DIALOG_ONCLICK_LISTENER = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    /**
     * 提示获取到的设备类型和目标类型不匹配
     *
     * @param aimType
     * @param deviceType
     */
    protected void showAlertTypeNotSame(DeviceType aimType, String deviceType) {
        int start = 9;
        int end = 9 + deviceType.length();
        SpannableString spannableString = new SpannableString(String.format(_getString(R.string.notice_device_type_not_same), deviceType, aimType.getName()));
        spannableString.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = end + 6;
        end = start + aimType.getName().length();
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        showAlert(spannableString, _getString(R.string.confirm), null, _getString(R.string.wifi_setting), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), WifiHomeActivity.REQUEST_CODE_WIFI_SETTING);
                dialog.dismiss();
            }
        }, false);
    }

    /**
     * 提示获取到的设备MAC和本地mac不匹配
     *
     * @param aimMac
     * @param localMac
     */
    protected void showAlertMacNotSame(String aimMac, String localMac) {
        int start = 11;
        int end = 11 + localMac.length();
        SpannableString spannableString = new SpannableString(String.format(_getString(R.string.notice_device_type_not_same), localMac, aimMac));
        spannableString.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = end + 6;
        end = start + localMac.length();
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        showAlert(spannableString, _getString(R.string.confirm), null, _getString(R.string.wifi_setting), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), WifiHomeActivity.REQUEST_CODE_WIFI_SETTING);
                dialog.dismiss();
            }
        }, false);
    }

    protected String _getString(int resId) {
        try {
            return getString(resId);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.d(getClass().getSimpleName(), "====~context of fragment is Invalid in getString()");
            return mActivity.getString(resId);
        }
    }

    protected Resources _getResources() {
        try {
            return getResources();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.d(getClass().getSimpleName(), "====~context of fragment is Invalid in getResources()");
            return mActivity.getResources();
        }
    }

    protected void marth_parent(View view) {
        int height = mActivity.getWindow().getDecorView().getHeight();
        if (view.getHeight() < height)
            view.setMinimumHeight(height);
    }
}
