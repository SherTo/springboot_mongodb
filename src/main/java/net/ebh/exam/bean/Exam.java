package net.ebh.exam.bean;

/**
 * Created by xh on 2017/4/18.
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.ebh.exam.TempVo.GeneratedValue;
import net.ebh.exam.base.BaseEntity;
import net.ebh.exam.base.ExamType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Created by xh on 2017/4/18.
 * 作业实体
 */
//@CompoundIndexes({
//        //复合索引
////        @CompoundIndexes()
//})
@Data
public class Exam extends BaseEntity implements Serializable {

    /**
     * 作业唯一编号
     */
    @Id
    @GeneratedValue
    private long eid;

    /**
     * 作业标题
     */
    @Indexed(unique = true)
    private String esubject;

    /**
     * 作业发布时间
     */
    @Indexed(unique = true)
    private long dateline;

    /**
     * 作业crid
     */
    @Indexed(unique = true)
    private long crid;

    /**
     * 作业发布人的uid
     */
    @Indexed(unique = true)
    private long uid;

    /**
     * 作业类型
     */
    @Indexed(unique = true)
    private ExamType etype;
    /**
     * 试卷答题限时
     */
    private int limittime;
    /**
     * 数据包，用于展开数据(status为1则会根据data数据展开数据)
     */
    @JsonIgnore
    private String data = "";
    /**
     * 试卷状态
     */
    @Indexed(unique = true)
    private int status;

    /**
     * 试卷总分
     */
    @Indexed(unique = true)
    private int examtotalscore;

    /**
     * 作业来源 (只有教师布置的智能作业生成的学生作业才会有值)
     */
    @Indexed(unique = true)
    private long fromeid;
    /**
     * 主观题是否允许学生自主批改
     */
    private int stucancorrect;

    /**
     * 作业开放开始时间
     */
    private long examstarttime;

    /**
     * 作业开放结束时间
     */
    private long examendtime;

    /**
     * 答案开放开始时间
     */
    private long ansstarttime;

    /**
     * 答案开放截止时间
     */
    private long ansendtime;

    /**
     * 是否可以再次组卷
     */
    private int canreexam;

    /**
     * 允许推送错题相关
     */
    private int canpusherror;

    /**
     * 作业排序(学生根据教师的智能作业生成的试卷才有值)
     */
    private int eorder = 0;
    /**
     * 作业布置类型(家庭作业，课堂作业，考试)
     */
    private String estype;
    /**
     * 是否关联班级（1是，0否）
     */
    private long isclass;
}

