package com.changhong.wifimng.ui.view;

import android.text.Editable;
import android.text.TextWatcher;

import com.changhong.wifimng.uttils.NumberUtils;

import java.util.regex.Pattern;

public class DefaultIntegerWatcher implements TextWatcher {
    private int mCount;
    private int mStart;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mStart = start;
        mCount = count;
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() == 0) {
            s.insert(0, "0");
            return;
        }

        if (mCount > 0) {
            if (s.length() > 1 && s.charAt(0) == '0') {
                s.delete(0, 1);
                return;
            }

            if (!isInteger(s)) {
                s.delete(mStart, mStart + mCount);
            }
        }

    }

    private boolean isInteger(CharSequence s) {
        return Pattern.matches(NumberUtils.REGEX_INTEGER, s);
    }
}
