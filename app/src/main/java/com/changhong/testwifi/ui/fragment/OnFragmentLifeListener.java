package com.changhong.testwifi.ui.fragment;

public interface OnFragmentLifeListener<T> {

    void onStarted(T t);

    void onChanged(T t);

    void onStopped(T t);

}
