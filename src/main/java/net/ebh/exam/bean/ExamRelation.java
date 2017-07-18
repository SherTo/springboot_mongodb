package net.ebh.exam.bean;

import lombok.Data;
import net.ebh.exam.base.BaseRelation;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Created by zkq on 2016/6/1.
 * 作业关联映射
 */
@Data
public class ExamRelation extends BaseRelation implements Serializable {

    /**
     * 对应的作业
     */
    @Indexed(unique = true)
    private long eid;
    @Indexed(unique = true)
    private long classid;
}
