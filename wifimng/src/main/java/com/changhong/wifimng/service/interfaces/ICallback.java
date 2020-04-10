package com.changhong.wifimng.service.interfaces;

public interface ICallback<T1, T2> {
    T1 onCallBack(T2... t2s);
}
