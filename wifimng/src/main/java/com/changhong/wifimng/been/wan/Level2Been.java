package com.changhong.wifimng.been.wan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import java.lang.reflect.Field;

public class Level2Been {
    /**
     * 需校准mac地址是否有效
     */
    private String mac;
    /**
     * 不得超过32字符
     */
    private String description;
    private String ip;

    /**
     * 上行带宽限制，单位Kbps，0表示不限速。不能超过WAN口最大速率，百兆为102400（100Mbps），千兆为1024000（1000Mbps）
     */
    private Integer max_up_bandwidth;
    /**
     * 下行带宽限制，单位Kbps，0:表示不限速。不能超过WAN口最大速率，百兆为102400（100Mbps），千兆为1024000（1000Mbps）
     */
    private Integer max_down_bandwidth;

    private String name;
    /**
     * bit0: 有线;bit1:2.4G; bit2:5G；bit3:访客网络
     * 如1表示有线； 2表示2.4G；4表示5G；8表示访客网络
     */
    private Integer connect_type;
    /**
     * 在线时长 单位秒.(有线没有时长)
     */
    private Integer link_time;

    /**
     * 上行带宽限制，单位Kbps，0表示不限速。不能超过WAN口最大速率，百兆为102400（100Mbps），千兆为1024000（1000Mbps）
     */
    private Integer max_upload;
    /**
     * 下行带宽限制，单位Kbps，0:表示不限速。不能超过WAN口最大速率，百兆为102400（100Mbps），千兆为1024000（1000Mbps）
     */
    private Integer max_download;

    /**
     * 允许上网的开始时间，小时；     为0-23取整数值。
     */
    private Integer start_h;
    /**
     * 允许上网的开始时间，分钟；     为0-59取整数值
     */
    private Integer start_m;
    /**
     * 允许上网的开始时间，星期；
     * 0-7取整数，0和7表示周日；如果同时存几个的话，输入格式为：x,x,x
     * 比如同时有周一周三周日，输入为：
     * 1,3,7；只“*”表示周一至周日均执行
     */
    private String start_w;
    /**
     * 结束上网的开始时间，小时；
     * 为0-23取整数值。
     */
    private Integer end_h;
    /**
     * 结束上网的开始时间，分钟；
     * 为0-59取整数值
     */
    private Integer end_m;

    /**
     * string	required	结束上网的开始时间，星期；
     * 0-7取整数，0和7表示周日；如果同时存几个的话，输入格式为：x,x,x
     * 比如同时有周一周三周日，输入为：
     * 1,3,7；只“*”表示周一至周日均执行
     */
    private String end_w;

    /**
     * 主要用在6.3.4	internet_time_limit_setting，用于判断表单行是新增、修改、删除
     * null：删除
     * true：新增
     * false：修改
     */
    private Boolean state;

    /**
     * 主要用在6.3.4	internet_time_limit_setting，用于判断表单行是新增、修改、删除
     * null：删除
     * true：新增
     * false：修改
     */
    public Boolean getState() {
        return state;
    }

