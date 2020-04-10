package com.changhong.testwifi.ui.fragment;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.changhong.testwifi.R;
import com.changhong.wifimng.ui.fragment.BaseFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TestListViewFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {


    private ListView mListView;
    private RadioGroup radioGroup;
    List<CharSequence> list = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] strings = getResources().getStringArray(R.array.week_days);
        StaInGroupItem item = new StaInGroupItem();
        for (int i = 0; i < strings.length; i++) {
            item.group = Math.random() > 0.5f ? strings[i] : null;
            item.name = Math.random() > 0.5f ? strings[i] : null;
            item.mac = strings[i];
            list.add(item.getShowCharsequence(Color.HSVToColor(new float[]{Math.round(Math.random() * 180 + 180), 1, 1})));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mListView = view.findViewById(R.id.listview);

        mListView.setAdapter(new ArrayAdapter(mActivity, android.R.layout.simple_list_item_1, list));
        mListView.setOnItemClickListener(this);
        mListView.setOnItemSelectedListener(this);
        radioGroup = view.findViewById(R.id.radioGroup01);
        radioGroup.setOnCheckedChangeListener(this);
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onItemClick(@NonNull AdapterView<?> parent, View view, int position, long id) {
        if (view.getTag() == null) {
            view.setTag(true);
            view.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.view_disable));
        } else {
            view.setTag(null);
            view.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.view_enable));
        }

        switch (radioGroup.indexOfChild(radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()))) {
            case 1:
                Log.d(getClass().getSimpleName(), "====~" + mListView.getCheckedItemPosition());
                break;
            case 2:
                Log.d(getClass().getSimpleName(), "====~" + Arrays.toString(mListView.getCheckItemIds()));
                Log.d(getClass().getSimpleName(), "====~" + Arrays.toString(mListView.getCheckedItemIds()));
                break;
            default:
                setTimeTextView((TextView) view);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int index = group.indexOfChild(group.findViewById(checkedId));
        switch (index) {
            case 1:
                mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                mListView.setAdapter(new ArrayAdapter(mActivity, android.R.layout.simple_list_item_single_choice, list));
                break;
            case 2:
                mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                mListView.setAdapter(new ArrayAdapter(mActivity, android.R.layout.simple_list_item_multiple_choice, list));
                break;
            default:
                mListView.setAdapter(new ArrayAdapter(mActivity, android.R.layout.simple_list_item_1, list));
                mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class StaInGroupItem {
        String name;
        String mac;
        String group;

        CharSequence getShowCharsequence(int... colors) {
            String imgstr = "<img>";
            String prefix = TextUtils.isEmpty(name) ? "<no name>" : name;

            String suffix = TextUtils.isEmpty(group) ? "DEFAULT_GROUP" : group;
            SpannableString ss = new SpannableString(imgstr + prefix + " " + suffix + '\n' + mac);
            int start = 0;
            int end = imgstr.length();

            ss.setSpan(new ImageSpan(mActivity, R.drawable.ic_client_android), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            if (TextUtils.isEmpty(name)) {
                start = end;
                end += prefix.length();
                ss.setSpan(new ForegroundColorSpan(Color.GRAY), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }

            start = imgstr.length() + prefix.length() + 1;
            end = start + suffix.length();
            if (!TextUtils.isEmpty(group) && colors != null && colors.length > 0)
                ss.setSpan(new ForegroundColorSpan(colors[0]), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            else
                ss.setSpan(new ForegroundColorSpan(Color.LTGRAY), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

            ss.setSpan(new RelativeSizeSpan(0.8f), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            ss.setSpan(new RelativeSizeSpan(0.9f), end, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return ss;
        }
    }

    private void setTimeTextView(final TextView textView) {
        SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
        Date date;
        try {
            date = sf.parse(textView.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }
        TimePickerDialog dialog = new TimePickerDialog(mActivity, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Log.d(getClass().getSimpleName(), "====~" + hourOfDay + ":" + minute);
                textView.setText(String.format("%02d:%02d", hourOfDay, minute));
            }
        }, 12, 0, true);
        dialog.setMessage(textView.getText());
        dialog.updateTime(date.getHours(), date.getMinutes());
        dialog.show();
    }
}
