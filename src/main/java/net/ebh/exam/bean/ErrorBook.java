package net.ebh.exam.bean;

import lombok.Data;
import net.ebh.exam.TempVo.GeneratedValue;
import net.ebh.exam.base.BaseEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Created by zkq on 2016/6/16.
 * 错题集实体
 */
@Data
public class ErrorBook extends BaseEntity implements Serializable {

    /**
     * 错题集试题id
     */
    @Id
    @GeneratedValue
    private long errorid;

    /**
     * 错题来源试题
     */
    @Indexed(unique = true)
    private long qid;

    /**
     * 错题答题详情
     */
    @Indexed(unique = true)
    private long dqid;

    /**
     * 错题集所属用户
     */
    @Indexed(unique = true)
    private long uid = 0;

    /**
     * 试题添加到错题集的时间
     */
    private long dateline = 0;
    /**
     * 添加错题的方式默认0自动，手动1
     */
    @Indexed(unique = true)
    private int style;

}
