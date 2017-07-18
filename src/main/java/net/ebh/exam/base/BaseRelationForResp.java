package net.ebh.exam.base;

import lombok.Data;

/**
 * Created by xh on 2017/4/14.
 * 用于返回通用关系集合
 */
@Data
public class BaseRelationForResp {

    private long tid;

    private String ttype;

    private String path = "";

    private String relationname = "";

    private long classid;

    public String extdata = "";

}