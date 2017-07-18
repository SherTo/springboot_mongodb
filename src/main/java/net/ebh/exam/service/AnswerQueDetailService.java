package net.ebh.exam.service;

import net.ebh.exam.TempVo.CorrectBlank;
import net.ebh.exam.TempVo.UpdateUtil;
import net.ebh.exam.TempVo.User;
import net.ebh.exam.base.ExamType;
import net.ebh.exam.bean.*;
import net.ebh.exam.dao.*;
import net.ebh.exam.jpa.AnswerBlankDetailRepository;
import net.ebh.exam.util.CException;
import net.ebh.exam.util.CUtil;
import net.ebh.exam.util.ErrorCode;
import net.ebh.exam.util.HMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by zkq on 2016/6/6.
 * 单个题目的答题详情服务
 */
@Service
public class AnswerQueDetailService {
    @Autowired
    AnswerQueDetailDao answerQueDetailDao;
    @Autowired
    QuestionDao questionDao;
    @Autowired
    AnswerBlankDetailRepository answerBlankDetailRepository;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    UserAnswerDao userAnswerDao;
    @Autowired
    BlankDao blankDao;
    @Autowired
    ExamDao examDao;

    /**
     * 根据dqid获取答题详情
     *
     * @param dqid
     * @return
     */
    public AnswerQueDetail getById(long dqid) {
        return answerQueDetailDao.getAnswerQueDetailRepository().findByDqid(dqid);
    }

    /**
     * 保存单条答题
     *
     * @param answerQueDetail
     * @return
     * @throws Exception
     */
    public AnswerQueDetail doSave(AnswerQueDetail answerQueDetail) throws Exception {
        if (ObjectUtils.isEmpty(answerQueDetail)) {
            throw new CException(ErrorCode.AQD_ISNOT_EXIST);
        }
        mongoTemplate.updateFirst(new Query(new Criteria().and("dqid").is(answerQueDetail.getDqid())), UpdateUtil.buildBaseUpdate(answerQueDetail), AnswerQueDetail.class);
        return answerQueDetailDao.getAnswerQueDetailByDqid(answerQueDetail.getDqid());
    }


    /**
     * 批改单个学生答题详情
     *
     * @param answerQueDetail
     * @param user            批改者的用户信息 用来获取教师uid
     * @param params          批改信息，必须包含 long stuid,int status,correctList->[{dbid:1,score:300},{dbid:2,score:400}],可选remark评语
     * @return
     * @throws Exception
     */
    public AnswerQueDetail correctOne(AnswerQueDetail answerQueDetail, User user, HMapper params) throws Exception {
        String remark = params.getString("remark");

        if (ObjectUtils.isEmpty(answerQueDetail)) {
            throw new CException(ErrorCode.AQD_ISNOT_EXIST);
        }

        Long stuid = params.getLong("stuid");

        if (stuid.longValue() != answerQueDetail.getUid()) {
            throw new CException(ErrorCode.ANSWER_ISNOT_BELONG);
        }

        int quescore = questionDao.getQuestionByQid(answerQueDetail.getQid()).getQuescore();

        int status = params.getIntValue("status");
        status = status > 1 ? 1 : (status < 0 ? 0 : status);

        List<AnswerBlankDetail> dbAnswerBlankDetailSet = answerBlankDetailRepository.getAnswerBlankDetailByDqid(answerQueDetail.getDqid());
        CorrectBlank[] correctBlanks = params.getObject("correctList", CorrectBlank[].class);

        if (ObjectUtils.isEmpty(correctBlanks)) {
            throw new CException(ErrorCode.NOT_FIND_CORRECTLIST);
        }

        if (correctBlanks.length != dbAnswerBlankDetailSet.size()) {
            throw new CException(ErrorCode.ABDLANKS_NOT_MATCH);
        }

        Map<Long, CorrectBlank> answerBlankDetailMap = new HashMap<>();
        for (int i = 0, size = correctBlanks.length; i < size; i++) {
            CorrectBlank tmp = correctBlanks[i];
            Long dbid = tmp.getDbid();
            Double bscore = tmp.getScore();
            if (dbid == null || bscore == null) {
                throw new CException("单个空的dbid或者score没有检测到");
            }
            answerBlankDetailMap.put(dbid, tmp);
        }


        int stutotalscore = 0;

        for (AnswerBlankDetail answerBlankDetail : dbAnswerBlankDetailSet) {
            long dbid = answerBlankDetail.getDbid();
            if (!answerBlankDetailMap.containsKey(dbid)) {
                throw new CException(ErrorCode.ABDLANKS_NOT_MATCH);
            }
            double score = answerBlankDetailMap.get(dbid).getScore();
            answerBlankDetail.setScore(score);
            answerBlankDetail.setStatus(status);
            stutotalscore += score;
        }

        if (quescore < stutotalscore) {
            throw new CException(ErrorCode.SCORE_ISNOT_ABOVE_TOTALSCORE);
        }

        int allright = 0;
        if (stutotalscore == quescore) {
            allright = 1;
        }

        answerQueDetail.setStatus(status);
        answerQueDetail.setAllright(allright);
        answerQueDetail.setMarkuid(user.getUid());
        answerQueDetail.setRemark(remark);
        answerQueDetail.setTotalscore(stutotalscore);

        answerQueDetailDao.beforeSave(answerQueDetail, dbAnswerBlankDetailSet);
        return doSave(answerQueDetail);
    }

