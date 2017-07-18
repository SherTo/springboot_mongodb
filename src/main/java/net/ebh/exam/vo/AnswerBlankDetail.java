package net.ebh.exam.vo;

import lombok.Data;
import net.ebh.exam.base.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by zkq on 2016/5/26.
 * 用户单个答题下的每个空的映射
 */
@Data
@Entity
@Table(name = "ebh_answerblankdetails")
public class AnswerBlankDetail extends BaseEntity implements Serializable {

    /**
     * 主键索引
     */
    @Id
    @GeneratedValue
    private long dbid;

    /**
     * 试题标号
     */
    private long qid;

    /**
     * 试题填空的标号
     */
    private long bid;

    /**
     * 该空的得分
     */
    private double score;

    /**
     * 用户的答案
     */
    private String content;

    /**
     * 该空的状态
     */
    private int status;


    /**
     * 该空的回答这的用户编号
     */
    private long uid = 0;

    /**
     * 关联的答题详情
     */
    public long dqid;


}
