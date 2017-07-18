package net.ebh.exam.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import net.ebh.exam.base.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by xh on 2016/5/25.
 * 用户针对试卷的答题记录
 */
@Data
@Entity
@Table(name = "ebh_useranswers")
public class UserAnswer extends BaseEntity implements Serializable {

    /**
     * 答题编号
     */
    @Id
    @GeneratedValue
    private long aid;

    /**
     * 答题用户id
     */
    private long uid;

    /**
     * 答题关联的作业
     */
    private long eid;
    private long fromeid;

    /**
     * 答题得分
     */
    private double anstotalscore;

    /**
     * 答题时间
     */
    private long ansdateline;

    /**
     * 状态
     */
    private int status;

    /**
     * 答题耗时
     */
    private int usedtime;

    /**
     * 批改进度
     */
    private int correctrat = 0;

    /**
     * 答案序号(对应eorder)
     */
    private int aorder = 0;
    @JsonIgnore
    private String data;
    /**
     * 整张试卷评语
     */
    @JsonIgnore
    public String remark = "";


}

