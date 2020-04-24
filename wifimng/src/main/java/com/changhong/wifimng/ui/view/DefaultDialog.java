package com.changhong.wifimng.ui.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.changhong.wifimng.R;

public class DefaultDialog extends Dialog implements View.OnClickListener {
    private final TextView mTvMessage;
    private final TextView button1;
    private final TextView button2;

    public DefaultDialog(@NonNull Context context, CharSequence title, @DrawableRes int icon, CharSequence message, CharSequence btnText1,
                         OnClickListener listener1, CharSequence btnText2, OnClickListener listener2) {
        super(context, R.style.defaultDialogStyle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_default);

        TextView tvTitle = findViewById(R.id.dialog_title);
        if (title != null) {
            tvTitle.setText(title);
            tvTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0);
        } else
            tvTitle.setVisibility(View.GONE);


        mTvMessage = findViewById(R.id.dialog_message);
        mTvMessage.setText(message);

        button1 = findViewById(R.id.btn1);
        if (!TextUtils.isEmpty(btnText1)) {
            button1.setText(btnText1);
            button1.setTag(listener1);
        }
        button1.setOnClickListener(this);

        button2 = findViewById(R.id.btn2);
        if (!TextUtils.isEmpty(btnText2)) {
            button2.setText(btnText2);
            button2.setTag(listener2);
        } else
            button2.setVisibility(View.GONE);
        button2.setOnClickListener(this);

        Window dialogWindow = getWindow();
        //设置动画
        dialogWindow.setWindowAnimations(R.style.defualtDialogAnimation);
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(lp);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn1) {
            if (view.getTag() != null && view.getTag() instanceof OnClickListener) {
                OnClickListener listener = (OnClickListener) view.getTag();
                listener.onClick(this, AlertDialog.BUTTON_POSITIVE);
            }
        } else if (id == R.id.btn2) {
            if (view.getTag() != null && view.getTag() instanceof OnClickListener) {
                OnClickListener listener = (OnClickListener) view.getTag();
                listener.onClick(this, AlertDialog.BUTTON_NEGATIVE);
            }
        }
        dismiss();
    }

    public void setMessage(CharSequence charSequence) {
        mTvMessage.setText(charSequence);
    }

    public void setButton(int buttonPositive, CharSequence txBtn, OnClickListener listener) {
        if (buttonPositive == AlertDialog.BUTTON_POSITIVE) {
            if (!TextUtils.isEmpty(txBtn))
                button1.setText(txBtn);
            else
                button1.setText(R.string.ok);
            button1.setTag(listener);
        } else if (buttonPositive == AlertDialog.BUTTON_NEGATIVE) {
            if (!TextUtils.isEmpty(txBtn)) {
                button2.setText(txBtn);
                button2.setVisibility(View.VISIBLE);
            } else
                button2.setVisibility(View.GONE);
            button2.setTag(listener);
        }
    }
}