    public AnswerQueDetail getAnswerQueDetailById(long dqid) {
        if (ObjectUtils.isEmpty(dqid)) {
            return null;
        }
        return answerQueDetailDao.getAnswerQueDetailByDqid(dqid);
    }

    /**
     * 根据试题和筛选条件获取该试题下面的答题详情分页
     *
     * @param question 试题持久化实体
     * @param params
     * @return
     * @throws Exception
     */
    public Page<AnswerQueDetail> getPageByQuestionAndParam(Question question, HMapper params) throws Exception {
        return answerQueDetailDao.getAQDList(question, params);
    }


    public AnswerQueDetail correctQtypeXandH(AnswerQueDetail answerQueDetail, User user, HMapper hMapper) throws Exception {
        answerQueDetail.setMarkuid(user.getUid());
        UserAnswer userAnswer = userAnswerDao.getUserAnswerRepository().findByAid(answerQueDetail.getAid());
        mongoTemplate.updateFirst(new Query(new Criteria().and("dqid").is(answerQueDetail.getDqid())), UpdateUtil.buildBaseUpdate(answerQueDetail), AnswerQueDetail.class);
        beforeSave(userAnswer, hMapper);
        mongoTemplate.updateFirst(new Query(new Criteria().and("aid").is(userAnswer.getAid())), UpdateUtil.buildBaseUpdate(userAnswer), UserAnswer.class);
        return answerQueDetailDao.getAnswerQueDetailByDqid(answerQueDetail.getDqid());
    }

    public UserAnswer beforeSave(UserAnswer userAnswer, HMapper hMapper) {
        if (userAnswer.getStatus() == 1) {
            double usergetscore = 0;//double相加要转换下类型
            int statusok = 0;
            long uid = userAnswer.getUid();
            List<AnswerQueDetail> answerQueDetailList = answerQueDetailDao.getAnswerQueDetailListByAid(userAnswer.getAid());
            for (AnswerQueDetail answerQueDetail : answerQueDetailList) {
                answerQueDetail.setAid(userAnswer.getAid());
                BigDecimal bigDecimal = new BigDecimal(String.valueOf(answerQueDetail.getTotalscore())).add(new BigDecimal(usergetscore));
                usergetscore = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                answerQueDetail.setUid(uid);
                List<AnswerBlankDetail> answerBlankDetailList = answerBlankDetailRepository.getAnswerBlankDetailByDqid(answerQueDetail.getDqid());
                answerBlankDetailList.forEach(answerBlankDetail -> answerBlankDetail.setUid(uid));
                if (answerQueDetail.getStatus() == 1) {
                    statusok += 1;
                }
            }
            userAnswer.setAnstotalscore(usergetscore);
            if (answerQueDetailList.size() > 0)
                userAnswer.setCorrectrat(statusok * 100 / answerQueDetailList.size());
        }

        if (userAnswer.getAnsdateline() == 0L) {
            userAnswer.setAnsdateline(CUtil.getUnixTimestamp());
        }

        //如果作业是第一份学生智能作业则显示来源作业为教师布置的作业
        Exam exam = examDao.getExamRepository().findByEid(userAnswer.getEid());
        if (exam.getEtype() == ExamType.SSMART) {
            userAnswer.setFromeid(exam.getFromeid());
        } else {
            userAnswer.setFromeid(exam.getEid());
        }
        String remark = hMapper.getString("remark");
        userAnswer.setRemark(remark == null ? "" : remark);
        userAnswer.setAorder(exam.getEorder());
        return userAnswer;
    }

