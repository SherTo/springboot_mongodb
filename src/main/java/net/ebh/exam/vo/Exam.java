package net.ebh.exam.vo;

/**
 * Created by xh on 2017/4/18.
 */

import lombok.Data;
import net.ebh.exam.base.ExamType;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by xh on 2017/4/18.
 * 作业实体
 */
@Data
@Entity
@Table(name = "ebh_exams")
public class Exam implements Serializable {

    /**
     * 作业唯一编号
     */
    @Id
    @GeneratedValue
    private long eid;

    /**
     * 作业标题
     */
    private String esubject;

    /**
     * 作业发布时间
     */
    private long dateline;

    /**
     * 作业crid
     */
    private long crid;

    /**
     * 作业发布人的uid
     */
    private long uid;

    /**
     * 作业类型
     */
    @Enumerated(EnumType.STRING)
    private ExamType etype;
    /**
     * 试卷答题限时
     */
    private int limittime;
    /**
     * 数据包，用于展开数据(status为1则会根据data数据展开数据)
     */
    private String data = "";
    /**
     * 试卷状态
     */
    private int status;

    /**
     * 试卷总分
     */
    private int examtotalscore;

    /**
     * 作业来源 (只有教师布置的智能作业生成的学生作业才会有值)
     */
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
     * 乐观锁版本号
     */
    private long version = 0;

    /**
     * 删除标志 0表示正常，1表示删除了
     */
    private int dtag = 0;

    private int isclass;
}

