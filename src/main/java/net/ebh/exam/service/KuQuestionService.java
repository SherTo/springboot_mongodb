package net.ebh.exam.service;

import net.ebh.exam.base.QueType;
import net.ebh.exam.bean.KuQuestion;
import net.ebh.exam.bean.Question;
import net.ebh.exam.dao.KuQuestionDao;
import net.ebh.exam.dao.KuQuestionRelationDao;
import net.ebh.exam.dao.QuestionDao;
import net.ebh.exam.util.CException;
import net.ebh.exam.util.CUtil;
import net.ebh.exam.util.ErrorCode;
import net.ebh.exam.util.HMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Created by admin on 2017/2/21.
 */
@Service
public class KuQuestionService {
    @Autowired
    KuQuestionDao kuQuestionDao;
    @Autowired
    KuQuestionRelationDao kuQuestionRelationDao;
    @Autowired
    private QuestionDao questionDao;

    public KuQuestion saveKuquestion(KuQuestion kuQuestion) {
        return kuQuestionDao.saveKuquestion(kuQuestion);
    }

    /**
     * 将指定试题添加到题库,MD5序列相同则不重复添加
     *
     * @param qid 试题编号
     * @param uid 添加之后试题的uid
     * @return
     * @throws Exception
     */
    public KuQuestion addKuFromQuestion(long qid, long uid) throws Exception {
        Question question = questionDao.getQuestionByQid(qid);
        if (ObjectUtils.isEmpty(question)) {
            throw new CException(ErrorCode.QUESTION_ISNOT_EXIST);
        }
        KuQuestion dbKuQuestion = kuQuestionDao.getKuQuestionRepository().findByMd5codeAndUidAndCrid(question.getMd5code(), uid, question.getCrid());
        if (!ObjectUtils.isEmpty(dbKuQuestion) && dbKuQuestion.getDtag() == 0) {
            return dbKuQuestion;
        }

        if (!ObjectUtils.isEmpty(dbKuQuestion) && dbKuQuestion.getDtag() == 1) {
            dbKuQuestion.setDtag(0);
        } else {
            dbKuQuestion = new KuQuestion();
            dbKuQuestion.setQsubject(question.getQsubject());
            dbKuQuestion.setCrid(question.getCrid());
            dbKuQuestion.setData(question.getData());
            dbKuQuestion.setLevel(question.getLevel());
            dbKuQuestion.setMd5code(question.getMd5code());
            dbKuQuestion.setQueType(question.getQueType());
            dbKuQuestion.setQuescore(question.getQuescore());
            dbKuQuestion.setExtdata(question.getExtdata());
            dbKuQuestion.setUid(uid);
            dbKuQuestion.setStatus(1);
            dbKuQuestion.setDateline(CUtil.getUnixTimestamp());
        }
        return kuQuestionDao.SaveKu(dbKuQuestion);
    }

    public KuQuestion findById(long kuqid) {
        return kuQuestionDao.getKuQuestionRepository().findByKuqid(kuqid);
    }


    /**
     * 从题库删除(置换标志位)
     *
     * @param kuqid
     */
    public void doDelete(long kuqid) {
        if (kuqid != 0) {
            kuQuestionDao.deleteKu(kuqid);
        }
    }

    /**
     * 保存试题到题库
     *
     * @param kuQuestion
     * @return
     * @throws Exception
     */
    public KuQuestion doSave(KuQuestion kuQuestion) throws Exception {
        if (!ObjectUtils.isEmpty(kuQuestion.getKuqid())) {
            KuQuestion dbKuQuestion = kuQuestionDao.getKuQuestionRepository().findByKuqid(kuQuestion.getKuqid());
            if (ObjectUtils.isEmpty(dbKuQuestion)) {
                throw new CException(ErrorCode.NOT_FIND_KUQUESTION);
            }
            if (dbKuQuestion.getUid() != kuQuestion.getUid()) {
                throw new CException(ErrorCode.KUQUESTION_ISNOT_BELONG);
            }
            if (dbKuQuestion.getCrid() != kuQuestion.getCrid()) {
                throw new CException(ErrorCode.CRID_ISNOT_SAME);
            }

            mergeKuQuestion(dbKuQuestion, kuQuestion);
            kuQuestion = dbKuQuestion;
        }
        canSave(kuQuestion);
        if (kuQuestion.getStatus() != 1) {
            kuQuestion.setStatus(1);
        }
        return kuQuestionDao.SaveKu(kuQuestion);
    }

