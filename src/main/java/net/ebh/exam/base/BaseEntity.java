package net.ebh.exam.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * Created by Xh on 2016/6/17.
 */
@Data
public class BaseEntity {
    /**
     * 乐观锁版本号
     */
    @JsonIgnore
    private long version = 0;

    /**
     * 删除标志 0表示正常，1表示删除了
     */
    @JsonIgnore
    private int dtag = 0;

}
