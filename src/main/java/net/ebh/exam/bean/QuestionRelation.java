package net.ebh.exam.bean;

import lombok.Data;
import net.ebh.exam.base.BaseRelation;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Created by zkq on 2016/6/1.
 * 试题关联关系映射
 */
@Data
public class QuestionRelation extends BaseRelation implements Serializable {
    /**
     * 对应的试题
     */
    @Indexed(unique = true)
    private long qid;
}
