package com.changhong.testwifi.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RandomLineBackgroundView extends View {

    /**
     * 连接距离
     */
    private static float DISTANCE_CONNECT;
    /**
     * 点数量
     */
    private static final int COUNT_OF_POINTS = 100;
    /**
     * 点的速度
     */
    private static final int SPEED_OF_POINT = 5;
    /**
     * 重绘间隔
     */
    private final static int SCALE_TIME = 600;

    private static final int WIDTH_LINE = 2;

    private Paint mPaint;

    private List<Point> mArrPoint;
    private ValueAnimator scaleUp;
    private int mLastFingerX = -1, mLastFingerY = -1;

    public RandomLineBackgroundView(Context context) {
        super(context);
    }

    public RandomLineBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RandomLineBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {

//        DISTANCE_CONNECT = CommUtil.dip2px(getContext(), 100);
//        WIDTH_LINE = CommUtil.dip2px(getContext(), 6);

        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(WIDTH_LINE);

        mArrPoint = new ArrayList<>();
        super.onFinishInflate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!changed) {
            return;
        }

        int width = right - left - WIDTH_LINE;
        int height = bottom - top - WIDTH_LINE;
        DISTANCE_CONNECT = Math.round(Math.sqrt(width * width + height * height) / 6);

        mArrPoint.clear();
        if (width > WIDTH_LINE && height > WIDTH_LINE) {
            for (int i = 0; i < COUNT_OF_POINTS; i++) {
                int x = (int) (WIDTH_LINE / 2 + Math.round(Math.random() * width));
                int y = (int) (WIDTH_LINE / 2 + Math.round(Math.random() * height));
                int sx = (int) Math.round((Math.random() - 0.5f) * 2 * SPEED_OF_POINT);
                int sy = (int) Math.round((Math.random() - 0.5f) * 2 * SPEED_OF_POINT);
                if (Math.abs(sx) < 1)
                    sx = sx >= 0 ? 1 : -1;
                if (Math.abs(sy) < 1)
                    sy = sy >= 0 ? 1 : -1;
                mArrPoint.add(new Point(x, y, sx, sy));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        onDrawPoints(canvas);
        onDrawLine(canvas);
//        onDrawPi(canvas, getWidth() / 2, getHeight() - 400, getWidth() / 2, 400);
    }

    private void onDrawPoints(Canvas canvas) {
        mPaint.setAlpha(255);
        mPaint.setStrokeWidth(2);
        int radius = WIDTH_LINE << 1;
        for (Point point : mArrPoint) {
            canvas.drawOval(new RectF(point.x - radius, point.y - radius, point.x + radius, point.y + radius), mPaint);
        }
        radius = WIDTH_LINE << 4;
        if (mLastFingerX >= 0 && mLastFingerY >= 0) {
            canvas.drawOval(new RectF(mLastFingerX - radius, mLastFingerY - radius, mLastFingerX + radius, mLastFingerY + radius), mPaint);
        }
    }

    private void onDrawLine(Canvas canvas) {
        for (int i = 0; i < mArrPoint.size() - 1; i++) {
            for (int j = i + 1; j < mArrPoint.size(); j++) {
                Point point1 = mArrPoint.get(i);
                Point point2 = mArrPoint.get(j);
                float distanceRate = getDistanceRate(point1, point2);
                if (distanceRate > 1)
                    continue;

                mPaint.setStrokeWidth((1 - distanceRate) * WIDTH_LINE + 0.1f);
                mPaint.setAlpha(Math.round((1 - distanceRate) * 160 + 40));
                canvas.drawLine(point1.x, point1.y, point2.x, point2.y, mPaint);
            }
        }
    }

    private float getDistanceRate(Point point1, Point point2) {
        double distance = Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2);
        distance = Math.sqrt(distance);
        float result = (float) (distance / DISTANCE_CONNECT);
        return result;
    }

    private double getDistance(float x1, float y1, float x2, float y2) {
        double result = Math.pow(x1 - x2, 2);
        result += Math.pow(y1 - y2, 2);
        result = Math.sqrt(result);
        return result;
    }


    private void stopAnim() {
        if (scaleUp != null) {
            scaleUp.cancel();
            scaleUp = null;
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            startAnim();
        } else {
            stopAnim();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        startAnim();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnim();
        super.onDetachedFromWindow();
    }

    private class Point {
        private int x, y;
        private int speedX, speedY;

        public Point(int x, int y, int speedX, int speedY) {
            this.x = x;
            this.y = y;
            this.speedX = speedX;
            this.speedY = speedY;
        }

        public void set(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        private void move() {
            if (mLastFingerX >= 0 && mLastFingerY >= 0) {
                double distance = getDistance(mLastFingerX, mLastFingerY, x, y);
                double distanceNext = getDistance(mLastFingerX, mLastFingerY, x + speedX, y + speedY);
                if ((distance <= DISTANCE_CONNECT
                        || Math.round(distance - DISTANCE_CONNECT) == 0)
                        && distanceNext > DISTANCE_CONNECT) {
                    double angle = Math.asin((mLastFingerY - y) / distanceNext);
                    if (mLastFingerX >= x && mLastFingerY < y)
                        angle = -Math.PI - angle;
                    else if (mLastFingerX >= x && mLastFingerY > y) {
                        angle = Math.PI - angle;
                    }
                    Log.d(getClass().getSimpleName(), "====~ angle = " + angle * 180 / Math.PI);
                    y = (int) (DISTANCE_CONNECT * Math.sin(angle)) + mLastFingerY;
                    x = (int) (DISTANCE_CONNECT * Math.cos(angle)) + mLastFingerX;
                    return;
                }
            }

            x += speedX;
            y += speedY;
            if (x < WIDTH_LINE / 2) {
                speedX = -speedX;
                x = WIDTH_LINE - x;
            } else if (x > getWidth() - WIDTH_LINE / 2) {
                speedX = -speedX;
                x = getWidth() * 2 - x - WIDTH_LINE / 2;
            }

            if (y < WIDTH_LINE / 2) {
                speedY = -speedY;
                y = WIDTH_LINE - y;
            } else if (y > getHeight() - WIDTH_LINE / 2) {
                speedY = -speedY;
                y = getHeight() * 2 - y - WIDTH_LINE / 2;
            }
        }
    }


    private void startAnim() {
        stopAnim();

        scaleUp = ValueAnimator.ofInt(0, 100);
        scaleUp.setRepeatCount(Animation.INFINITE);
        scaleUp.setRepeatMode(ValueAnimator.REVERSE);
        scaleUp.setDuration(SCALE_TIME);
        scaleUp.setInterpolator(new LinearInterpolator());
        scaleUp.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (Point point : mArrPoint) {
                    point.move();
                }
                invalidate();
            }
        });
        scaleUp.start();
    }

    private void onDrawPi(Canvas canvas, int offx, int offy, int width, int height) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(4);
        canvas.drawLine(offx - width / 2, offy, offx + width / 2, offy, paint);
        canvas.drawLine(offx + width / 2, offy, offx + width / 2 - 10, offy - 10, paint);
        canvas.drawLine(offx + width / 2, offy, offx + width / 2 - 10, offy + 10, paint);
        canvas.drawLine(offx, offy - height / 2, offx, offy + height / 2, paint);
        canvas.drawLine(offx, offy - height / 2, offx - 10, offy - height / 2 + 10, paint);
        canvas.drawLine(offx, offy - height / 2, offx + 10, offy - height / 2 + 10, paint);

//        paint.setStrokeWidth(1);
        for (int i = -180; i <= 180; i++) {
//            System.out.println("cos(" + i + ") = " + Math.cos(i * Math.PI / 180));
            int x = offx + width * i / 360;
            paint.setColor(Color.MAGENTA);
            double angle = Math.sin(i * Math.PI / 180);
            int y = (int) (height * angle / 2 + offy);
            canvas.drawPoint(x, y, paint);

            paint.setColor(Color.BLUE);
            angle = Math.cos(i * Math.PI / 180);
            y = (int) (height * angle / 2 + offy);
            canvas.drawPoint(x, y, paint);
        }
//        paint.setColor(Color.RED);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d(getClass().getSimpleName(), "====~ onTouchEvent :" + event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mLastFingerX = (int) event.getX();
                mLastFingerY = (int) event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                mLastFingerX = -1;
                mLastFingerY = -1;
                break;
        }
        return super.onTouchEvent(event);
    }
}
