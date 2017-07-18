package net.ebh.exam.service;

import com.mongodb.DBObject;
import net.ebh.exam.TempVo.UpdateUtil;
import net.ebh.exam.base.QueType;
import net.ebh.exam.bean.*;
import net.ebh.exam.dao.ErrorBookDao;
import net.ebh.exam.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zkq on 2016/6/16.
 * 错题集服务
 */
@Service
public class ErrorBookService {
    @Autowired
    private ErrorBookDao errorBookDao;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    private ExamService examService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private AnswerDetailService answerQueDetailService;
    @Autowired
    private UserAnswerService userAnswerService;
    @Autowired
    private BlankService blankService;

    public ErrorBook findOneByUidAndDqidAndQid(Long uid, long dqid, long qid) {
        List<ErrorBook> errorBookList = errorBookDao.getErrorBookRepository().findErrorBookByUidAndDqidAndQid(uid, dqid, qid);
        if (errorBookList.size() > 0) {
            return errorBookList.get(0);
        }
        return null;
    }

    /**
     * 获取符合条件的错题分页信息
     *
     * @return
     */
    public void canSave(ErrorBook errorBook) throws Exception {
        if (errorBook == null) {
            throw new CException(ErrorCode.ERRORBOOK_ISNOT_EXIST);
        }
        if (errorBook.getUid() == 0L) {
            throw new CException(ErrorCode.ERRORBOOK_UID_ISNULL);
        }
        if (errorBook.getQid() == 0L) {
            throw new CException(ErrorCode.QUESTION_ISNOT_EXIST);
        }
    }

    /**
     * 指定的试题附加上答题详情加入错题集
     *
     * @param answerQueDetail
     * @param uid
     * @return
     */
    public ErrorBook doSave(AnswerQueDetail answerQueDetail, Long uid) throws Exception {
        ErrorBook errorBook = new ErrorBook();
        errorBook.setQid(answerQueDetail.getQid());
        errorBook.setDqid(answerQueDetail.getDqid());
        errorBook.setUid(uid);
        errorBook.setStyle(1);
        errorBook.setDateline(CUtil.getUnixTimestamp());
        canSave(errorBook);
        return errorBookDao.getErrorBookRepository().save(errorBook);
    }

    /**
     * 添加错题
     *
     * @param answerQueDetail
     * @return
     */
    public ErrorBook addErrorbook(AnswerQueDetail answerQueDetail) {
        ErrorBook errorBook = new ErrorBook();
        errorBook.setUid(answerQueDetail.getUid());
        errorBook.setDqid(answerQueDetail.getDqid());
        errorBook.setQid(answerQueDetail.getQid());
        errorBook.setDateline(CUtil.getUnixTimestamp());
        return save(errorBook);
    }

    /**
     * 错题分页
     *
     * @param params
     * @return
     */
    public Page<DBObject> getErrorList(HMapper params) {
        return errorBookDao.getErrorPage(params);
    }


    public ErrorBook getErrorBookByErrorId(long errorid) {
        return errorBookDao.getErrorBookRepository().findByErrorid(errorid);
    }

    public void updateErrorBook(ErrorBook errorbook) throws Exception {
        canSave(errorbook);
        errorbook.setDateline(CUtil.getUnixTimestamp());
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("errorid").is(errorbook.getErrorid());
        query.addCriteria(criteria);
        Update update = UpdateUtil.buildBaseUpdate(errorbook);
        mongoTemplate.updateFirst(query, update, ErrorBook.class);
    }

    /**
     * 保存错题
     *
     * @param errorbook
     * @return
     */
    public ErrorBook save(ErrorBook errorbook) {
        return errorBookDao.getErrorBookRepository().save(errorbook);
    }

