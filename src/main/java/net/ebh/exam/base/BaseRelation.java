package net.ebh.exam.base;

import lombok.Data;
import net.ebh.exam.TempVo.GeneratedValue;
import org.springframework.data.annotation.Id;

/**
 * Created by zkq on 2016/6/1.
 * 基础关联模型(所有关联模型的父类)
 */
@Data
public class BaseRelation extends BaseEntity {

    /**
     * 关联关系唯一索引
     */
    @Id
    @GeneratedValue
    private long relationid;

    /**
     * 关联的目标id
     */
    private long tid;

    /**
     * 关联的目标类型
     */
    private RelationType ttype;

    /**
     * 关联的目标path
     */
    private String path = "";

    /**
     * 关联的目标名字
     */
    private String relationname = "";

    private String extdata;
    /**
     * 备注
     */
    private String remark;

}