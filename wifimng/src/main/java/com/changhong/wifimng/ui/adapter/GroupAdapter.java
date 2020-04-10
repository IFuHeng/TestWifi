package com.changhong.wifimng.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen;

import java.util.List;

/**
 * 分组列表的容器
 */
public class GroupAdapter extends BaseAdapter {
    Context context;
    private List<BaseBeen<String, Integer>> mData;
    private View.OnClickListener mOnclickListener;

    public GroupAdapter(Context context, List<BaseBeen<String, Integer>> mData, View.OnClickListener listener) {
        this.context = context;
        this.mData = mData;
        this.mOnclickListener = listener;
    }

    @Override
    public int getCount() {
        if (mData == null)
            return 1;
        return mData.size() + 1;
    }

    @Override
    public Object getItem(int i) {
        if (mData == null)
            return null;
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        if (mData == null)
            return 0;
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_group, null, false);
            view.findViewById(R.id.btn_edit).setVisibility(View.GONE);
            view.findViewById(R.id.btn_delete).setOnClickListener(mOnclickListener);
        }
        TextView textView = view.findViewById(R.id.tv_name);
        textView.setTypeface(Typeface.DEFAULT);
        if (i == 0)
            textView.setText(R.string.default_group);
        else {
            String name = mData.get(i - 1).getT1();
            int num = mData.get(i - 1).getT2();
            SpannableString ss = new SpannableString(name + " (" + num + ")");
            ss.setSpan(new RelativeSizeSpan(0.9f), name.length(), ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(Color.MAGENTA), name.length(), ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            textView.setText(ss);
        }

        view.findViewById(R.id.btn_delete).setVisibility(i == 0 ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.btn_delete).setTag(i - 1);

        return view;
    }
}