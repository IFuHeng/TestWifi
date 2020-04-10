package com.changhong.wifimng.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.StaInfo;
import com.changhong.wifimng.uttils.ClientIconUtils;
import com.changhong.wifimng.uttils.FileSizeUtils;

import java.util.List;

public class ClientAdapter extends BaseAdapter {

    protected Context mContext;

    protected List<StaInfo> mData;

    public ClientAdapter(Context context, List<StaInfo> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public int getCount() {
        if (mData == null)
            return 0;
        else
            return mData.size();
    }

    @Override
    public Object getItem(int position) {
        if (mData == null)
            return null;
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_mobile, null, false);

        ImageView imageView = convertView.findViewById(R.id.icon);

        TextView tv_name = convertView.findViewById(R.id.tv_name);
        TextView tv_245g = convertView.findViewById(R.id.tv_245g);
        TextView tv_time = convertView.findViewById(R.id.tv_time);
        TextView tv_down_speed = convertView.findViewById(R.id.tv_down_speed);

        StaInfo item = mData.get(position);
        if (TextUtils.isEmpty(item.getName()))
            tv_name.setText(item.getMac());
        else
            tv_name.setText(item.getName());
        tv_245g.setText(item.getConnectType().getResId());

        imageView.setImageResource(
                ClientIconUtils.getClientDevcieByString(item.getName())
        );

        if (item.getLink_time() != 0) {
            //连接时间
            String linktime;
            int hour = item.getLink_time() / 3600;
            int minute = item.getLink_time() % 3600 / 60;
            if (hour > 0) {
                String format = mContext.getString(R.string.format_link_time_hhmm);
                if (format.contains("minutes")) {
                    if (minute < 2)
                        format.replace("minutes", "minute");
                    if (hour < 2)
                        format.replace("hours", "hour");
                }
                linktime = String.format(mContext.getString(R.string.format_link_time_hhmm), hour, minute);
            } else {
                String format = mContext.getString(R.string.format_link_time_mm);
                if (minute < 2 && format.contains("minutes"))
                    format.replace("minutes", "minute");
                linktime = String.format(format, minute);
            }
            tv_time.setText(linktime);
        } else if (!TextUtils.isEmpty(item.getIp())) {
            tv_time.setText(item.getIp());
        }
        //速度
        tv_down_speed.setText(FileSizeUtils.FormetFileSize(item.getDownload()) + "/s");

        return convertView;
    }

}