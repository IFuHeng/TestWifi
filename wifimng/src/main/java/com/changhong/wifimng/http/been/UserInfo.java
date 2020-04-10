package com.changhong.wifimng.http.been;

/**
 * 用户信息
 */
public class UserInfo {
    private String uuid;
    private String username;
    private String name;
    private String email;
    private String phoneNumber;

    public String getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    Boolean isAccept;
    public Boolean getAccept() {
        return isAccept;
    }

    public void setAccept(Boolean accept) {
        isAccept = accept;
    }

    @Override
    public String toString() {
        return "FamilyMemberBeen{" +
                "uuid='" + uuid + '\'' +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
