package com.changhong.wifimng.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.changhong.wifimng.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class InputDialog extends BaseDialogFragment<String> implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, TextWatcher {

    public static final String EXTRA_PASSWORD = "password";
    public static final String EXTRA_VISIBLE_PWD = "password_visible";
    public static final String KEY_CANCEL_ABLE = "dialog cancel enable";
    public static final String KEY_SUPPORT_EN_ONLY = "support en only";
    public static final String KEY_HINT = "hint";
    private EditText mEditText;
    private CheckBox mCheckBox;
    private View mBtnCancel;
    private View mBtnConfirm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null
                && getArguments().containsKey(KEY_CANCEL_ABLE))
            setCancelable(getArguments().getBoolean(KEY_CANCEL_ABLE));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_input, null, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        TextView textView = view.findViewById(android.R.id.text1);
        mBtnCancel = view.findViewById(android.R.id.button1);
        mBtnConfirm = view.findViewById(android.R.id.button2);
        EditText editText = view.findViewById(android.R.id.edit);
        if (getArguments() != null
                && getArguments().getBoolean(KEY_SUPPORT_EN_ONLY)) {
            editText.addTextChangedListener(this);
        }
        mCheckBox = view.findViewById(android.R.id.checkbox);

        String title = getArguments().getString(Intent.EXTRA_TEXT);
        if (TextUtils.isEmpty(title))
            textView.setVisibility(View.GONE);
        else
            textView.setText(title);

        mCheckBox.setOnCheckedChangeListener(this);

        mEditText = editText;
        String hint = getArguments().getString(KEY_HINT);
        if (!TextUtils.isEmpty(hint))
            editText.setHint(hint);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0)
                    mBtnConfirm.setEnabled(false);
                else
                    mBtnConfirm.setEnabled(true);
            }
        });


        boolean isPassword = getArguments().getBoolean(EXTRA_PASSWORD);
        boolean isPasswordVisible = getArguments().getBoolean(EXTRA_VISIBLE_PWD);

        if (isPassword) {
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            mCheckBox.setChecked(true);
        } else if (isPasswordVisible) {
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setMaxLines(1);
            editText.setSingleLine();
            mCheckBox.setChecked(true);
        } else {
            editText.setInputType(EditorInfo.TYPE_CLASS_TEXT);
            mCheckBox.setVisibility(View.GONE);
            editText.setSingleLine(false);
            editText.setMaxLines(5);
        }

        mBtnCancel.setOnClickListener(this);
        mBtnConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (mFragmentListener != null) {
            switch (view.getId()) {
                case android.R.id.button1:
                    mFragmentListener.onChanged(null);
                    break;
                case android.R.id.button2:
                    if (mEditText.getHint() != null && mEditText.getHint().length() > 0 && mEditText.getText().length() == 0)
                        mFragmentListener.onChanged(mEditText.getHint().toString());
                    else
                        mFragmentListener.onChanged(mEditText.getText().toString());
                    break;
            }

        }
        destroyMyself();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked)
            mEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        else
            mEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        if (mEditText.getText().length() > 0) {
            mEditText.setSelection(mEditText.getText().length());
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String regEx = "[^a-zA-Z0-9]";  //只能输入字母或数字
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(editable);
        String str = m.replaceAll("").trim();    //删掉不是字母或数字的字符
        if (!editable.toString().equals(str)) {
            showToast(R.string.english_only);
            mEditText.removeTextChangedListener(this);
            editable.clear();
            editable.append(str);//设置EditText的字符
//            mEditText.setSelection(str.length()); //因为删除了字符，要重写设置新的光标所在位置
            mEditText.addTextChangedListener(this);
        }
    }
}
