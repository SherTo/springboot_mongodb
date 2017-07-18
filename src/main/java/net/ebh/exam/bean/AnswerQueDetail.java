package net.ebh.exam.bean;

import lombok.Data;
import net.ebh.exam.TempVo.GeneratedValue;
import net.ebh.exam.base.BaseEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Created by zkq on 2016/5/31.
 * 试题答题映射
 */
@Data
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
    @Indexed(unique = true)
    private long aid;

    /**
     * 关联的试题
     */
    @Indexed(unique = true)
    private long qid;

    /**
     * 用户得分
     */
    @Indexed(unique = true)
    private double totalscore;

    /**
     * 用户选项 0001 10001 10等
     */
    private String choicestr = "";

    /**
     * 试题答题是否全对
     */
    @Indexed(unique = true)
    private int allright;

    /**
     * 试题状态1表示批改完毕
     */
    @Indexed(unique = true)
    private int status;

    /**
     * 答题用户编号
     */
    @Indexed(unique = true)
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
