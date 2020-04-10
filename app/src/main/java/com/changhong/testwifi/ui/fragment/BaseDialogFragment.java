package com.changhong.testwifi.ui.fragment;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;

import com.changhong.testwifi.R;

public class BaseDialogFragment<T> extends DialogFragment {

    protected Context mContext;
    protected OnFragmentLifeListener<T> mFragmentListener;

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
}
