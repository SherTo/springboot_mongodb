package net.ebh.exam.bean;

import lombok.Data;
import net.ebh.exam.TempVo.GeneratedValue;
import net.ebh.exam.base.BaseEntity;
import net.ebh.exam.base.QueType;
import net.ebh.exam.util.CUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by zkq on 2016/5/30.
 */
@Data
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
    @Indexed(unique = true)
    private long uid;

    /**
     * 题库题目对应的教室
     */
    @Indexed(unique = true)
    private long crid;

    /**
     * 试题发布时间
     */
    private long dateline;

    /**
     * 试题分数
     */
    @Indexed(unique = true)
    private int quescore;

    /**
     * 试题标题
     */
    private String qsubject;
    /**
     * 试题类型
     */
    @Indexed(unique = true)
    private QueType queType;
    /**
     * 试题发布状态
     */
    @Indexed(unique = true)
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

    public void beforeSave() {
        StringBuffer sb = new StringBuffer();
        sb.append(getData()).append(getQsubject()).append(getQueType()).append(getExtdata());
        setMd5code(CUtil.MD5(sb.toString()));
    }
}
