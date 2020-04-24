package com.changhong.wifimng.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.changhong.wifimng.R;

public class DefaultProgressDialog extends Dialog {
    private final TextView mTvMessage;

    public DefaultProgressDialog(@NonNull Context context, CharSequence message) {
        super(context, R.style.progressDialogStyle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_progress);

        mTvMessage = findViewById(R.id.dialog_message);
        mTvMessage.setText(message);

        Window dialogWindow = getWindow();
        //设置动画
        dialogWindow.setWindowAnimations(R.style.defualtDialogAnimation);
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(lp);
    }


    public void setMessage(CharSequence charSequence) {
        mTvMessage.setText(charSequence);
    }

}
