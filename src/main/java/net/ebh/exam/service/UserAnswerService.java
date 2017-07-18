package net.ebh.exam.service;

import net.ebh.exam.TempVo.AnswerMap;
import net.ebh.exam.TempVo.UpdateUtil;
import net.ebh.exam.base.ExamType;
import net.ebh.exam.base.QueType;
import net.ebh.exam.bean.*;
import net.ebh.exam.dao.*;
import net.ebh.exam.util.CException;
import net.ebh.exam.util.CUtil;
import net.ebh.exam.util.ErrorCode;
import net.ebh.exam.util.HMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by xh on 2017/4/18.
 */
@Service
public class UserAnswerService {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    UserAnswerDao userAnswerDao;
    @Autowired
    ExamDao examDao;
    @Autowired
    ExamCorrectService examCorrectService;
    @Autowired
    AnswerQueDetailDao answerQueDetailDao;
    @Autowired
    QuestionDao questionDao;
    @Autowired
    AnswerBlankDetailDao answerBlankDetailDao;
    @Autowired
    ExamService examService;

    /**
     * 根据uid,eid查询答题记录
     *
     * @param uid
     * @param eid
     * @return
     */
    public UserAnswer findUserAnswerByUidAndEid(Long uid, long eid) {
        List<UserAnswer> userAnswers = userAnswerDao.getUserAnswerRepository().findUserAnswerByUidAndEid(uid, eid);
        if (!ObjectUtils.isEmpty(userAnswers)) {
            return userAnswers.get(0);
        }
        return null;
    }

    /**
     * 获取答案的简要信息
     *
     * @param userAnswer
     * @return
     */
    public HMapper getSimpleInfo(UserAnswer userAnswer) {
        HMapper hMapper = new HMapper();
        if (ObjectUtils.isEmpty(userAnswer)) {
            return hMapper;
        }
        hMapper.put("aid", userAnswer.getAid());
        hMapper.put("anstotalscore", userAnswer.getAnstotalscore());
        hMapper.put("usedtime", userAnswer.getUsedtime());
        hMapper.put("ansdateline", userAnswer.getAnsdateline());
        hMapper.put("correctrat", userAnswer.getCorrectrat());
        hMapper.put("status", userAnswer.getStatus());
        hMapper.put("uid", userAnswer.getUid());
        hMapper.put("remark", userAnswer.getRemark());
        hMapper.put("data", userAnswer.getData());
        return hMapper;
    }

    /**
     * 查询某个试题的所有答题记录
     *
     * @param eid
     */
    public List<UserAnswer> getUserAnswerListByEid(long eid) {
        return mongoTemplate.find(new Query(new Criteria().and("eid").is(eid)), UserAnswer.class);
    }

    /**
     * 删除答题记录（dtag=1）
     *
     * @param userAnswer
     */
    public void deleteUserAnswer(UserAnswer userAnswer) {
        Update update = new Update().set("dtag", 1);
        Query query = new Query(new Criteria().and("aid").is(userAnswer.getAid()));
        mongoTemplate.updateFirst(query, update, UserAnswer.class);
    }

    public UserAnswer getUserAnswerByAid(long aid) {
        return userAnswerDao.getUserAnswerByAid(aid);
    }

    /**
     * 保存答题
     *
     * @param userAnswer
     * @return
     * @throws Exception
     */
    public UserAnswer doSave(UserAnswer userAnswer, Exam exam, HMapper hMapper) throws Exception {
        String remark = hMapper.getString("remark");
        userAnswer.setRemark(remark == null ? "" : remark);
        if (userAnswer.getAid() != 0) {
            UserAnswer dbUserAnswer = userAnswerDao.getUserAnswerRepository().findByAid(userAnswer.getAid());
            if (ObjectUtils.isEmpty(dbUserAnswer)) {
                throw new CException(ErrorCode.USERANSWER_ISNOT_EXIST);
            }
            if (dbUserAnswer.getStatus() == 1) {
                throw new CException(ErrorCode.NOT_AOLLOW_EDIT);
            }

            if (dbUserAnswer.getUid() != (userAnswer.getUid())) {
                throw new CException(ErrorCode.USERANSWER_UID_NOTSAME);
            }
            mergeUserAnswer(dbUserAnswer, userAnswer);
            userAnswer = dbUserAnswer;
        }
        if (userAnswer.getStatus() != 0) {
            correctAnswer(userAnswer, exam);//批改答案
        } else {
            userAnswer = userAnswerDao.saveUserAnswer(userAnswer);//保存草稿
        }
        return userAnswer;
    }

