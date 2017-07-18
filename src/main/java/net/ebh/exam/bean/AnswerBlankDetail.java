package net.ebh.exam.bean;

import lombok.Data;
import net.ebh.exam.TempVo.GeneratedValue;
import net.ebh.exam.base.BaseEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Created by zkq on 2016/5/26.
 * 用户单个答题下的每个空的映射
 */
@Data
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
    @Indexed(unique = true)
    private long qid;

    /**
     * 试题填空的标号
     */
    @Indexed(unique = true)
    private long bid;

    /**
     * 该空的得分
     */
    @Indexed(unique = true)
    private double score;

    /**
     * 用户的答案
     */
    private String content;

    /**
     * 该空的状态
     */
    @Indexed(unique = true)
    private int status;


    /**
     * 该空的回答这的用户编号
     */
    @Indexed(unique = true)
    private long uid = 0;

    /**
     * 关联的答题详情
     */
    @Indexed(unique = true)
    public long dqid;

}
