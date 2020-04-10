package com.changhong.wifimng.ui.view;

import android.text.Editable;
import android.text.TextWatcher;

public class DefaultIPV4Watcher implements TextWatcher {
    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.'};

    private int mCount;
    private int mStart;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mCount = count;
        mStart = start;
    }

    @Override
    public void afterTextChanged(Editable s) {
        checkAndFormat(s);
    }

    private int getCharCount(CharSequence s, char c) {
        int result = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c)
                result++;
        }
        return result;
    }

    /**
     * 判断是否是输入中的ipv4
     */
    private void checkAndFormat(Editable s) {
        if (s.length() == 0) {
            return;
        }

        if (!isRuleCharactor(s)) {
            s.delete(mStart, mStart + mCount);
            return;
        }

//        if (s.charAt(0) == '.') {
//            s.insert(0, "0");
//            return;
//        }

        int endsymbolNum = getCharCount(s, '.');
        if (endsymbolNum > 3) {
            dealEndsymbolGt3(s);
        } else {
            dealEndsymbolLt3(s);
        }


    }

    /**
     * 处理'.'数量正常的情况
     */
    private void dealEndsymbolLt3(Editable s) {
        String str = s.toString();
        int start = 0;
        int end;
        do {
            end = str.indexOf('.', start + 1);
            if (end == -1)
                end = s.length();
//            Log.d(getClass().getSimpleName(), "====~ dealEndsymbolLt3 :" + s.subSequence(start, end));

//            if (end - start == 0) {
//                s.insert(start, "0");
//                return;
//            }

            if (end - start > 1 && s.charAt(start) == '0') {
                s.delete(start, start + 1);
                return;
            }

            if (end - start > 2 && Integer.parseInt(s.subSequence(start, end).toString()) > 255) {
                s.delete(end - 1, end);
                return;
            }

            start = end + 1;
        } while (end != -1 && end != s.length());

    }

    /**
     * 处理'.'大于3的情况
     */
    private void dealEndsymbolGt3(Editable s) {
        for (int i = 0, num = 0; i < s.length(); i++) {
            if (s.charAt(i) == '.')
                ++num;

            if (num > 3) {
//                Log.d(getClass().getSimpleName(), "====~ dealEndsymbolGt3 :" + s.subSequence(i, s.length()));
                s.delete(i, s.length());
                break;
            }

        }

    }

    /**
     * 判断超出的字符
     */
    private boolean isRuleCharactor(CharSequence s) {
        for (int i = 0; i < s.length(); ++i) {

            boolean isIn = false;
            for (int j = 0; j < HEX_CHARS.length && !isIn; j++) {
                if (s.charAt(i) == HEX_CHARS[j]) {
                    isIn = true;
                }
            }

            if (!isIn) {
                return false;
            }
        }
        return true;
    }
}
