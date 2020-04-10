package com.changhong.wifimng.been;

public class BaseResponseBeen {
    protected int err_code;

    /**
     * 错误信息，error=0时为空；其余都有相关信息
     */
    protected String message;

    /**
     * 等待执行时间，单位s
     */
    protected Integer waite_time;

    private Error error;

    public int getErr_code() {
        if (error != null)
            return error.code;
        return err_code;
    }


    public String getMessage() {
        if (error != null)
            return error.message;
        return message;
    }


    public Integer getWaite_time() {
        return waite_time;
    }

    @Override
    public String toString() {
        return "BaseResponseBeen{" +
                "err_code=" + getErr_code() +
                ", message='" + getMessage() + '\'' +
                ", waite_time=" + waite_time +
                '}';
    }

    class Error {
        Integer code;
        String message;
    }
}
