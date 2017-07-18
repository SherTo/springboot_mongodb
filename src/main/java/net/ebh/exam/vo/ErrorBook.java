package net.ebh.exam.vo;

import lombok.Data;
import net.ebh.exam.base.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by zkq on 2016/6/16.
 * 错题集实体
 */
@Data
@Entity
@Table(name = "ebh_errorbooks")
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
    private long qid;

    /**
     * 错题答题详情
     */
    private long dqid;

    /**
     * 错题集所属用户
     */
    private long uid = 0;

    /**
     * 试题添加到错题集的时间
     */
    private long dateline = 0;
    /**
     * 添加错题的方式默认0自动，手动1
     */
    private int style;

}
