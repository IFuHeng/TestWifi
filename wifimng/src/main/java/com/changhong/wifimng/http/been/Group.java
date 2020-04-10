package com.changhong.wifimng.http.been;

public class Group {
    String userId;
    String groupName;
    String uuid;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserId() {
        return userId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return "Group{" +
                "userId='" + userId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
