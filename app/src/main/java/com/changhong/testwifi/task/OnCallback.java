package com.changhong.testwifi.task;

public interface OnCallback<T1,T2> {
    void onStep(T2 result);

    void onCallback(T1 result);
}