    public UserAnswer beforeSave(UserAnswer ua, List<AnswerQueDetail> answerQueDetailList) {
        if (ua.getStatus() == 1) {
            double usergetscore = 0;//double相加要转换下类型
            int statusok = 0;
            long uid = ua.getUid();
            for (AnswerQueDetail answerQueDetail : answerQueDetailList) {
                answerQueDetail.setAid(ua.getAid());
                BigDecimal bigDecimal = new BigDecimal(String.valueOf(answerQueDetail.getTotalscore())).add(new BigDecimal(usergetscore));
                usergetscore = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                answerQueDetail.setUid(uid);
                answerBlankDetailDao.getBlankListByDqid(answerQueDetail.getDqid()).forEach(answerBlankDetail -> answerBlankDetail.setUid(uid));
                if (answerQueDetail.getStatus() == 1) {
                    statusok += 1;
                }
            }
            ua.setAnstotalscore(usergetscore);
            if (answerQueDetailList.size() > 0)
                ua.setCorrectrat(statusok * 100 / answerQueDetailList.size());
        }

        if (ua.getAnsdateline() == 0L) {
            ua.setAnsdateline(CUtil.getUnixTimestamp());
        }

        //如果作业是第一份学生智能作业则显示来源作业为教师布置的作业
        Exam exam = examDao.getExamRepository().findByEid(ua.getEid());
        if (exam.getEtype() == ExamType.SSMART) {
            ua.setFromeid(exam.getFromeid());
        } else {
            ua.setFromeid(exam.getEid());
        }

        ua.setAorder(exam.getEorder());
        return ua;
    }

    /**
     * 解析答案
     *
     * @param userAnswer
     * @return
     * @throws Exception
     */
    public Map<Long, List<String>> parseAnswer(UserAnswer userAnswer) throws Exception {
        if (ObjectUtils.isEmpty(userAnswer.getData())) {
            throw new CException(ErrorCode.USERANSWER_DATA_ISNULL);
        }
        AnswerMap answerMap = HMapper.parseObject(userAnswer.getData(), AnswerMap.class);
        return answerMap.getAnswerMap();
    }

    /**
     * 校验能否进行数据持久化
     *
     * @param userAnswer
     * @throws Exception
     */
    public void canSave(UserAnswer userAnswer) throws Exception {
        if (ObjectUtils.isEmpty(userAnswer)) {
            throw new CException(ErrorCode.USERANSWER_ISNOT_EXIST);
        }
        if (StringUtils.isEmpty(userAnswer.getEid())) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }

        if (userAnswer.getUid() == 0L) {
            throw new CException(ErrorCode.USERANSWER_UID_ISNULL);
        }

