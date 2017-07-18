package net.ebh.exam.vo;

import lombok.Data;
import net.ebh.exam.base.BaseEntity;
import net.ebh.exam.base.QueType;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by zkq on 2016/5/18.
 */
@Data
@Entity
@Table(name = "ebh_questions")
public class Question extends BaseEntity implements Serializable {

    /**
     * 试题唯一编号
     */
    @Id
    @GeneratedValue
    private long qid;

    /**
     * 试题发布人的uid
     */
    private long uid;

    /**
     * 试题所属教室
     */
    private long crid;

    /**
     * 试题发布时间
     */
    private long dateline;

    /**
     * 试题分值
     */
    private int quescore;

    /**
     * 试题标题
     */
    private String qsubject;

    /**
     * 试题所属的作业映射
     */
    private long eid;

    /**
     * 数据包，status为1则展开该数据包
     */
    private String data;
    /**
     * 试题的类型
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
     * 试题md5序列
     */
    private String md5code = "";
    /**
     * 试题额外数据,系统不进行处理
     */
    private String extdata = "";

    /**
     * 选择题，或者判断题简明答案 ，比如00110 表示选择cd,1001表示选择ad
     */
    private String choicestr = "";

}
