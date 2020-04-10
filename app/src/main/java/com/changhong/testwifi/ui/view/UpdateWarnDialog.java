package com.changhong.testwifi.ui.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.changhong.testwifi.R;
import com.changhong.testwifi.ui.fragment.BaseDialogFragment;

public class UpdateWarnDialog extends BaseDialogFragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_update_info, null, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        TextView textView = view.findViewById(R.id.tv_info);

        String updateinfo = getArguments().getString(Intent.EXTRA_TEXT);
        int start = updateinfo.indexOf(':');
        if (start == -1) {
            textView.setText(updateinfo);
        } else {
            SpannableString ss = new SpannableString(updateinfo);
            int end;
            do {
                end = updateinfo.indexOf('\n', start + 1);
                if (end == -1)
                    end = updateinfo.length();
                else while (end < updateinfo.length() && end - start == 1)
                    end = updateinfo.indexOf('\n', end + 1);

                ss.setSpan(new ForegroundColorSpan(Color.BLUE), start + 1, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                ss.setSpan(new RelativeSizeSpan(0.7f), start + 1, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                start = updateinfo.indexOf(':', end + 1);
                if (start == -1)
                    break;
            } while (end != -1 && end <= updateinfo.length());
            textView.setText(ss);
        }
        view.findViewById(R.id.btn_done).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (mFragmentListener != null) {
            switch (view.getId()) {
                case R.id.btn_done:
                    mFragmentListener.onChanged(true);
                case R.id.btn_cancel:
                    mFragmentListener.onChanged(false);
                    break;
            }

        }
        destroyMyself();
    }
}
