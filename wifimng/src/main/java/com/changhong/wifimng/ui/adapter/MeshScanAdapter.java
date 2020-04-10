package com.changhong.wifimng.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.graphics.drawable.DrawableCompat;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceBeen;
import com.changhong.wifimng.been.DeviceItem;
import com.changhong.wifimng.uttils.CommUtil;

import java.util.ArrayList;

public class MeshScanAdapter extends BaseAdapter {

    private ArrayList<DeviceItem> mData;

    private Context mContext;

    public MeshScanAdapter(Context context, ArrayList<DeviceItem> data) {
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
        else
            return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
//            convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_selectable_list_item, null, false);
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_mesh_scanning, null, false);
            TextView textView = (TextView) convertView;
            textView.setCompoundDrawablePadding(CommUtil.dip2px(mContext, 8));
            Drawable drawable = mContext.getResources().getDrawable(R.drawable.ic_mesh);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            textView.setCompoundDrawables(drawable, null, null, null);
        }

        TextView textView = (TextView) convertView;
        DeviceItem item = mData.get(position);
        StringBuilder sb = new StringBuilder();
        if (item.getDeviceName() != null)
            sb.append(item.getDeviceName()).append('\n');

        sb.append(item.getIp()).append('\n');
        sb.append(item.getMac()).append('\n');

        Drawable drawable = textView.getCompoundDrawables()[0];
        if (!item.isChild()) {
            convertView.setEnabled(item.isChild());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DrawableCompat.wrap(drawable.mutate()).setTint(mContext.getResources().getColor(R.color.colorCellEdge));
            }
            sb.append(mContext.getString(R.string.mainNode));
        } else if (item.getQlink() != 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DrawableCompat.wrap(drawable.mutate()).setTint(mContext.getResources().getColor(R.color.colorPrimaryDark));
            }
            convertView.setEnabled(false);
            sb.append(mContext.getString(R.string.childNode)).append('(').append(mContext.getString(R.string.unavailable)).append(')');
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DrawableCompat.wrap(drawable.mutate()).setTint(mContext.getResources().getColor(R.color.colorPrimary));
            }
            convertView.setEnabled(true);
            sb.append(mContext.getString(R.string.childNode));
        }

        textView.setText(sb.toString());

        return convertView;
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        if (convertView == null)
//            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_device_client_scanning, null, false);
//
//        ImageView icon = convertView.findViewById(R.id.icon);
//        icon.setImageResource(R.drawable.ic_mesh);
//        TextView tvName = convertView.findViewById(R.id.tv_ssid);
//        TextView tvip = convertView.findViewById(R.id.tv_ip);
//        TextView tvmac = convertView.findViewById(R.id.tv_mac);
//        TextView tvMainOrChild = convertView.findViewById(R.id.tv_main_or_child);
//
//        DeviceItem item = mData.get(position);
//
//        tvName.setText(item.getDeviceName());
//        tvMainOrChild.setText(item.isChild() ? R.string.childNode : R.string.mainNode);
//
//        tvip.setText(item.getIp());
//        tvmac.setText(item.getMac());
//
//        return convertView;
//    }
}
