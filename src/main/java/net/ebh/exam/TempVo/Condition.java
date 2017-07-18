package net.ebh.exam.TempVo;

/**
 * Created by zkq on 2016/6/2.
 * 智能作业条件模型
 */
public class Condition {
    private String quetype;
    private String ttype;
    private long tid;
    private int level;
    private int quescore;
    private int quecount;
    private String relationname ;
    private String path;
    private String chapterstr;

    public String getQuetype() {
        return quetype;
    }

    public void setQuetype(String quetype) {
        this.quetype = quetype;
    }

    public String getTtype() {
        return ttype;
    }

    public void setTtype(String ttype) {
        this.ttype = ttype;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getQuescore() {
        return quescore;
    }

    public void setQuescore(int quescore) {
        this.quescore = quescore;
    }

    public int getQuecount() {
        return quecount;
    }

    public void setQuecount(int quecount) {
        this.quecount = quecount;
    }

    public String getRelationname() {
        return relationname;
    }

    public void setRelationname(String relationname) {
        this.relationname = relationname;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getChapterstr() {
        return chapterstr;
    }

    public void setChapterstr(String chapterstr) {
        this.chapterstr = chapterstr;
    }
}