    public AnswerQueDetail correctAll(AnswerQueDetail answerQueDetail, User user, HMapper hMapper) throws Exception {
        Question question = questionDao.getQuestionByQid(answerQueDetail.getQid());
        int quescore = question.getQuescore();
        int allright = 0;
        List<AnswerBlankDetail> answerBlankDetailList = answerBlankDetailRepository.getAnswerBlankDetailByDqid(answerQueDetail.getDqid());
        CorrectBlank[] correctBlanks = HMapper.parseData2Array(answerQueDetail.getData(), "correctList", CorrectBlank[].class);

        if (ObjectUtils.isEmpty(correctBlanks)) {
            throw new CException(ErrorCode.NOT_FIND_CORRECTLIST);
        }

        if (correctBlanks.length != answerBlankDetailList.size()) {
            throw new CException(ErrorCode.ABDLANKS_NOT_MATCH);
        }

        Map<String, CorrectBlank> answerBlankDetailMap = getCorrectMap(Arrays.asList(correctBlanks), hMapper);


        double stutotalscore = 0;

        for (AnswerBlankDetail answerBlankDetail : answerBlankDetailList) {
            long dbid = answerBlankDetail.getDbid();
            if (!answerBlankDetailMap.containsKey(dbid)) {
                throw new CException(ErrorCode.ABDLANKS_NOT_MATCH);
            }
            Double score = answerBlankDetailMap.get(dbid).getScore();
            answerBlankDetail.setScore(score);
            BigDecimal bigDecimal = new BigDecimal(String.valueOf(stutotalscore)).add(new BigDecimal(score));
            stutotalscore = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        if (quescore < stutotalscore) {
            stutotalscore = quescore;
        }
        if (stutotalscore == quescore) {
            allright = 1;
        }
        answerQueDetail.setTotalscore(stutotalscore);
        answerQueDetail.setAllright(allright);
        answerQueDetail.setMarkuid(user.getUid());
        answerQueDetailDao.beforeSave(answerQueDetail, answerBlankDetailList);
        return doSave(answerQueDetail);
    }

    public Map getCorrectMap(List<CorrectBlank> correctBlanks, HMapper hMapper) throws Exception {
        Map<Long, CorrectBlank> answerBlankDetailMap = new HashMap<>();
        for (int i = 0, size = correctBlanks.size(); i < size; i++) {
            CorrectBlank tmp = correctBlanks.get(i);
            Long dbid = tmp.getDbid();
            Double bscore = tmp.getScore();
            if (dbid == null || bscore == null) {
                throw new CException("单个空的dbid或者score没有检测到");
            }
            answerBlankDetailMap.put(dbid, tmp);
        }
        return answerBlankDetailMap;
    }

    /**
     * 获取答题分页
     *
     * @param hMapper
     * @return
     * @throws Exception
     */

    public Page<AnswerQueDetail> getAnswerListBypage(HMapper hMapper) throws Exception {
        Query query = new Query();
        Criteria criteria = new Criteria();

        //删除标志判断
        Integer dtag = hMapper.getInteger("dtag");
        if (dtag == null) {
            dtag = 0;
        }
        criteria.and("dtag").is(dtag);
        String choicestr = hMapper.getString("choicestr");
        if (!choicestr.equals("") && choicestr != null) {
            criteria.and("choicestr").is(choicestr);
        }
        String qid = hMapper.getString("qid");
        if (!StringUtils.isEmpty(qid)) {
            criteria.and("qid").is(qid);
        }
        query.addCriteria(criteria);
        long count = mongoTemplate.count(query, AnswerQueDetail.class);
        List<AnswerQueDetail> answerQueDetailList = mongoTemplate.find(query.with(hMapper.parsePage()), AnswerQueDetail.class);
        return new PageImpl<>(answerQueDetailList, hMapper.parsePage(), count);

    }

    public List<AnswerQueDetail> getAnswerQueDetailListByQid(long qid) {
        return answerQueDetailDao.getAnswerQueDetailListByQid(qid);
    }
}
