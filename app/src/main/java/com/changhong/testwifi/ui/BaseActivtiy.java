package com.changhong.testwifi.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Administrator on 2018/1/15.
 */

public class BaseActivtiy extends AppCompatActivity {
    private AlertDialog alertDilaog;
    private Toast mToast;

    public static final String ACTION_EXIT_APP = "com.changhong.homelink.action.ACTION_APP_EXIT";
    public static final String ACTION_RECONNECTED_THE_WIFI = "com.changhong.homelink.action.ACTION_RECONNECTED_THE_WIFI";

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_EXIT_APP);
        filter.addAction(ACTION_RECONNECTED_THE_WIFI);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    protected void showProgressDialog(CharSequence cs, boolean cancelable, DialogInterface.OnCancelListener listener) {
        if (mProgressDialog == null) {
//            mProgressDialog = ProgressDialog.show(this, null, cs, true, cancelable, listener);
            mProgressDialog = new ProgressDialog(this);
        }
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();

        mProgressDialog.setMessage(cs);
        mProgressDialog.setCancelable(cancelable);
        mProgressDialog.setOnCancelListener(listener);
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


    protected void showAlert(CharSequence charSequence, CharSequence txBtn, DialogInterface.OnClickListener listener) {
        if (alertDilaog != null) {
            alertDilaog.setMessage(charSequence);
            alertDilaog.setButton(AlertDialog.BUTTON_POSITIVE, txBtn, listener);
        } else
            alertDilaog = new AlertDialog.Builder(this).setMessage(charSequence).setPositiveButton(txBtn, listener).setCancelable(false).create();
        alertDilaog.show();
    }


    protected boolean isDialogShown() {

        if (mProgressDialog != null && mProgressDialog.isShowing())
            return true;

        if (alertDilaog != null && alertDilaog.isShowing())
            return true;

        return false;
    }

}
