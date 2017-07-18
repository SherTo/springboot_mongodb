package net.ebh.exam.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import net.ebh.exam.TempVo.GeneratedValue;
import net.ebh.exam.base.BaseEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by xh on 2016/5/25.
 * 用户针对试卷的答题记录
 */
@Data
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
    @Indexed(unique = true)
    private long uid;

    /**
     * 答题关联的作业
     */
    @Indexed(unique = true)
    private long eid;
    @Indexed(unique = true)
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
    @Indexed(unique = true)
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

