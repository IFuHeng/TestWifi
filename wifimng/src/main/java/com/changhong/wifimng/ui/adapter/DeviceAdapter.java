package com.changhong.wifimng.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceItem;
import com.changhong.wifimng.been.DeviceType;

import java.util.List;

/**
 * mesh list adapter
 */
public class DeviceAdapter extends BaseAdapter {

    private final View.OnClickListener listener;
    private Context mContext;
    private List<DeviceItem> mData;

    public DeviceAdapter(Context context, List<DeviceItem> data, View.OnClickListener listener) {
        this.mContext = context;
        this.mData = data;
        this.listener = listener;
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
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_device, null, false);

        View vLink = convertView.findViewById(R.id.view_vertical_line);
        ImageView icon = convertView.findViewById(R.id.icon);
        TextView tv_num = convertView.findViewById(R.id.tv_num);
        TextView tv_ssid = convertView.findViewById(R.id.tv_ssid);
        TextView tv_mac = convertView.findViewById(R.id.tv_mac);
        TextView tv_ip = convertView.findViewById(R.id.tv_ip);
        TextView tv_location = convertView.findViewById(R.id.tv_location);
        ImageView btn_add = convertView.findViewById(R.id.btn_add);

        DeviceItem been = mData.get(position);
        //设置连接状态
        if (been.getUpConnected() == null) {
            vLink.setVisibility(View.GONE);
        } else if (been.getUpConnected()) {
            vLink.setVisibility(View.VISIBLE);
            vLink.setBackgroundColor(Color.GREEN);
        } else {
            vLink.setVisibility(View.VISIBLE);
            vLink.setBackgroundColor(Color.RED);
        }
        // TODO  设置图片
        if (been.getType() == DeviceType.BWR) {
            if (!been.isChild()) {
                setBtnAttr(btn_add, false, true, listener);
            } else
                setBtnAttr(btn_add, false, false, null);
        } else if (been.getType() == DeviceType.R2s) {
            setBtnAttr(btn_add, false, false, null);
        } else {
            setBtnAttr(btn_add, false, false, null);
        }

        //设置连接设备数量
        if (been.getStaNum() == 0)
            tv_num.setVisibility(View.GONE);
        else {
            tv_num.setVisibility(View.VISIBLE);
            tv_num.setText(String.valueOf(been.getStaNum()));
        }

        //设置基本信息
        tv_ssid.setText(been.getDeviceName());
        tv_mac.setText("MAC:" + been.getMac());
        tv_ip.setText("IP:" + been.getIp());
        tv_location.setText(been.getLocation());

        return convertView;
    }

    private void setBtnAttr(View view, boolean focuceable, boolean isVisible, View.OnClickListener listener) {
        view.setFocusable(focuceable);
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        view.setOnClickListener(listener);
    }

}

