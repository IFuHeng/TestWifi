package com.changhong.testwifi.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.changhong.testwifi.R;
import com.changhong.testwifi.utils.BeepUtils2;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BeepFragment extends BaseFragment {


    private BeepUtils2 mBeepUtils;

    private long mInterval;

    private int mCount;

    private int mAimCount;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mBeepUtils.play_voice();
            mTvCount.setText(String.valueOf(++mCount));
            if (mCount < mAimCount)
                sendEmptyMessageDelayed(0, mInterval);
            else {
                setEditEnable(true);
            }
            super.handleMessage(msg);
        }
    };

    private EditText mEtTimes;
    private EditText mEtInterval;
    private FloatingActionButton mBtnPlay;
    private TextView mTvCount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBeepUtils = new BeepUtils2(mActivity);
        mBeepUtils.initVoice2();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_beep, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mEtTimes = (EditText) view.findViewById(R.id.et_times);
        mEtInterval = (EditText) view.findViewById(R.id.et_interval);
        mTvCount = (TextView) view.findViewById(R.id.et_has_done_count);

        mBtnPlay = view.findViewById(R.id.btn_play);
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mEtInterval.getText().length() == 0) {
                    mInterval = 1000;
                    mEtInterval.setText(String.valueOf(mInterval));
                } else {
                    int interval = 1000;
                    try {
                        interval = Integer.parseInt(mEtInterval.getText().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (interval < 100) {
                        interval = 100;
                        mEtInterval.setText(String.valueOf(interval));
                    }else if (interval>30000){
                        interval = 30000;
                        mEtInterval.setText(String.valueOf(interval));
                    }

                    mInterval = interval;
                }
                if (mEtTimes.getText().length() == 0) {
                    mAimCount = Integer.MAX_VALUE;
                    mEtTimes.setText(String.valueOf(mAimCount));
                } else {
                    int count;
                    try {
                        count = Integer.parseInt(mEtTimes.getText().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        count = 1;
                    }
                    if (count == 0) {
                        count = Integer.MAX_VALUE;
                    }
                    mEtTimes.setText(String.valueOf(count));
                    mAimCount = count;
                }

                setEditEnable(v.getTag() != null && (Boolean) v.getTag());
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onDestroy() {
        handler.removeMessages(0);
        super.onDestroy();
    }

    private void setEditEnable(boolean is) {

        mEtInterval.setEnabled(is);
        mEtTimes.setEnabled(is);

        mBtnPlay.setImageResource(is ? android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause);

        mBtnPlay.setTag(is ? null : true);

        if (is) {
            handler.removeMessages(0);
        } else {
            handler.sendEmptyMessage(0);
            mCount = 0;
            mTvCount.setText(String.valueOf(mCount));
        }
    }
}
