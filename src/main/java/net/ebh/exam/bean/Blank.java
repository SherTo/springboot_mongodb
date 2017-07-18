package net.ebh.exam.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.ebh.exam.TempVo.GeneratedValue;
import net.ebh.exam.base.BaseEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
public class Blank extends BaseEntity implements Serializable {

    /**
     * 题空标志
     */
    @Id
    @GeneratedValue
    private long bid;

    /**
     * 空或者选项内容
     */
    private String bsubject;

    /**
     * 空或者选项是否是正确答案
     */
    @Indexed(unique = true)
    private int isanswer;

    /**
     * 该空得分
     */
    @Indexed(unique = true)
    private double score;

    /**
     * 该空属于的题目
     */
    @Indexed(unique = true)
    private long qid;

    @JsonProperty("isanswer")
    @JSONField(name = "isanswer")
    public String getIsAnswer() {
        if (1 == isanswer) {
            return "1";
        } else {
            return "0";
        }
    }

    @JsonIgnore
    public int isanswer() {
        return isanswer;
    }
}
