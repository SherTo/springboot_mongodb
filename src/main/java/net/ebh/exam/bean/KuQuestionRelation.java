package net.ebh.exam.bean;

import lombok.Data;
import net.ebh.exam.base.BaseRelation;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Created by xh on 2016/6/1.
 * 题库关联关系
 */
@Data
public class KuQuestionRelation extends BaseRelation implements Serializable {
    /**
     * 对应题库试题
     */
    @Indexed(unique = true)
    private long kuqid;

}
