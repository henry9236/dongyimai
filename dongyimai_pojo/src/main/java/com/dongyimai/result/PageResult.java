package com.dongyimai.result;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 * 包含了返回的行，与总统计个数
 */
public class PageResult implements Serializable {
    private long totalCount; //总记录数
    private List<?> rows;    //分页查询后的集合

    public PageResult() {
    }

    public PageResult(long totalCount, List<?> rows) {
        this.totalCount = totalCount;
        this.rows = rows;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public List<?> getRows() {
        return rows;
    }

    public void setRows(List<?> rows) {
        this.rows = rows;
    }
}