    /**
     * 主要用在6.3.4	internet_time_limit_setting，用于判断表单行是新增、修改、删除
     * null：删除
     * true：新增
     * false：修改
     */
    public void setState(Boolean state) {
        this.state = state;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getMax_up_bandwidth() {
        return max_up_bandwidth;
    }

    public void setMax_up_bandwidth(Integer max_up_bandwidth) {
        this.max_up_bandwidth = max_up_bandwidth;
    }

    public Integer getMax_down_bandwidth() {
        return max_down_bandwidth;
    }

    public void setMax_down_bandwidth(Integer max_down_bandwidth) {
        this.max_down_bandwidth = max_down_bandwidth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getConnect_type() {
        return connect_type;
    }

    public void setConnect_type(Integer connect_type) {
        this.connect_type = connect_type;
    }

    public Integer getLink_time() {
        return link_time;
    }

    public void setLink_time(Integer link_time) {
        this.link_time = link_time;
    }

    public Integer getMax_upload() {
        return max_upload;
    }

    public void setMax_upload(Integer max_upload) {
        this.max_upload = max_upload;
    }

    public Integer getMax_download() {
        return max_download;
    }

    public void setMax_download(Integer max_download) {
        this.max_download = max_download;
    }

    public Integer getStart_h() {
        return start_h;
    }

    public Integer getStart_m() {
        return start_m;
    }

    public String getStart_w() {
        return start_w;
    }

    public Integer getEnd_h() {
        return end_h;
    }

    public Integer getEnd_m() {
        return end_m;
    }

    public String getEnd_w() {
        return end_w;
    }

    public void setStart_h(Integer start_h) {
        this.start_h = start_h;
    }

    public void setStart_m(Integer start_m) {
        this.start_m = start_m;
    }

    public void setStart_w(String start_w) {
        this.start_w = start_w;
    }

    public void setEnd_h(Integer end_h) {
        this.end_h = end_h;
    }

    public void setEnd_m(Integer end_m) {
        this.end_m = end_m;
    }

    public void setEnd_w(String end_w) {
        this.end_w = end_w;
    }

    public Level2Been cloneOneForInternetTimeLimit() {
        Level2Been result = new Level2Been();
        result.setStart_h(getStart_h());
        result.setStart_m(getStart_m());
        result.setStart_w(getStart_w());
        result.setEnd_h(getEnd_h());
        result.setEnd_m(getEnd_m());
        result.setEnd_w(getEnd_w());
        return result;
    }

    /**
     * 将上网健康时间限制的内容转化成文字
     *
     * @return
     */
    public String getHealthString(@NonNull String[] weekday, @NonNull String to, @NonNull String weekends, @Nullable String weekdays, @Nullable String everyday) {
        String weekRule;
        byte value = getWeekDays();
        if (value == 0x7f) {
            weekRule = everyday;
        } else if (value == 0x1f) {
            weekRule = weekdays;
        } else if (value == 0x60) {
            weekRule = weekends;
        } else if (getStart_w().indexOf(',') == -1) {//连续日期
            Integer start = Integer.parseInt(getStart_w());
            Integer end = Integer.parseInt(getEnd_w());
            if (start == end) {
                weekRule = weekday[start - 1];
            } else {
                weekRule = weekday[start - 1] + to + weekday[end - 1];
            }
        } else {//断续日期
            StringBuilder ss = new StringBuilder();
            String[] starts = getStart_w().split(",");
            String[] ends = getEnd_w().split(",");
            for (int j = 0; j < starts.length; j++) {
                Integer start = Integer.parseInt(starts[j]);
                Integer end = Integer.parseInt(ends[j]);
                if (start == end) {
                    ss.append(weekday[start - 1]);
                } else {
                    ss.append(weekday[start - 1]).append(to).append(weekday[end - 1]);
                }
                ss.append(' ');
            }
            weekRule = ss.deleteCharAt(ss.length() - 1).toString();
        }

        return String.format("%02d:%02d - %02d:%02d %s"
                , getStart_h(), getStart_m()
                , getEnd_h(), getEnd_m()
                , weekRule);
    }

    public void setWeekStartAndEnd(@NonNull @Size(value = 7) boolean[] weekChoice) {
        StringBuilder sbStart = new StringBuilder();
        StringBuilder sbEnd = new StringBuilder();
        for (int i = 0, start = -1; i < weekChoice.length; i++) {
            if (weekChoice[i]) {
                if (start == -1) {
                    start = i;
                    if (sbStart.length() > 0)
                        sbStart.append(',');
                    sbStart.append(i + 1);
                }
            } else {
                if (start != -1) {
                    if (sbEnd.length() > 0)
                        sbEnd.append(',');
                    sbEnd.append(i);
                    start = -1;
                }
            }
        }
        if (weekChoice[weekChoice.length - 1]) {
            if (sbEnd.length() > 0)
                sbEnd.append(',');
            sbEnd.append(weekChoice.length);
        }

        start_w = sbStart.toString();
        end_w = sbEnd.toString();
    }

    /**
     * @return 获取星期选中状态 星期7654321
     */
    public byte getWeekDays() {
        byte value = 0;
        if (getStart_w().indexOf(',') == -1) {//连续日期
            Integer start = Integer.parseInt(getStart_w());
            Integer end = Integer.parseInt(getEnd_w());
            if (start == end) {
                value |= (1 << (start - 1));
            } else {
                for (int i = start; i <= end; ++i)
                    value |= (1 << (i - 1));
            }
            return value;
        } else {//断续日期
            String[] starts = getStart_w().split(",");
            String[] ends = getEnd_w().split(",");
            for (int j = 0; j < starts.length; j++) {
                Integer start = Integer.parseInt(starts[j]);
                Integer end = Integer.parseInt(ends[j]);
                if (start == end) {
                    value |= (1 << (start - 1));
                } else {
                    for (int i = start; i <= end; ++i)
                        value |= (1 << (i - 1));
                }
            }
            return value;
        }
    }

    @Override
    public String toString() {
        return "Level2Been{" +
                "mac='" + mac + '\'' +
                ", description='" + description + '\'' +
                ", ip='" + ip + '\'' +
                ", max_up_bandwidth=" + max_up_bandwidth +
                ", max_down_bandwidth=" + max_down_bandwidth +
                ", name='" + name + '\'' +
                ", connect_type=" + connect_type +
                ", link_time=" + link_time +
                ", max_upload=" + max_upload +
                ", max_download=" + max_download +
                ", start_h=" + start_h +
                ", start_m=" + start_m +
                ", start_w='" + start_w + '\'' +
                ", end_h=" + end_h +
                ", end_m=" + end_m +
                ", end_w='" + end_w + '\'' +
                ", state=" + state +
                '}';
    }

    public Level2Been getCopy() {
        Level2Been newItem = new Level2Been();
        for (Field declaredField : getClass().getDeclaredFields()) {
            try {
                declaredField.set(newItem, declaredField.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return newItem;
    }
}
