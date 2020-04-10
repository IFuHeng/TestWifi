package com.changhong.wifimng.ui.view;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

/**
 * 显示或隐藏
 */
public class HideOrShowWatcher implements TextWatcher {

    View aim;

    public HideOrShowWatcher(View aim) {
        this.aim = aim;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > 0)
            aim.setVisibility(View.VISIBLE);
        else
            aim.setVisibility(View.GONE);

    }

}