    /**
     * 获取错题列表
     *
     * @param page
     * @param params
     * @return
     */
    public List<Map<String, Object>> getErrorMapList(Page<DBObject> page, HMapper params) {
        Long uid = params.getLong("uid");
        List<Map<String, Object>> errbookList = new ArrayList<>();
        for (DBObject obj : page.getContent()) {
            Long qid = (Long) obj.get("qid");
            Integer count = (Integer) obj.get("count");
            Map<String, Object> tmpMap = new HashMap<>();
            Question question = questionService.getQuestionByQid(qid);
            Exam exam = examService.getExamByEid(question.getEid());
            tmpMap.put("exam", exam);
            List<ErrorBook> errorBookList = errorBookDao.getErrorListByQid(qid);
            //学生错题
            if (!ObjectUtils.isEmpty(errorBookList)) {
                for (ErrorBook errorBook : errorBookList) {
                    if (errorBook.getUid() == uid) {
                        tmpMap.put("answerQueDetail", answerQueDetailService.getAnswerQueDetailMap(errorBook.getDqid()));
                        tmpMap.put("dateline", errorBook.getDateline());
                        tmpMap.put("errorid", errorBook.getErrorid());
                        tmpMap.put("uid", errorBook.getUid());
                    }
                }
            }
            tmpMap.put("question", questionService.getQuestionInfo(question));
            tmpMap.put("errorCount", count);
            errbookList.add(tmpMap);
        }
        return errbookList;
    }

    public Exam getExamByEid(Long eid) {
        return examService.getExamByEid(eid);
    }

    public List<UserAnswer> getUserAnswerCount(Long eid) {
        return userAnswerService.getUserAnswerListByEid(eid);
    }

    public Exam getSmartExam(Exam exam) {
        return examService.getsmartExam(exam);
    }

    public AnswerQueDetail getAnswerQueDetailByDqid(long dqid) {
        return answerQueDetailService.getAnswerQueDetailByDqid(dqid);
    }

    public Question getQuestionByQid(long qid) {
        return questionService.getQuestionByQid(qid);
    }

    public List<AnswerQueDetail> getAnswerDetailList(long qid) {
        return answerQueDetailService.getAnswerDetailList(qid);
    }

    public HMapper getSimpleQuestion(Question question) {
        return questionService.getSimpleInfo(question);
    }

    public Page<AnswerQueDetail> getAnswerListBypage(HMapper hMapper) throws Exception {
        return answerQueDetailService.getAnswerListBypage(hMapper);
    }

    public List<UserAnswer> getUserAnswerListByEid(long eid) {
        return userAnswerService.getUserAnswerListByEid(eid);
    }

    public List<AnswerQueDetail> getAnswerQueDetailList(long aid) {
        return answerQueDetailService.getAnswerDetailList(aid);
    }

    public List<Question> getQuestionListByEid(long eid) {
        return examService.getQuestionListByEid(eid);
    }

    public HMapper getExamSimpleInfo(long eid) {
        return examService.getExamSimpleInfo(eid);
    }

    public List<Map<String, Object>> getMapList(Map<String, List<AnswerQueDetail>> map) {
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (String key : map.keySet()) {
            Map tmpmap = new HashMap<>();
            tmpmap.put("choiceStr", key);
            tmpmap.put("count", map.get(key).size());
            list.add(tmpmap);
        }
        return list;
    }

    public List<Map<String, Object>> getAnswerList(Page<AnswerQueDetail> page) {
        List<Map<String, Object>> answerList = new ArrayList<>();
        for (AnswerQueDetail answerQueDetail : page.getContent()) {
            Question question = getQuestionByQid(answerQueDetail.getQid());
            Map<String, Object> tmpMap = new HashMap<>();
            if (question.getQueType() == QueType.D || question.getQueType() == QueType.A) {
                tmpMap.put("aid", answerQueDetail.getAid());
                tmpMap.put("qtype", question.getQueType());
                tmpMap.put("uid", answerQueDetail.getUid());
                tmpMap.put("choicestr", ChangeChoice.choiceToString(answerQueDetail.getChoicestr()));
                answerList.add(tmpMap);
            }
        }
        return answerList;
    }
}
