package com.changhong.wifimng.been.status;

import android.os.Parcel;
import android.os.Parcelable;

import com.changhong.wifimng.been.BaseResponseBeen;
import com.google.gson.Gson;

public class RequireAndResponseBeen extends BaseResponseBeen implements Parcelable {


    /**
     * 1为WEB, 2为APP，3为其他
     */
    private Integer src_type = 1;

    /**
     * 上电的秒数
     */
    private Integer uptime;
    private String wan_ip;
    /**
     * 单位：bits/s
     */
    private Integer upstream_rate;
    /**
     * 单位：bits/s
     */
    private Integer downstream_rate;
    /**
     * bit0: 有线;bit1:2.4G;bit2:5G
     * 如7表示LAN具有有线、2.4G和5G
     */
    private Integer lan_interface;

    /**
     * Integer	组网required，其他不必要	0：表示等待状态，未组网；
     * 3：表示主机
     * 4：表示子机
     */
    private Integer mesh_client;

    /**
     * 上网时长的秒数
     */
    private Integer network_time;

    private RequireAndResponseBeen(Parcel in) {
        if (in.readByte() == 0) {
            src_type = null;
        } else {
            src_type = in.readInt();
        }
        if (in.readByte() == 0) {
            uptime = null;
        } else {
            uptime = in.readInt();
        }
        wan_ip = in.readString();
        if (in.readByte() == 0) {
            upstream_rate = null;
        } else {
            upstream_rate = in.readInt();
        }
        if (in.readByte() == 0) {
            downstream_rate = null;
        } else {
            downstream_rate = in.readInt();
        }
        if (in.readByte() == 0) {
            lan_interface = null;
        } else {
            lan_interface = in.readInt();
        }
        if (in.readByte() == 0) {
            mesh_client = null;
        } else {
            mesh_client = in.readInt();
        }
        if (in.readByte() == 0) {
            network_time = null;
        } else {
            network_time = in.readInt();
        }
    }

    public static final Creator<RequireAndResponseBeen> CREATOR = new Creator<RequireAndResponseBeen>() {
        @Override
        public RequireAndResponseBeen createFromParcel(Parcel in) {
            return new RequireAndResponseBeen(in);
        }

        @Override
        public RequireAndResponseBeen[] newArray(int size) {
            return new RequireAndResponseBeen[size];
        }
    };

    @Override
    public String toString() {
        return "RequireAndResponseBeen{" +
                "src_type=" + src_type +
                ", err_code=" + err_code +
                ", message='" + message + '\'' +
                ", waite_time=" + waite_time +
                ", uptime=" + uptime +
                ", wan_ip='" + wan_ip + '\'' +
                ", upstream_rate=" + upstream_rate +
                ", downstream_rate=" + downstream_rate +
                ", lan_interface=" + lan_interface +
                ", mesh_client=" + mesh_client +
                ", network_time=" + network_time +
                '}';
    }

    public String toJsonString() {
        return new Gson().toJson(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (src_type == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(src_type);
        }
        if (uptime == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(uptime);
        }
        dest.writeString(wan_ip);
        if (upstream_rate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(upstream_rate);
        }
        if (downstream_rate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(downstream_rate);
        }
        if (lan_interface == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(lan_interface);
        }
        if (mesh_client == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(mesh_client);
        }
        if (network_time == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(network_time);
        }
    }

    public Integer getSrc_type() {
        return src_type;
    }

    public Integer getUptime() {
        return uptime;
    }

    public String getWan_ip() {
        return wan_ip;
    }

    public Integer getUpstream_rate() {
        return upstream_rate;
    }

    public Integer getDownstream_rate() {
        return downstream_rate;
    }

    public Integer getLan_interface() {
        return lan_interface;
    }

    public Integer getMesh_client() {
        return mesh_client;
    }

    public Integer getNetwork_time() {
        return network_time;
    }

    public RequireAndResponseBeen() {
    }

    public boolean has2_4g() {
        return lan_interface != null && ((lan_interface & 0x2) != 0);
    }

    public boolean hasLan() {
        return lan_interface != null && ((lan_interface & 0x1) != 0);
    }

    public boolean has5g() {
        return lan_interface != null && ((lan_interface & 0x4) != 0);
    }

}
