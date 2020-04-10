package com.changhong.wifimng.http.been;

import java.util.List;

public class FamilyMemberListBeen {

    Integer currentPage;
    Integer pageSize;
    Integer totalPage;
    Integer totalSize;
    List<UserInfo> list;


    public Integer getCurrentPage() {
        return currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public Integer getTotalSize() {
        return totalSize;
    }

    public List<UserInfo> getList() {
        return list;
    }

    @Override
    public String toString() {
        return "FamilyMemberListBeen{" +
                "currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", totalPage=" + totalPage +
                ", totalSize=" + totalSize +
                ", list=" + list +
                '}';
    }
}
