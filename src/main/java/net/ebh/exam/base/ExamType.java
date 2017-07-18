package net.ebh.exam.base;

/**
 * Created by zkq on 2016/5/28.
 * 作业类型
 */
public enum ExamType {
    COMMON("普通作业"), SSMART("学生智能作业"), TSMART("教师智能作业"), PAPER("试卷"),EXERCISE("巩固练习");
    private String name;

    public String getName() {
        return name;
    }

    ExamType(String name) {
        this.name = name;
    }
}
