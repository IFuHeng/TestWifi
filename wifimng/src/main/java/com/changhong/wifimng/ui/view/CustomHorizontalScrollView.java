package com.changhong.wifimng.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

import java.util.Observer;

public class CustomHorizontalScrollView extends HorizontalScrollView {
    private Observer mScrollChangeObserver;

    public CustomHorizontalScrollView(Context context) {
        super(context);
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (oldl != l)
            mScrollChangeObserver.update(null, 0);
        if (oldt != t)
            mScrollChangeObserver.update(null, 1);
    }

    @Override
    public void setOnScrollChangeListener(OnScrollChangeListener l) {
        super.setOnScrollChangeListener(l);
    }

    public void setOnScrollChangeListener(Observer observer) {
        mScrollChangeObserver = observer;
    }
}
