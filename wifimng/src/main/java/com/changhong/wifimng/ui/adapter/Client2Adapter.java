package com.changhong.wifimng.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.StaInfo;
import com.changhong.wifimng.uttils.ClientIconUtils;
import com.changhong.wifimng.uttils.CommUtil;
import com.changhong.wifimng.uttils.FileSizeUtils;

import java.util.List;

/**
 * support android.R.layout.
 */
public class Client2Adapter extends ClientAdapter {

    private int mResId;

    public Client2Adapter(Context context, int resId, List<StaInfo> data) {
        super(context, data);
        mResId = resId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(mContext).inflate(mResId, null, false);

        TextView textView = (TextView) convertView;
        StaInfo item = mData.get(position);
        {
            Drawable drawable = mContext.getResources().getDrawable(ClientIconUtils.getClientDevcieByString(item.getName()));
            int size = CommUtil.dip2px(mContext, 50);
            drawable.setBounds(0, 0, size, size);
            textView.setCompoundDrawables(drawable, null, null, null);
            textView.setCompoundDrawablePadding(CommUtil.dip2px(mContext, 10));
        }

        String name = item.getName();
        String mac = item.getMac();
        if (name == null)
            name = mac;

        textView.setText(name);
        return convertView;
    }


}