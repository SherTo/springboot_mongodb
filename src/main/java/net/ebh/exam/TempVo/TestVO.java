package net.ebh.exam.TempVo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by xh on 2017/5/4.
 */
@Document
@Data
public class TestVO {
    @Id
    private String id;
    @GeneratedValue
    private long uid;
    private String name;
}
