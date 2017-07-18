package net.ebh.exam.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.ebh.exam.base.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "ebh_blanks")
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
    private int isanswer;

    /**
     * 该空得分
     */
    private double score;

    /**
     * 该空属于的题目
     */
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
