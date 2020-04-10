package com.changhong.wifimng.been;

import com.changhong.wifimng.R;

public enum DeviceType {
    BWR("BWR-510", "15", R.drawable.ic_mesh,R.string.bwr510,R.drawable.ic_distribute_router),
    R2s("R2s", "14", R.drawable.ic_router,R.string.R2s,R.drawable.ic_dualband_router),
    PLC("PN200", "4", R.drawable.ic_plc,R.string.plc,R.drawable.ic_homeplug);
    private String name;

    public String getTypeOnCloud() {
        return typeOnCloud;
    }

    private String typeOnCloud;

    private int iconResId;
    private int nameResId;
    private int thumbResId;

    DeviceType(String n, String typeOnCloud, int resId,int nameResId,int thumbResId) {
        name = n;
        this.typeOnCloud = typeOnCloud;
        iconResId = resId;
        this.nameResId = nameResId;
        this.thumbResId = thumbResId;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public static DeviceType getDeviceTypeFromName(String deviceType) {
        for (DeviceType value : values()) {
            if (value.getName().equalsIgnoreCase(deviceType))
                return value;
        }
        return null;
    }

    public static DeviceType getDeviceTypeByCloudCode(String cloudCode) {
        for (DeviceType value : values()) {
            if (value.getTypeOnCloud().equalsIgnoreCase(cloudCode))
                return value;
        }
        return null;
    }

    public int getNameResId() {
        return nameResId;
    }

    public int getThumbResId() {
        return thumbResId;
    }
}
