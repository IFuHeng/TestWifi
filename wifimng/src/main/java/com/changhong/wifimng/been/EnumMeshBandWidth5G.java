package com.changhong.wifimng.been;

public enum EnumMeshBandWidth5G {
    _20MHz("20MHz", 0), _40MHz("40MHz", 1), _80MHz("80MHz", 2);

    String name;
    int code;

    EnumMeshBandWidth5G(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public static EnumMeshBandWidth5G findBandWidthByPlcCode(int code) {
        for (EnumMeshBandWidth5G value : values()) {
            if (value.code == code)
                return value;
        }
        return null;
    }
}
