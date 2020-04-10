package com.changhong.wifimng.http.been;

public class UserManagerRequestBeen {

    /**
     * 验证账户,邮箱或者手机号
     */
    private String account;
    /**
     * 验证类型，0：电话号码，1：邮箱
     */
    private Integer authType;

    public void setAccount(String account) {
        this.account = account;
    }

    public void setAuthType(Integer authType) {
        this.authType = authType;
    }
}
