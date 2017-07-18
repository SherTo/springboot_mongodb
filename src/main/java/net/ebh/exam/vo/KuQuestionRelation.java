package net.ebh.exam.vo;

import lombok.Data;
import net.ebh.exam.base.BaseRelation;
import net.ebh.exam.base.RelationType;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by xh on 2016/6/1.
 * 题库关联关系
 */
@Data
@Entity
@Table(name = "ebh_kuquestionrelations")
public class KuQuestionRelation implements Serializable {
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
    @Enumerated(EnumType.STRING)
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
    /**
     * 对应题库试题
     */
    private long kuqid;
    private int dtag;
    private int version;
}
