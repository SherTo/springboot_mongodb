package net.ebh.exam.base;

/**
 * Created by zkq on 2016/5/23.
 * 关联关系枚举
 */
public enum RelationType {
    FOLDER("课程"), COURSE("课件"), CHAPTER("知识点"),CLASS("班级"), EXAM("作业"), QUESTION("试题"), KUQUESTION("题库试题"),ATTACH("附件"),OTHER("其它");
    private String name;

    public String getName() {
        return name;
    }

    RelationType(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