    /**
     * 编辑的时候合并数据(非持久化试题合并到数据库持久化实体)
     *
     * @param dbKuquestion
     * @param kuQuestion
     */
    public void mergeKuQuestion(KuQuestion dbKuquestion, KuQuestion kuQuestion) {
        if (!StringUtils.isEmpty(kuQuestion.getData())) {
            dbKuquestion.setData(kuQuestion.getData());
        }

        if (!StringUtils.isEmpty(kuQuestion.getExtdata())) {
            dbKuquestion.setExtdata(kuQuestion.getExtdata());
        }
        dbKuquestion.setQsubject(kuQuestion.getQsubject());
        dbKuquestion.setLevel(kuQuestion.getLevel());
        dbKuquestion.setStatus(kuQuestion.getStatus());
        dbKuquestion.setQuescore(kuQuestion.getQuescore());
        dbKuquestion.setQueType(kuQuestion.getQueType());
        if (dbKuquestion.getDateline() == 0L) {
            dbKuquestion.setDateline(CUtil.getUnixTimestamp());
        }
        if (!ObjectUtils.isEmpty(kuQuestion.getDateline())) {
            dbKuquestion.setDateline(kuQuestion.getDateline());
        } else if (ObjectUtils.isEmpty(kuQuestion)) {
            dbKuquestion.setDateline(CUtil.getUnixTimestamp());
        }
    }

    /**
     * 判断试题是否可以保存
     *
     * @param kuQuestion
     * @throws Exception
     */
    public void canSave(KuQuestion kuQuestion) throws Exception {
        if (kuQuestion == null) {
            throw new CException(ErrorCode.QUESTION_ISNOT_EXIST);
        }

        //判断试题的题目
        if (StringUtils.isEmpty(kuQuestion.getQsubject())) {
            throw new CException(ErrorCode.KUQUESTION_QSUBJECT_ISNULL);
        }

        if (kuQuestion.getQuescore() == 0) {
            throw new CException(ErrorCode.SCORE_ISNOT_ZORE);
        }

        //判断试题的题目
        if (StringUtils.isEmpty(kuQuestion.getData())) {
            throw new CException(ErrorCode.QUESTION_DATA_ISNULL);
        }

        if (ObjectUtils.isEmpty(kuQuestion.getQueType())) {
            throw new CException(ErrorCode.KUQUESTION_QTYPE_ISNULL);
        }

        if (ObjectUtils.isEmpty(kuQuestion.getCrid())) {
            throw new CException(ErrorCode.CRID_IS_NULL);
        }

        if (kuQuestion.getDateline() == 0L) {
            kuQuestion.setDateline(CUtil.getUnixTimestamp());
        }

        if (kuQuestion.getUid() == 0L) {
            throw new CException(ErrorCode.KUQUESTION_UID_IS_NULL);
        }
    }

    /**
     * 获取题库分页
     *
     * @param params
     * @return
     */
    public Page<KuQuestion> getPageForKu(HMapper params) {
        return kuQuestionDao.getPageForKu(params);
    }

    /**
     * 判断指定md5code的试题是否已经加入到教师的题库
     *
     * @param params
     * @return
     */
    public KuQuestion ifinku(HMapper params) {
        if (params == null) {
            return null;
        }
        return kuQuestionDao.ifinku(params);
    }

    /**
     * 获取题库所有题型
     *
     * @return
     */
    public List<QueType> getKuquestionTypeList() {

        return kuQuestionDao.getKuquestionTypeList();
    }

    public Long kufenxi(HMapper hMapper) {
        return kuQuestionDao.kufenxi(hMapper);
    }


}
