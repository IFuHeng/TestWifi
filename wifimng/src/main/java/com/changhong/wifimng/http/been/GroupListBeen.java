package com.changhong.wifimng.http.been;

import java.util.List;

public class GroupListBeen {

    private Integer currentPage;
    private Integer pageSize;
    private Integer totalPage;
    private Integer totalSize;
    private List<Group> list;

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
