package com.changhong.wifimng.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceItem;

import java.util.List;
import java.util.Stack;

public class ChildRouterAdapter extends PagerAdapter {

    private List<DeviceItem> mData;
    private LayoutInflater inflater;

    private Stack<View> mStackViews;
    private View mViewAdd;

    private View.OnClickListener mListenerChild;

    public ChildRouterAdapter(Context context, List<DeviceItem> data, View.OnClickListener childlistener, View.OnClickListener addListener) {
        this.mData = data;
        inflater = LayoutInflater.from(context);

        mListenerChild = childlistener;

        mViewAdd = inflater.inflate(R.layout.item_device_add, null, false);
        mViewAdd.setOnClickListener(addListener);

        mStackViews = new Stack<>();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = container.getChildAt(position);
        view.setOnClickListener(null);
        container.removeView(view);
        if (view == mViewAdd)
            ;
        else
            mStackViews.push(view);
        Log.d(getClass().getSimpleName(), "====~ destroyItem :   mStackViews = " + mStackViews);
        Log.d(getClass().getSimpleName(), "====~ destroyItem :   position = " + position + " , object =" + object);
    }

    @Override
    public int getCount() {
        if (mData.isEmpty())
            return 1;
        return mData.size() + 1;
    }

    @Override
    public Object instantiateItem(ViewGroup viewGroup, int position) {
        View view;
        if (mData == null) {
            viewGroup.addView(mViewAdd);
            return mViewAdd;
        } else if (mData.size() <= position) {
            if (isContainView(viewGroup, mViewAdd)) {
                viewGroup.removeView(mViewAdd);
            }
            viewGroup.addView(mViewAdd);
            return mViewAdd;
        } else if (mStackViews.isEmpty())
            view = inflater.inflate(R.layout.item_device1, viewGroup, false);
        else
            view = mStackViews.pop();

        DeviceItem item = mData.get(position);
        ImageView icon = view.findViewById(R.id.icon);
        TextView tv_num = view.findViewById(R.id.tv_num);
        TextView name = view.findViewById(R.id.tv_name);
        TextView ip = view.findViewById(R.id.tv_ip);
        tv_num.setText(String.valueOf(item.getStaNum()));
        name.setText(item.getDeviceName() + "\t" + item.getLocation());
        ip.setText("IP\t" + item.getIp());

        view.setOnClickListener(mListenerChild);
        view.setTag(item);

        viewGroup.addView(view, 0);
        Log.d(getClass().getSimpleName(), "====~ instantiateItem :   mStackViews = " + mStackViews);
        return view;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        Log.d(getClass().getSimpleName(), "====~ isViewFromObject :   view = " + view + " , object =" + object);
        return view.equals(object);
    }

    private boolean isContainView(ViewGroup viewGroup, View view) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i) == view)
                return true;
        }
        return false;
    }

}
