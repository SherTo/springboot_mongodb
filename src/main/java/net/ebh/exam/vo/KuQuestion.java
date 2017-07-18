package net.ebh.exam.vo;

import lombok.Data;
import net.ebh.exam.base.BaseEntity;
import net.ebh.exam.base.QueType;
import net.ebh.exam.util.CUtil;

import javax.persistence.*;

/**
 * Created by zkq on 2016/5/30.
 */
@Data
@Entity
@Table(name = "ebh_kuquestions")
public class KuQuestion extends BaseEntity {

    /**
     * 题库题目唯一标志
     */
    @Id
    @GeneratedValue
    private long kuqid;

    /**
     * 题库题目对应的用户编号
     */
    private long uid;

    /**
     * 题库题目对应的教室
     */
    private long crid;

    /**
     * 试题发布时间
     */
    private long dateline;

    /**
     * 试题分数
     */
    private int quescore;

    /**
     * 试题标题
     */
    private String qsubject;
    /**
     * 试题类型
     */
    @Enumerated(EnumType.STRING)
    private QueType quetype;
    /**
     * 试题发布状态
     */
    private int status;

    /**
     * 试题难度等级
     */
    private int level;

    /**
     * 试题md5识别码
     */
    private String md5code = "";

    private String data;

    private String extdata;

}
