package com.changhong.wifimng.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.changhong.wifimng.R;

public class BaseDialogFragment<T> extends DialogFragment {

    protected Context mContext;
    protected OnFragmentLifeListener<T> mFragmentListener;
    private Toast mToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.mydialog);
        this.mContext = getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        getDialog().getWindow().setGravity(Gravity.CENTER);
        getDialog().getWindow().setLayout(dm.widthPixels, -2);
    }

    public void setFragmentListener(OnFragmentLifeListener<T> fragmentLiseter) {
        this.mFragmentListener = fragmentLiseter;
    }


    protected void destroyMyself() {
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    protected void showToast(CharSequence charSequence) {
        if (mToast == null)
            mToast = Toast.makeText(mContext, charSequence, Toast.LENGTH_SHORT);
        else
            mToast.setText(charSequence);
        mToast.show();
    }

    protected void showToast(int resid) {
        if (mToast == null)
            mToast = Toast.makeText(mContext, resid, Toast.LENGTH_SHORT);
        else
            mToast.setText(resid);
        mToast.show();
    }
}
