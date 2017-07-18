package net.ebh.exam.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;


/**
 * Created by xh on 2017/4/15.
 */
public class PageInfo implements Serializable, Pageable {
    // 当前页
    private Integer pagenumber = 1;
    // 当前页面条数
    private Integer pagesize = 20;
    //排序条件
    private Sort sort;

    public Integer getPagenumber() {
        return pagenumber;
    }

    public void setPagenumber(Integer pagenumber) {
        this.pagenumber = pagenumber;
    }

    public Integer getPagesize() {
        return pagesize;
    }

    public void setPagesize(Integer pagesize) {
        this.pagesize = pagesize;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    @Override
    public int getPageNumber() {
        return getPagenumber();
    }

    @Override
    public int getPageSize() {
        return getPagesize();
    }

    @Override
    public int getOffset() {
        return (getPagenumber() - 1) * getPagesize();
    }

    @Override
    public Pageable next() {
        return null;
    }

    @Override
    public Pageable previousOrFirst() {
        return null;
    }

    @Override
    public Pageable first() {
        return null;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

}

