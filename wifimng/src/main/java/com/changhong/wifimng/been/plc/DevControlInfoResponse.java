package com.changhong.wifimng.been.plc;

import com.changhong.wifimng.been.BaseResponseBeen;
import com.changhong.wifimng.been.Group;

import java.util.List;

public class DevControlInfoResponse extends BaseResponseBeen {
    /**
     * 实施父母控制的设备列表
     */
    List<DevControlRuleBeen> dev_info;

    public List<DevControlRuleBeen> getDev_info() {
        return dev_info;
    }
}
