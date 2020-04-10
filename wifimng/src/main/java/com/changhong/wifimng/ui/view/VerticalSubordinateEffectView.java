package com.changhong.wifimng.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.changhong.wifimng.R;
import com.changhong.wifimng.uttils.CommUtil;

import java.util.Arrays;

/**
 * 纵向从属效果
 */
public class VerticalSubordinateEffectView extends View {

    private View mMain;
    private ViewGroup mGroupChild;
    private Paint paint = new Paint();

    private final int[] DEFAULT_LOCATION = new int[2];

    private int[] X_LOCATION_CHILDS;
    /**
     * 此view在屏幕中的坐标
     */
    private int mScreenLocationX, mScreenLocationY;

    public VerticalSubordinateEffectView(Context context) {
        super(context);
    }

    public VerticalSubordinateEffectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalSubordinateEffectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        paint.setColor(getContext().getResources().getColor(R.color.colorLine));
        paint.setStrokeWidth(CommUtil.dip2px(getContext(),3));
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            getLocationOnScreen(DEFAULT_LOCATION);
            mScreenLocationX = DEFAULT_LOCATION[0];
            mScreenLocationY = DEFAULT_LOCATION[1];
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    public void setMain(View main) {
        this.mMain = main;
    }

    public void setArrChild(ViewGroup arrChild) {
        this.mGroupChild = arrChild;
    }

    public void setLineColor(int lineColor) {
        paint.setColor(lineColor);
    }

    /**
     * @param view
     * @return 目标view的相对左X坐标
     */
    private int getViewScreenX(View view) {
        view.getLocationOnScreen(DEFAULT_LOCATION);
        return DEFAULT_LOCATION[0] - mScreenLocationX;
    }

    /**
     * @param view
     * @return 目标view的相对居中X坐标
     */
    private int getViewScreenCenterX(View view) {
        view.getLocationOnScreen(DEFAULT_LOCATION);
        return DEFAULT_LOCATION[0] - mScreenLocationX + view.getWidth() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int x = 0;
        int y = getHeight() / 2;
        if (mMain != null)
            x = getViewScreenCenterX(mMain);
        else
            x = getWidth() / 2;

        if (mGroupChild == null || mGroupChild.getChildCount() == 0) {
            return;
        }

        canvas.drawLine(x, 0, x, getHeight() >> 1, paint);

        //drawChildPart
        if (X_LOCATION_CHILDS == null || X_LOCATION_CHILDS.length != mGroupChild.getChildCount()) {
            X_LOCATION_CHILDS = new int[mGroupChild.getChildCount()];
        }

        for (int i = 0; i < mGroupChild.getChildCount(); i++) {
            View view = mGroupChild.getChildAt(i);
            X_LOCATION_CHILDS[i] = getViewScreenCenterX(view);
        }
        Arrays.sort(X_LOCATION_CHILDS);

        x = X_LOCATION_CHILDS[0];
        canvas.drawLine(x, y, X_LOCATION_CHILDS[X_LOCATION_CHILDS.length - 1], y, paint);
        for (int i : X_LOCATION_CHILDS) {
            canvas.drawLine(i, y, i, getHeight(), paint);
        }
    }
}
