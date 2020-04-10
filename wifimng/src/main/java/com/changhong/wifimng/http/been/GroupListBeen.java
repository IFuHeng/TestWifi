package com.changhong.wifimng.http.been;

import java.util.List;

public class GroupListBeen {

    Integer currentPage;
    Integer pageSize;
    Integer totalPage;
    Integer totalSize;
    List<Group> list;

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

    public List<Group> getList() {
        return list;
    }
}
