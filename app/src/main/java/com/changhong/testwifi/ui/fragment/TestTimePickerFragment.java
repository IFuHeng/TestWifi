package com.changhong.testwifi.ui.fragment;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.changhong.wifimng.ui.fragment.BaseFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestTimePickerFragment extends BaseFragment implements View.OnClickListener {

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat mSDFormatHHmm = new SimpleDateFormat("HH:mm");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(com.changhong.wifimng.R.layout.fragment_group_setting2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewFlipper mViewFlipper = view.findViewById(com.changhong.wifimng.R.id.viewFliper01);
        mViewFlipper.setDisplayedChild(3);

        view.findViewById(com.changhong.wifimng.R.id.tv_startTime).setOnClickListener(this);
        view.findViewById(com.changhong.wifimng.R.id.tv_endTime).setOnClickListener(this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == com.changhong.wifimng.R.id.tv_startTime
                || v.getId() == com.changhong.wifimng.R.id.tv_endTime)
            setTimeTextView((TextView) v);
    }

    /**
     * 设置时间
     *
     * @param textView 预备修改的文字组件
     */
    private void setTimeTextView(@NonNull TextView textView) {
        Date date;
        Log.d(getClass().getSimpleName(), "====~setTimeTextView  textview id = " + textView.getId() + " ,  textview text = " + textView.getText());
        try {
            date = mSDFormatHHmm.parse(textView.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }
        TimePickerDialog timeDialog = new TimePickerDialog(mActivity, new OnTimeSetListener(textView), 12, 0, true);
        timeDialog.updateTime(date.getHours(), date.getMinutes());
        timeDialog.show();
    }

    private class OnTimeSetListener implements TimePickerDialog.OnTimeSetListener {

        private TextView textView;

        public OnTimeSetListener(@NonNull TextView textView) {
            this.textView = textView;
            Log.d(getClass().getSimpleName(), "====~Constructor  textview id = " + textView.getId() + " ,  textview text = " + textView.getText());
            Log.d(getClass().getSimpleName(), "====~Constructor  this.textview id = " + this.textView.getId() + " ,  this.textview text = " + this.textView.getText());
        }

        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
            Log.d(getClass().getSimpleName(), "====~" + hourOfDay + ":" + minute);
            textView.setText(String.format("%02d:%02d", hourOfDay, minute));
            Log.d(getClass().getSimpleName(), "====~onTimeSet textview id = " + textView.getId() + " ,  textview text = " + textView.getText());
        }
    }
}
