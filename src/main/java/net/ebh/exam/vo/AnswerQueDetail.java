package net.ebh.exam.vo;

import lombok.Data;
import net.ebh.exam.base.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by zkq on 2016/5/31.
 * 试题答题映射
 */
@Data
@Entity
@Table(name = "ebh_answerquedetails")
public class AnswerQueDetail extends BaseEntity implements Serializable {

    /**
     * 主键索引
     */
    @Id
    @GeneratedValue
    private long dqid;

    /**
     * 关联的答案
     */
    private long aid;

    /**
     * 关联的试题
     */
    private long qid;

    /**
     * 用户得分
     */
    private double totalscore;

    /**
     * 用户选项 0001 10001 10等
     */
    private String choicestr = "";

    /**
     * 试题答题是否全对
     */
    private int allright;

    /**
     * 试题状态1表示批改完毕
     */
    private int status;

    /**
     * 答题用户编号
     */
    private long uid = 0L;

    /**
     * 教师评语
     */
    private String remark = "";

    /**
     * 批改该答案的教师uid
     */
    private long markuid = 0L;
    /**
     * data数据
     */
    private String data;


}