        if (ObjectUtils.isEmpty(userAnswer.getData())) {
            throw new CException(ErrorCode.USERANSWER_DATA_ISNULL);
        }
    }

    /**
     * 批改答案
     *
     * @param userAnswer
     * @throws Exception
     */
    public List<AnswerQueDetail> correctAnswer(UserAnswer userAnswer, Exam exam) throws Exception {
        Map<Long, List<String>> answerMap = parseAnswer(userAnswer);
        if (ObjectUtils.isEmpty(answerMap)) {
            throw new CException(ErrorCode.ANSWERMAP_CANNOT_PARSING);
        }

        List<AnswerQueDetail> answerQueDetails = new ArrayList<>();
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }
        if (exam.getEtype() == ExamType.TSMART) {
            exam = examService.getsmartExam(exam);
        }
        List<Question> questions = examDao.getQuestionListByEid(exam.getEid());

        List<List<AnswerBlankDetail>> answerBlankDetailList = new ArrayList<>();
        for (Question question : questions) {
            if (question.getQueType() == QueType.Z || question.getQueType() == QueType.G) {
                continue;
            }
            long qid = question.getQid();
            if (!answerMap.containsKey(qid)) {
                throw new CException("存在试题没有提交qid:" + qid);
            }
            Map<String, Object> map = examCorrectService.correctQues(question, answerMap.get(qid));
            List<AnswerBlankDetail> answerBlankDetails = (List<AnswerBlankDetail>) map.get("answerBlankDetailList");
            if (!ObjectUtils.isEmpty(answerBlankDetails)) {
                answerBlankDetailList.add(answerBlankDetails);
            }
            AnswerQueDetail answerQueDetail = (AnswerQueDetail) map.get("answerQueDetail");
            answerQueDetails.add(answerQueDetail);
        }
        canSave(userAnswer);
        userAnswer = beforeSave(userAnswer, answerQueDetails);
        if (userAnswer.getAid() != 0) {
            mongoTemplate.updateFirst(new Query(new Criteria().and("aid").is(userAnswer.getAid())), UpdateUtil.buildBaseUpdate(userAnswer), UserAnswer.class);
        } else {
            userAnswer = userAnswerDao.saveUserAnswer(userAnswer);
        }
        for (AnswerQueDetail answerQueDetail : answerQueDetails) {
            answerQueDetail.setAid(userAnswer.getAid());
            answerQueDetail.setUid(userAnswer.getUid());
            AnswerQueDetail detail = answerQueDetailDao.getAnswerQueDetailRepository().save(answerQueDetail);
            if (!ObjectUtils.isEmpty(answerBlankDetailList)) {
                for (List<AnswerBlankDetail> answerBlankDetails : answerBlankDetailList) {
                    for (AnswerBlankDetail answerBlankDetail : answerBlankDetails.stream().filter(blankdetail -> blankdetail.getQid() == detail.getQid()).collect(Collectors.toList())) {
                        answerBlankDetail.setUid(detail.getUid());
                        answerBlankDetail.setDqid(detail.getDqid());
                        answerBlankDetailDao.getAnswerBlankDetailRepository().save(answerBlankDetail);

                    }
                }
            }
        }
        return answerQueDetails;
    }

    /**
     * 编辑答案的时候，提取用户传过来的数据到数据库持久化实体
     *
     * @param dbUserAnswer
     * @param userAnswer
     */
    public void mergeUserAnswer(UserAnswer dbUserAnswer, UserAnswer userAnswer) {
        if (!StringUtils.isEmpty(userAnswer.getData())) {
            dbUserAnswer.setData(userAnswer.getData());
        }
        //提取答题状态
        dbUserAnswer.setStatus(userAnswer.getStatus());
        //提取答题耗时
        dbUserAnswer.setUsedtime(userAnswer.getUsedtime());
    }

    /**
     * 获取答案的简要信息
     *
     * @param userAnswer
     * @return
     */
    public HMapper simpleInfo(UserAnswer userAnswer) {
        HMapper atmpMap = new HMapper();
        if (userAnswer == null) {
            return atmpMap;
        }
        atmpMap.put("aid", userAnswer.getAid());
        atmpMap.put("anstotalscore", userAnswer.getAnstotalscore());
        atmpMap.put("usedtime", userAnswer.getUsedtime());
        atmpMap.put("ansdateline", userAnswer.getAnsdateline());
        atmpMap.put("correctrat", userAnswer.getCorrectrat());
        atmpMap.put("status", userAnswer.getStatus());
        atmpMap.put("uid", userAnswer.getUid());
        atmpMap.put("remark", userAnswer.getRemark());
        return atmpMap;
    }

    /**
     * 根据aid获取一条数据
     */
    public UserAnswer getUserAnswer(long aid) {
        return userAnswerDao.getUserAnswerRepository().findByAid(aid);
    }

    /**
     * 获取答案的简要信息
     *
     * @param userAnswer
     * @return
     */
    public HMapper simpleInfoWithData(UserAnswer userAnswer) {
        HMapper atmpMap = simpleInfo(userAnswer);
        if (userAnswer != null) {
            atmpMap.put("data", userAnswer.getData());
        }
        return atmpMap;
    }

    /**
     * 获取答案的简要信息
     *
     * @param userAnswer
     * @return
     */
    public HMapper simpleInfoForCorrect(UserAnswer userAnswer) {
        HMapper atmpMap = simpleInfo(userAnswer);
        List<AnswerQueDetail> answerQueDetailList = answerQueDetailDao.getAnswerQueDetailListByAid(userAnswer.getAid());
        List<Map<String, Object>> answerQueDetails = answerQueDetailList.stream().collect(ArrayList::new, (list, answerQueDetail) -> {
            Map<String, Object> tmpmap = new HashMap();
            tmpmap.put("dqid", answerQueDetail.getDqid());
            tmpmap.put("qid", questionDao.getQuestionByQid(answerQueDetail.getQid()).getQid());
            tmpmap.put("totalscore", answerQueDetail.getTotalscore());
            tmpmap.put("answerBlankDetails", answerBlankDetailDao.getBlankListByDqid(answerQueDetail.getDqid()).stream().filter(answerBlankDetail -> answerBlankDetail.getUid() == userAnswer.getUid()).collect(Collectors.toList()));
            tmpmap.put("choicestr", answerQueDetail.getChoicestr());
            tmpmap.put("data", answerQueDetail.getData());
            tmpmap.put("allright", answerQueDetail.getAllright());
            tmpmap.put("status", answerQueDetail.getStatus());
            tmpmap.put("uid", answerQueDetail.getUid());
            tmpmap.put("remark", answerQueDetail.getRemark());
            tmpmap.put("markid", answerQueDetail.getMarkuid());
            list.add(tmpmap);
        }, ArrayList::addAll);
        atmpMap.put("answerQueDetails", answerQueDetails);
        return atmpMap;
    }

    /**
     * 根据筛选条件获取分页列表
     *
     * @param exam
     * @param params
     * @return
     * @throws Exception
     */
    public Page<UserAnswer> findPage(Exam exam, HMapper params) throws Exception {
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }

        if (ObjectUtils.isEmpty(params)) {
            throw new CException(ErrorCode.PARAMS_IS_NULL);
        }
        return userAnswerDao.findPage(exam, params);
    }

    public void dodelete(UserAnswer userAnswer) throws Exception {
        if (ObjectUtils.isEmpty(userAnswer)) {
            throw new CException(ErrorCode.USERANSWER_ISNOT_EXIST);
        }
        userAnswer.setDtag(1);
        userAnswerDao.getUserAnswerRepository().save(userAnswer);
    }

    public long getExamAnswerCountForTeacher(Exam exam) {
        return userAnswerDao.getExamAnswerCountForTeacher(exam);
    }

}
