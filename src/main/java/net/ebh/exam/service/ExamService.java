package net.ebh.exam.service;

import net.ebh.exam.TempVo.Condition;
import net.ebh.exam.TempVo.QueMap;
import net.ebh.exam.TempVo.UpdateUtil;
import net.ebh.exam.TempVo.User;
import net.ebh.exam.base.BaseRelationForResp;
import net.ebh.exam.base.ExamType;
import net.ebh.exam.base.QueType;
import net.ebh.exam.base.RelationType;
import net.ebh.exam.bean.*;
import net.ebh.exam.dao.*;
import net.ebh.exam.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by admin on 2016/12/30.
 */
@Service
public class ExamService {
    @Autowired
    ExamDao examDao;

    public ExamDao getExamDao() {
        return examDao;
    }

    @Autowired
    ExamRelationDao examRelationDao;
    @Autowired
    QuestionDao questionDao;
    @Autowired
    QuestionRelationDao questionRelationDao;
    @Autowired
    QuestionService questionService;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    KuQuestionDao kuQuestionDao;
    @Autowired
    UserAnswerService userAnswerService;
    @Autowired
    AnswerDetailService answerDetailService;
    @Autowired
    BlankService blankService;
    @Autowired
    AnswerBlankDetailService answerBlankDetailService;

    /**
     * 根据ID获取作业实体
     *
     * @param eid
     * @return
     */
    public Exam findExamByEid(long eid) {
        return examDao.getExamRepository().findByEid(eid);
    }

    /**
     * 保存作业
     *
     * @return
     * @throws Exception
     */
    public Exam doSave(Exam exam, HMapper hMapper) throws Exception {
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }
        canSave(exam);
        exam.setDateline(CUtil.getUnixTimestamp());
        switch (exam.getEtype()) {
            case COMMON:
                exam = parseExam(exam);
                break;
            case TSMART:
                exam = parseSmartExam(exam, hMapper);
                break;
            case EXERCISE:
                break;
            case SSMART:
                break;
            case PAPER:
                break;
        }

        return exam;
    }

    /**
     * 根据老师智能作业获取学生作业或巩固练习
     *
     * @param exam
     * @param hMapper
     * @return
     */
    public Exam getSmartExam(Exam exam, HMapper hMapper) {
        Exam smartexam = new Exam();
        smartexam.setCanreexam(exam.getCanreexam());
        smartexam.setCanpusherror(exam.getCanpusherror());
        Long uid = hMapper.getLong("uid");
        if (uid != null) {
            smartexam.setUid(uid);
        } else {
            smartexam.setUid(exam.getUid());
        }
        smartexam.setCrid(exam.getCrid());
        smartexam.setData(exam.getData());
        smartexam.setStatus(1);
        smartexam.setEstype(exam.getEstype());
        smartexam.setLimittime(exam.getLimittime());
        smartexam.setStucancorrect(exam.getStucancorrect());
        smartexam.setAnsstarttime(exam.getAnsstarttime());
        smartexam.setAnsendtime(exam.getAnsendtime());
        smartexam.setExamendtime(exam.getExamendtime());
        smartexam.setExamstarttime(exam.getExamstarttime());
        smartexam.setExamtotalscore(exam.getExamtotalscore());
        smartexam.setFromeid(exam.getEid());
        String esubject = hMapper.getString("esubject");

        ExamType etype = hMapper.getObject("etype", ExamType.class);
        if (etype == ExamType.EXERCISE) {
            if (!StringUtils.isEmpty(esubject)) {
                smartexam.setEsubject(esubject);
            }
            smartexam.setEtype(etype);
        } else {
            smartexam.setEtype(ExamType.SSMART);
            smartexam.setEsubject(exam.getEsubject());
        }
        smartexam.setDateline(exam.getDateline());
        return smartexam;
    }

    /**
     * 解析examrelation
     *
     * @param exam
     */
    public void parseExamRelation(Exam exam) {
        List<ExamRelation> examRelationList = HMapper.parseData2List(exam.getData(), "relationSet", ExamRelation[].class);
        if (!ObjectUtils.isEmpty(examRelationList)) {
            parseRelation(exam);
        }
    }

    /**
     * 解析智能作业
     *
     * @param exam
     * @return
     */
    public Exam parseSmartExam(Exam exam, HMapper hMapper) throws Exception {
        List<Condition> conditionList = HMapper.parseData2List(exam.getData(), "conditionList", Condition[].class);
        int totalscore = 0;
        for (Condition condition : conditionList) {
            totalscore += condition.getQuescore() * condition.getQuecount();
        }
        exam.setExamtotalscore(totalscore);
        //正式布置=1
        if (exam.getStatus() == Constant.EXAM_STATUS_IS_OK) {
            if (exam.getEid() > 0) {
                mongoTemplate.updateFirst(new Query(new Criteria().and("eid").is(exam.getEid())), UpdateUtil.buildBaseUpdate(exam), Exam.class);
            } else {
                if (exam.getEtype() != ExamType.EXERCISE) {
                    //先生成TSMART在生成SSMart
                    exam = examDao.getExamRepository().save(exam);
                }
            }
            //解析并保存examRelation
            parseExamRelation(exam);
            Exam smartexam = getSmartExam(exam, hMapper);
            Exam oldsamrtexam = getsmartExam(exam);
            if (!ObjectUtils.isEmpty(oldsamrtexam)) {
                smartexam.setEid(oldsamrtexam.getEid());
                smartexam.setFromeid(exam.getEid());
                mongoTemplate.updateFirst(new Query(new Criteria().and("eid").is(smartexam.getEid())), UpdateUtil.buildBaseUpdate(smartexam), Exam.class);
                List<Question> questionList = getQuestionListByEid(smartexam.getEid());
                if (!ObjectUtils.isEmpty(questionList)) {
                    questionList.forEach(question1 -> mongoTemplate.remove(question1));
                }
            } else {
                smartexam = examDao.getExamRepository().save(smartexam);
            }
            User user = new User();
            user.setUid(exam.getUid());
            user.setCrid(exam.getCrid());
            //解析并保存examRelation
            parseExamRelation(smartexam);
            //解析conditionlist
            parseCondition(conditionList, smartexam);
        }
        return exam;
    }

    /**
     * 解析智能条件
     *
     * @param conditionList
     * @param exam
     * @throws Exception
     */
    public void parseCondition(List<Condition> conditionList, Exam exam) throws Exception {
        List<Condition> list = new ArrayList<>();
        Question question = new Question();
        for (Condition condition : conditionList) {
            List<KuQuestion> kuQuestionListByCondition = kuQuestionDao.getExamCountByCondition(condition);
            ArrayList<KuQuestion> kuQuestionArrayList = new ArrayList<>(kuQuestionListByCondition);
            Collections.shuffle(kuQuestionArrayList);//打乱顺序
            //返回生成约定的数量
            List<KuQuestion> kuQuestionList = kuQuestionArrayList.stream().limit(condition.getQuecount()).collect(Collectors.toList());
            if (kuQuestionList.size() > 0) {
                condition.setQuecount(kuQuestionList.size());
                list.add(condition);
            }

            for (KuQuestion kuQuestion : kuQuestionList) {
                question.setEid(exam.getEid());
                question.setQuescore(condition.getQuescore());
                question.setCrid(exam.getCrid());
                question.setUid(exam.getUid());
                question.setDateline(CUtil.getUnixTimestamp());
                question.setStatus(1);
                question.setLevel(condition.getLevel());
                question.setQsubject(kuQuestion.getQsubject());
                question.setData(kuQuestion.getData());
                question.setQueType(kuQuestion.getQueType());
                question.setExtdata(kuQuestion.getExtdata());
                question.setMd5code(kuQuestion.getMd5code());
                question = parseQuestionInfo(question);

                question = questionService.getQuestionDao().getQuestionRepository().save(question);

                UpInsertBlank(question);

                List<QuestionRelation> questionRelationList = HMapper.parseData2List(question.getData(), "relationSet", QuestionRelation[].class);
                if (!ObjectUtils.isEmpty(questionRelationList)) {
                    for (QuestionRelation questionRelation : questionRelationList) {
                        questionRelation.setQid(question.getQid());
                        questionRelationDao.getQuestionRelationRepository().save(questionRelation);
                    }
                }
            }
        }
    }

    public void UpInsertBlank(Question question) throws Exception {
        //保存blank
        List<Blank> blanks = HMapper.parseData2List(question.getData(), "blankList", Blank[].class);
        List<Blank> blankList = blankService.getBlankListByQid(question.getQid());
        long count = blanks.stream().filter(blank -> blank.getIsAnswer().equals("1")).count();
        if (blankList.size() > 0) {
            blankList.forEach(b -> mongoTemplate.remove(b));
        }
        for (Blank blank : blanks) {
            if (blank.getIsAnswer().equals("1")) {
                double eachScore = (double) question.getQuescore() / (double) count;
                BigDecimal bd = new BigDecimal(String.valueOf(eachScore));
                eachScore = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                blank.setScore(eachScore);
            }
            blank.setQid(question.getQid());
            mongoTemplate.save(blank);
        }
    }


    /**
     * 解析relation
     *
     * @param exam
     */

    public void parseRelation(Exam exam) {
        //解析并保存examRelation
        List<ExamRelation> examRelationList = HMapper.parseData2List(exam.getData(), "relationSet", ExamRelation[].class);
        List<ExamRelation> relationList = mongoTemplate.find(new Query(new Criteria().and("eid").is(exam.getEid())), ExamRelation.class);
        if (relationList.size() > 0) {
            examRelationDao.getExamRelationRepository().delete(relationList);
            relationList = mongoTemplate.find(new Query(new Criteria().and("eid").is(exam.getEid())), ExamRelation.class);
            if (relationList.size() == 0) {
                examRelationList.forEach(examRelation -> {
                    examRelation.setEid(exam.getEid());
                    mongoTemplate.save(examRelation);
                });
            }
            return;
        }
        examRelationList.forEach(examRelation -> {
            examRelation.setEid(exam.getEid());
            mongoTemplate.save(examRelation);
        });
    }

    public Map<String, Object> getTmpMap(List<Exam> examList) {
        if (!ObjectUtils.isEmpty(examList)) {
            Map<String, Object> tmpMap = null;
            for (Exam e : examList) {
                tmpMap = new HashMap<>();
                tmpMap.put("exam", e);
                UserAnswer userAnswer = userAnswerService.findUserAnswerByUidAndEid(e.getUid(), e.getEid());
                tmpMap.put("userAnswer", userAnswerService.getSimpleInfo(userAnswer));
                return tmpMap;
            }
        }
        return null;
    }

    /**
     * 解析普通作业
     *
     * @param exam
     * @return
     * @throws Exception
     */
    private Exam parseExam(Exam exam) throws Exception {
        List<Question> questionList = HMapper.parseData2List(exam.getData(), "questionList", Question[].class);
        exam.setExamtotalscore(questionList.stream().mapToInt(Question::getQuescore).sum());

        //如果eid存在则更新
        if (exam.getEid() != 0) {
            Query query = new Query(new Criteria().and("eid").is(exam.getEid()));
            Update update = UpdateUtil.buildBaseUpdate(exam);
            mongoTemplate.updateFirst(query, update, Exam.class);
        } else {
            exam = examDao.getExamRepository().save(exam);
        }
        //解析并保存examRelation
        parseRelation(exam);
        //解析question和questionrelation
        exam = parseQuestion(exam);
        return exam;
    }

    /**
     * 保存前验证
     *
     * @param exam
     * @throws Exception
     */
    private void canSave(Exam exam) throws Exception {
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }
        if (StringUtils.isEmpty(exam.getEsubject())) {
            throw new CException(ErrorCode.BLANKSUBJECT_IS_NULL);
        }

        if (exam.getCrid() == 0L) {
            throw new CException(ErrorCode.CRID_IS_NULL);
        }

        if (exam.getUid() == 0L) {
            throw new CException(ErrorCode.EXAM_UID_ISNULL);
        }

    }

    /**
     * 判断试题是否可以保存
     *
     * @param question
     * @throws Exception
     */
    public void canSave(Question question) throws Exception {
        if (question == null) {
            throw new CException(ErrorCode.QUESTION_ISNOT_EXIST);
        }

        //判断试题的题目
        if (StringUtils.isEmpty(question.getQsubject())) {
            throw new CException(ErrorCode.QUESTION_QSUBJECT_ISNULL);
        }

        if (question.getQueType() == QueType.Z || question.getQueType() == QueType.G) {
            return;
        }

        if (question.getQuescore() == 0) {
            throw new CException(ErrorCode.QUESTION_SCORE_ISNULL);
        }

        //判断试题的题目
        if (StringUtils.isEmpty(question.getData())) {
            throw new CException(ErrorCode.QUESTION_DATA_ISNULL);
        }

        if (ObjectUtils.isEmpty(question.getQueType())) {
            throw new CException(ErrorCode.QUESTION_QTYPE_ISNULL);
        }

        if (ObjectUtils.isEmpty(question.getCrid())) {
            throw new CException(ErrorCode.CRID_IS_NULL);
        }

        if (ObjectUtils.isEmpty(question.getDateline())) {
            question.setDateline(CUtil.getUnixTimestamp());
        }

        if (question.getUid() == 0L) {
            throw new CException(ErrorCode.QUESTION_UID_ISNULL);
        }

    }

    /**
     * 从数据库中解析作业序列，生成作业对象实体，用于作业持久化
     *
     * @param question
     * @return
     * @throws Exception
     */
    public Question parseQuestionInfo(Question question) throws Exception {
        if (question.getQueType() == QueType.Z || question.getQueType() == QueType.G) {
            return question;
        }
        int queScore = question.getQuescore();
        if (queScore == 0) {
            throw new CException(ErrorCode.QUESTION_SCORE_ISNULL);
        }
        if (question.getQueType().toString().startsWith("X") || question.getQueType() == QueType.H) {
            canSave(question);
            return question;
        }
        List<Blank> blanks = HMapper.parseData2List(question.getData(), "blankList", Blank[].class);
        if (ObjectUtils.isEmpty(blanks)) {
            throw new CException(ErrorCode.BLANK_ISNOT_EXIST);
        }
        List<Blank> trueBlanks = new ArrayList<>();

        StringBuilder choicestr = new StringBuilder();
        if (Arrays.asList(QueType.A, QueType.B, QueType.D).contains(question.getQueType())) {
            blanks.forEach(blank -> {
                if (blank.isanswer() == 1) {
                    choicestr.append("1");
                    trueBlanks.add(blank);
                } else {
                    choicestr.append("0");
                }
            });
        } else {
            for (Blank blank : blanks) {
                if (blank.isanswer() == 1) {
                    trueBlanks.add(blank);
                }
            }
        }
        question.setChoicestr(choicestr.toString());
        canSave(question);
        return question;
    }

    /**
     * 解析作业(保存question & questionRelation)
     *
     * @param exam
     * @return
     */
    public Exam parseQuestion(Exam exam) throws Exception {
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }
        List<Question> questionList = HMapper.parseData2List(exam.getData(), "questionList", Question[].class);
        Query query = new Query(new Criteria().and("eid").is(exam.getEid()));
        List<Question> questions = mongoTemplate.find(query, Question.class);
        //如果存在
        if (questions.size() > 0) {
            questions.forEach(question -> mongoTemplate.remove(question));
        }
        //作业总分
        int totalScore = 0;
        for (Question question : questionList) {
            totalScore += question.getQuescore();
            question.setCrid(exam.getCrid());
            question.setStatus(exam.getStatus());
            question.setDateline(exam.getDateline());
            question.setUid(exam.getUid());
            question.setEid(exam.getEid());
            question.setMd5code(questionService.calcMd5Code(question));
            question = parseQuestionInfo(question);
            question = questionDao.getQuestionRepository().save(question);
            //保存blank
            UpInsertBlank(question);

            //解析并保存questionRelation
            List<QuestionRelation> questionRelationList = HMapper.parseData2List(question.getData(), "relationSet", QuestionRelation[].class);
            List<QuestionRelation> relationList = mongoTemplate.find(new Query(new Criteria().and("qid").is(question.getQid())), QuestionRelation.class);
            //如果存在，则更新
            if (relationList.size() > 0) {
                questionRelationDao.getQuestionRelationRepository().delete(relationList);
            }
            relationList = mongoTemplate.find(new Query(new Criteria().and("qid").is(question.getQid())), QuestionRelation.class);
            if (questionRelationList.size() > 0 && relationList.size() == 0) {
                for (QuestionRelation questionRelation : questionRelationList) {
                    questionRelation.setQid(question.getQid());
                    questionRelationDao.getQuestionRelationRepository().save(questionRelation);
                }
            }
            if (exam.getStatus() == 1) {
                //如果题库中没有该题目将作业添加到题库中，否则不添加
                KuQuestion dbKuQuestion = kuQuestionDao.getKuQuestionRepository().findByMd5codeAndUidAndCrid(question.getMd5code(), question.getUid(), question.getCrid());
                if (ObjectUtils.isEmpty(dbKuQuestion)) {
                    kuQuestionDao.addToKuQuestion(question);
                }
            }
        }
        if (exam.getExamtotalscore() != totalScore) {
            exam.setExamtotalscore(totalScore);
        }
        return exam;
    }

    /**
     * 根据eid查询exam
     *
     * @param eid
     * @return
     */
    public Exam getExamByEid(long eid) {
        return examDao.getExamRepository().findByEid(eid);
    }


    /**
     * 保存exam
     *
     * @param exam
     * @return
     */
    public Exam saveExam(Exam exam) {
        return examDao.getExamRepository().save(exam);
    }

    /**
     * 根据条件从题库生成question
     *
     * @param kuQuestion
     * @param exam
     * @param condition
     * @return
     */
    private Question createQuestion(KuQuestion kuQuestion, Exam exam, Condition condition) {
        Question question = new Question();
        question.setQuescore(condition.getQuescore());
        question.setCrid(exam.getCrid());
        question.setUid(exam.getUid());
        question.setDateline(CUtil.getUnixTimestamp());
        question.setStatus(1);
        question.setLevel(condition.getLevel());
        question.setQsubject(kuQuestion.getQsubject());
        question.setQueType(kuQuestion.getQueType());
        return question;
    }

    public Exam updateSmartExam(Exam exam, HMapper hMapper) throws Exception {
        Exam oldssExam = getsmartExam(exam);
        //如果有答题记录则不允许修改
        List<UserAnswer> userAnswerList = userAnswerService.getUserAnswerListByEid(oldssExam.getEid());
        if (userAnswerList.size() > 0) {
            throw new CException(ErrorCode.EXAM_IS_ANSWERED);
        }
        exam = parseSmartExam(exam, hMapper);
        return exam;
    }

    /**
     * 根据condition判断题库数量
     * (如果题库数量不足返回condition)
     *
     * @param conditions
     * @param user
     * @throws Exception
     */
    public List<HMapper> checkKuQuestionByCondition(List<Condition> conditions, User user) throws Exception {
        if (ObjectUtils.isEmpty(conditions)) {
            throw new CException(ErrorCode.CONDITIONLIST_IS_NULL);
        }
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        List<HMapper> list = new ArrayList<>();
        conditions.forEach(condition -> {
            List<KuQuestion> kuQuestionList = kuQuestionDao.getExamCountByCondition(condition);
            //如果题库中的数量小于目标数量，返回条件condition
            if (kuQuestionList.size() < condition.getQuecount()) {
                HMapper hMapper = new HMapper();
                hMapper.put("condition", condition);
                hMapper.put("kuQuestionList", kuQuestionList);
                list.add(hMapper);
            }
        });
        return list;
    }

    /**
     * 判断是否可以修改
     *
     * @param exam
     * @param params
     */
    public void canEdit(Exam exam, HMapper params) throws CException {

        //作业答案截止显示时间
        Long ansendtime = params.getLong("ansendtime");
        if (ansendtime != null) {
            exam.setAnsendtime(ansendtime.longValue());
        }

        //作业答案开始显示时间
        Long ansstarttime = params.getLong("ansstarttime");
        if (ansstarttime != null) {
            exam.setAnsstarttime(ansstarttime);
        }

        //作业开做时间
        Long examstarttime = params.getLong("examstarttime");
        if (examstarttime != null) {
            exam.setExamstarttime(examstarttime);
        }

        //作业截止答题时间
        Long examendtime = params.getLong("examendtime");
        if (examendtime != null) {
            exam.setExamendtime(examendtime);
        }

        //是否可以根据错题推送
        Integer canpusherror = params.getInteger("canpusherror");
        if (canpusherror != null) {
            exam.setCanpusherror(canpusherror);
        }

        //教师智能作业学生是否可以生成多分作业(一般情况下一份教师智能作业学生智能生成一个试卷)
        Integer canreexam = params.getInteger("canreexam");
        if (canreexam != null) {
            exam.setCanreexam(canreexam);
        }

        //作业大标题
        String esubject = params.getString("esubject", true);
        if (!StringUtils.isEmpty(esubject)) {
            exam.setEsubject(esubject);
        }

        //作业答题限时(能不改尽量不要修改)
        Integer limittime = params.getInteger("limittime");
        if (limittime != null) {
            exam.setLimittime(limittime.intValue());
        }

        //学生是否可以批改主观题
        Integer stucancorrect = params.getInteger("stucancorrect");
        if (stucancorrect != null) {
            exam.setStucancorrect(stucancorrect);
        }
        String estype = params.getString("estype");
        if (estype != null) {
            exam.setEstype(estype);
        }
        List<UserAnswer> userAnswerList = userAnswerService.getUserAnswerListByEid(exam.getEid());
        if (!ObjectUtils.isEmpty(userAnswerList)) {
            throw new CException(ErrorCode.EXAM_IS_ANSWERED);
        }
    }

    /**
     * 获取教师作业列表
     *
     * @param params
     * @return
     */
    public CheckResult getExamListForTeacher(HMapper params) throws Exception {
        Query query = new Query();
        Criteria criteria = new Criteria();
        User user = params.getObject("user", User.class);
        criteria.and("uid").is(user.getUid());
        Integer dtag = params.getInteger("dtag");

        if (dtag == null) {
            dtag = 0;
        }
        criteria.and("dtag").is(dtag);
        long crid = params.getLongValue("crid");
        if (crid != 0L) {
            criteria.and("crid").is(crid);
        }
        ExamType etype = params.getObject("etype", ExamType.class);
        if (!ObjectUtils.isEmpty(etype)) {
            criteria.and("etype").is(etype);
        }
        String estype = params.getString("estype");
        if (!StringUtils.isEmpty(estype)) {
            criteria.and("estype").is(estype);
        }
        Integer status = params.getInteger("status");
        if (status != null) {
            criteria.and("status").is(status);
        }
        //查询老师智能作业TSmart

        criteria.orOperator(criteria.where("etype").is(ExamType.COMMON), criteria.where("etype").is(ExamType.TSMART));
        //模糊查询q(mongo只能是string类型,基本数据类型无法模糊查询)
        String q = params.getString("q");
        if (!StringUtils.isEmpty(q)) {
            criteria.and("esubject").regex(".*?" + q + ".*");
        }
        query.addCriteria(criteria);
        query.with(new Sort(Sort.Direction.DESC, "eid"));
        long count = mongoTemplate.count(query, Exam.class);
        List<Exam> examList = mongoTemplate.find(query.with(params.parsePage()), Exam.class);
        Page<Exam> page = new PageImpl<>(examList, params.parsePage(), count);
        List<Map<String, Object>> list = new ArrayList<>();

        page.getContent().forEach(exam -> {
            Map<String, Object> examMap = new HashMap<>();
            examMap.put("eid", exam.getEid());
            examMap.put("esubject", exam.getEsubject());
            examMap.put("status", exam.getStatus());
            examMap.put("dateline", exam.getDateline());
            examMap.put("examscore", exam.getExamtotalscore());
            examMap.put("limittime", exam.getLimittime());
            examMap.put("etype", exam.getEtype());
            examMap.put("estype", exam.getEstype());
            examMap.put("examstarttime", exam.getExamstarttime());
            examMap.put("examendtime", exam.getExamendtime());
            examMap.put("ansstarttime", exam.getAnsstarttime());
            examMap.put("ansendtime", exam.getAnsendtime());
            List<BaseRelationForResp> relationSet = HMapper.parseData2List(exam.getData(), "relationSet", BaseRelationForResp[].class);
            examMap.put("relationSet", relationSet);
            examMap.put("answercount", examDao.getExamAnswerCountForTeacher(exam));
            list.add(examMap);
        });

        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("examList", list);
        checkResult.addErrData("pageInfo", HMapper.pageRet(page));
        return checkResult;
    }

    /**
     * 获取学生作业列表(先查出relation表中的ID然后根据ID去查询)
     *
     * @param params
     * @return
     */
    public CheckResult getExamListForStudent(HMapper params) throws CException {
        User user = params.getObject("user", User.class);
        String action = params.getString("action", true);
        if (StringUtils.isEmpty(action)) {
            action = "fordo";
        }
        //先根据relation查出满足eid
        List<Long> eids = examDao.getEidsByExamRelation(params);
        Set<Long> eidList;
        //在筛选出已做和未做的eid
        eidList = examDao.getExamByCondition(eids, user.getUid(), action);
        //查询满足条件的exam
        return examDao.getExamByEids(params, eidList);
    }

    /**
     * 根据eid查出question
     *
     * @param eid
     * @return
     */
    public List<Question> getQuestionListByEid(long eid) {
        return examDao.getQuestionListByEid(eid);

    }

    /**
     * 根据eid获取作业基础信息
     *
     * @param eid
     * @return
     */
    public HMapper getExamSimpleInfo(long eid) {
        HMapper hMapper = new HMapper();
        Exam exam = examDao.getExamRepository().findByEid(eid);
        if (ObjectUtils.isEmpty(exam)) {
            return hMapper;
        }
        List<ExamRelation> examRelationList = examDao.getExamRelationList(eid);
        Long tid = null;
        String relationname = "";
        if (!ObjectUtils.isEmpty(examRelationList)) {
            tid = examRelationList.get(0).getTid();
            relationname = examRelationList.get(0).getRelationname();
        }
        hMapper.put("eid", exam.getEid());
        hMapper.put("etype", exam.getEtype());
        hMapper.put("examtotalscore", exam.getExamtotalscore());
        hMapper.put("ansstarttime", exam.getAnsstarttime());
        hMapper.put("ansendtime", exam.getAnsendtime());
        hMapper.put("crid", exam.getCrid());
        hMapper.put("examstarttime", exam.getExamstarttime());
        hMapper.put("examendtime", exam.getExamendtime());
        hMapper.put("canreexam", exam.getCanreexam());
        hMapper.put("esubject", exam.getEsubject());
        hMapper.put("limittime", exam.getLimittime());
        hMapper.put("status", exam.getStatus());
        hMapper.put("dateline", exam.getDateline());
        hMapper.put("uid", exam.getUid());
        hMapper.put("tid", tid);
        hMapper.put("dtag", exam.getDtag());
        hMapper.put("relationname", relationname);
        hMapper.put("stucancorrect", exam.getStucancorrect());
        examRelationList.forEach(examRelation -> {
            if (examRelation.getTtype() == RelationType.COURSE) {
                hMapper.put("cwid", examRelation.getTid());
                hMapper.put("cwname", examRelation.getRelationname());
            }
        });
        return hMapper;
    }

    /**
     * 获取简单答题明细
     *
     * @param userAnswer
     * @return
     */

    public List<Map<String, Object>> getSimpleAQD(UserAnswer userAnswer) {
        return mongoTemplate.find(new Query(new Criteria().and("aid").is(userAnswer.getAid())), AnswerQueDetail.class).stream().collect(ArrayList<Map<String, Object>>::new, (list, answerQueDetail) -> {
            Map map = new HashMap<>();
            map.put("qid", answerQueDetail.getQid());
            map.put("dqid", answerQueDetail.getDqid());
            map.put("allright", answerQueDetail.getAllright());
            map.put("choicestr", answerQueDetail.getChoicestr());
            map.put("status", answerQueDetail.getStatus());
            map.put("totalscore", answerQueDetail.getTotalscore());
            map.put("markuid", answerQueDetail.getMarkuid());
            map.put("uid", answerQueDetail.getUid());
            map.put("version", answerQueDetail.getVersion());
            map.put("data", answerQueDetail.getData());
            List<AnswerBlankDetail> answerBlankDetailList = answerBlankDetailService.getBlankListByDqid(answerQueDetail.getDqid());
            if (answerBlankDetailList.size() > 0) {
                map.put("answerblankdetaillist", answerBlankDetailList);
            }
            list.add(map);
        }, ArrayList::addAll);
    }

    /**
     * 获取在教师布置的智能作业下指定学生生成的智能作业列表
     *
     * @param exam
     * @param params
     * @return
     */
    public Page<Exam> stuSmartExamList(Exam exam, HMapper params) {
        Query query = new Query();
        Criteria criteria = new Criteria();
//        删除标志判断
        Integer dtag = params.getInteger("dtag");
        if (dtag == null) {
            dtag = 0;
        }
        criteria.and("dtag").is(dtag);
        criteria.and("fromeid").is(exam.getEid());
        query.addCriteria(criteria);
        long count = mongoTemplate.count(query, Exam.class);
        List<Exam> examList = mongoTemplate.find(query.with(params.parsePage()), Exam.class);
        PageImpl<Exam> page = new PageImpl<>(examList, params.parsePage(), count);
        return page;
    }

    /**
     * 获取单个智能作业下所有学生生成的智能作业列表
     *
     * @param exam
     * @param params
     * @return
     * @throws Exception
     */
    public Page<Exam> stuAllSmartExamList(Exam exam, HMapper params) throws Exception {
        if (ObjectUtils.isEmpty(exam) || ObjectUtils.isEmpty(params)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST + "or" + ErrorCode.PARAMS_IS_NULL);
        }
        return examDao.getAllSmartExamList(exam, params);
    }

    /**
     * 删除(置换标志位)
     *
     * @param eid
     */
    public void doDelete(long eid) throws Exception {
        if (eid != 0) {
            Query query = new Query();
            Criteria criteria = new Criteria();
            criteria.and("eid").is(eid);
            Update update = new Update();
            update.set("dtag", 1);
            mongoTemplate.updateFirst(query.addCriteria(criteria), update, Exam.class);
        }
        //如果存在答题记录，删除记录
        List<UserAnswer> userAnswerList = userAnswerService.getUserAnswerListByEid(eid);
        if (userAnswerList.size() > 0) {
            userAnswerList.forEach(userAnswer -> userAnswerService.deleteUserAnswer(userAnswer));
        }

    }

    public Exam updateExam(Exam exam) throws Exception {
        return parseExam(exam);
    }

    /**
     * 获取学生所有作业集合
     *
     * @param hMapper
     * @return
     */
    public int getExamListforstu(HMapper hMapper) throws Exception {
        return examDao.getExamListForstu(hMapper);
    }

    /**
     * 获取当天布置作业数量
     *
     * @return
     */
    public long getExamCountByToday(HMapper hMapper, User user) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("uid").is(user.getUid());
        criteria.and("crid").is(user.getCrid());
        criteria.orOperator(criteria.where("dateline").gte(DateUtil.getCurrentDayStartTime()).lte(DateUtil.getCurrentDayEndTime()));
        ExamType etype = hMapper.getObject("etype", ExamType.class);
        if (etype == ExamType.EXERCISE) {
            criteria.and("etype").is(etype);
        } else {
            criteria.and("etype").in(Arrays.asList(ExamType.COMMON, ExamType.TSMART));
        }
        query.addCriteria(criteria);
        return mongoTemplate.count(query, Exam.class);
    }

    /**
     * 分析统计一份作业的概要
     *
     * @param exam
     * @param hMapper
     * @return
     */
    public CheckResult efenxiSummary(Exam exam, HMapper hMapper) throws CException {
        int submitcount = 0;
        int totalusedtime = 0;
        List<Long> uids = hMapper.arr2List("uids", Long[].class);
        List<UserAnswer> userAnswerList = userAnswerService.getUserAnswerListByEid(exam.getEid());
        if (!ObjectUtils.isEmpty(uids)) {
            userAnswerList = userAnswerList.stream().filter(userAnswer -> uids.contains(userAnswer.getUid())).collect(Collectors.toList());
        }
        for (UserAnswer userAnswer : userAnswerList) {
            if (userAnswer.getStatus() == 1) {
                submitcount += 1;
                totalusedtime += userAnswer.getUsedtime();
            }
        }

        List fenxilist = fenxiSummary(exam);
        List<ExamRelation> examRelationList = examDao.getExamRelationList(exam.getEid());
        examRelationList.stream().filter(examRelation -> examRelation.getTtype() == RelationType.FOLDER).forEach(examRelation -> {
            hMapper.put("tid", examRelation.getTid());
        });
        List<ExamRelation> examRelations = examRelationList.stream().filter(examRelation -> examRelation.getTtype() == RelationType.CLASS).collect(Collectors.toList());
        if (examRelations.size() > 0) {
            hMapper.put("class", getclassMap(examRelations));
        }
        hMapper.put("esubject", exam.getEsubject());
        hMapper.put("dateline", exam.getDateline());
        hMapper.put("totalscore", exam.getExamtotalscore());
        hMapper.put("limittime", exam.getLimittime());
        hMapper.put("fenxi", fenxilist);
        hMapper.put("submitcount", submitcount);
        int avgusedtime = 0;
        if (submitcount > 0) {
            avgusedtime = totalusedtime / submitcount;
        }
        hMapper.put("avgusedtime", avgusedtime);
        return CheckResult.newInstance().addErrData("efenxisummary", hMapper);
    }

    public List<Map<String, Object>> fenxiSummary(Exam exam) {
        Set<QueType> set = new HashSet();
        List<Map<String, Object>> list = new ArrayList();
        List<Question> questions = getQuestionListByEid(exam.getEid());
        questions.forEach(question -> set.add(question.getQueType()));
        if (set.size() < 1) {
            return new ArrayList<>();
        }
        for (QueType quetype : set) {
            int count = 0;
            Map map = new HashMap();
            for (Question question : questions) {
                if (quetype == question.getQueType()) {
                    count += 1;
                }
            }
            map.put("quetype", quetype);
            map.put("count", count);
            list.add(map);
        }
        return list;
    }

    /**
     * 学生分析作业
     *
     * @param exam
     * @param uid
     * @return
     */
    public CheckResult efenxiStuSummary(Exam exam, Long uid, HMapper hMapper) throws Exception {
        List<Long> uids = hMapper.arr2List("uids", Long[].class);
        UserAnswer userAnswer = null;
        Map<String, Object> map = new HashMap();
        List<UserAnswer> answerList = new ArrayList<>();
        List fenxilist = new ArrayList();
        if (exam.getEtype() == ExamType.TSMART) {
            userAnswer = userAnswerService.findUserAnswerByUidAndEid(uid, getsmartExam(exam).getEid());
            answerList = userAnswerService.getUserAnswerListByEid(getsmartExam(exam).getEid());
            fenxilist = fenxiSummary(getsmartExam(exam));
        } else if (exam.getEtype() == ExamType.COMMON) {
            userAnswer = userAnswerService.findUserAnswerByUidAndEid(uid, exam.getEid());
            answerList = userAnswerService.getUserAnswerListByEid(exam.getEid());
            fenxilist = fenxiSummary(exam);
        }
        if (!ObjectUtils.isEmpty(uids)) {
            map.put("answercounts", answerList.stream().filter(ua -> ua.getStatus() == 1 && uids.contains(ua.getUid())).count());
        } else {
            map.put("answercounts", answerList.stream().filter(ua -> ua.getStatus() == 1).count());
        }
        if (ObjectUtils.isEmpty(userAnswer)) {
            throw new CException(ErrorCode.USERANSWER_ISNOT_EXIST);
        }

        List<ExamRelation> examRelations = examRelationDao.getExamRelationList(exam.getEid());
        examRelations = examRelations.stream().filter(examRelation -> examRelation.getTtype() == RelationType.CLASS).collect(Collectors.toList());
        getclassMap(examRelations);
        if (examRelations.size() > 0) {
            map.put("class", getclassMap(examRelations));
        }
        examDao.getExamRelationList(exam.getEid()).stream().filter(examRelation -> examRelation.getTtype().equals(RelationType.FOLDER.toString())).forEach(examRelation ->
                map.put("tid", examRelation.getTid()));
        map.put("fenxi", fenxilist);
        map.put("esubject", exam.getEsubject());
        map.put("ansdateline", userAnswer.getAnsdateline());
        map.put("anstotalscore", userAnswer.getAnstotalscore());
        map.put("userdtime", userAnswer.getUsedtime());
        map.put("examlimittime", exam.getLimittime());
        List<UserAnswer> userAnswerList = userAnswerService.getUserAnswerListByEid(exam.getEid());
        if (userAnswerList.size() == 1 || userAnswer.getAnstotalscore() == exam.getExamtotalscore()) {
            map.put("level", 1);
        } else {
            List<UserAnswer> answers = answerList.stream().filter(userAnswer1 -> userAnswer1.getStatus() == 1).sorted(Comparator.comparing(UserAnswer::getAnstotalscore).reversed()).collect(Collectors.toList());
            if (!ObjectUtils.isEmpty(uids)) {
                answers = answers.stream().filter(userAnswer1 -> uids.contains(userAnswer1.getUid())).collect(Collectors.toList());
            }
            List<Double> list = new ArrayList();
            answers.forEach(ua -> list.add(ua.getAnstotalscore()));
            List<Double> collect = list.stream().distinct().collect(Collectors.toList());
            for (int i = 0, size = collect.size(); i < size; i++) {
                if (collect.get(i) == userAnswer.getAnstotalscore()) {
                    map.put("level", i + 1);
                }
            }
        }

        return CheckResult.newInstance().addErrData("efenxistu", map);
    }

    private List<Map<String, Object>> getclassMap(List<ExamRelation> examRelations) {
        List<Map<String, Object>> list = new ArrayList<>();
        examRelations.forEach(examRelation -> {
            HashMap<String, Object> tmap = new HashMap<>();
            tmap.put("classname", examRelation.getRelationname());
            tmap.put("classid", examRelation.getClassid());
            list.add(tmap);
        });
        return list;
    }

    /**
     * 获取答题明细列表
     *
     * @param exam
     * @param userAnswer
     * @return
     */
    public List<QueMap> getAnswerQuestionDetailList(Exam exam, UserAnswer userAnswer) {
        List<Condition> conditionList = HMapper.parseData2List(exam.getData(), "conditionList", Condition[].class);
        return answerDetailService.getAnswerDetailDao().getAnswerQueDetailList(userAnswer.getAid()).stream().collect(ArrayList<QueMap>::new, (list, answerQueDetail) -> {
            QueMap quemap = new QueMap();
            quemap.setAllright(answerQueDetail.getAllright());
            Question question = questionService.getQuestionByQid(answerQueDetail.getQid());
            List<QuestionRelation> questionRelationList = questionRelationDao.getQuestionRelationListByQid(answerQueDetail.getQid());
            quemap.setQuetype(question.getQueType().toString());
            HMapper map = new HMapper();
            QuestionRelation questionRelation = questionRelationList.stream().filter(qr -> qr.getTtype() == RelationType.CHAPTER && !StringUtils.isEmpty(qr.getPath())).findFirst().get();
            if (!ObjectUtils.isEmpty(questionRelation)) {
                map.put("path", questionRelation.getPath());
                map.put("queType", question.getQueType());
                map.put("crid", exam.getCrid());
                long count = kuQuestionDao.getCountForKu(map);
                quemap.setCanaddcount((int) count);
                quemap.setRelationname(questionRelation.getRelationname());
                quemap.setPath(questionRelation.getPath());
                Optional<Condition> optional = conditionList.stream().filter(c -> c.getPath().equals(questionRelation.getPath())).findFirst();
                if (optional.isPresent()) {
                    quemap.setChapterstr(optional.get().getChapterstr());
                }
            } else {
                quemap.setPath("");
                quemap.setRelationname("");
                quemap.setChapterstr("");
            }
            list.add(quemap);
        }, ArrayList::addAll);
    }

    /**
     * 获取巩固练习列表
     *
     * @param exam
     */
    public Page<Exam> getexeList(Exam exam, HMapper hMapper) throws CException {
        User user = hMapper.getObject("user", User.class);
        Query query = new Query();
        Criteria criteria = new Criteria();

        //删除标志判断
        Integer dtag = hMapper.getInteger("dtag");
        if (dtag == null) {
            dtag = 0;
        }
        criteria.and("dtag").is(dtag);
        criteria.and("uid").is(user.getUid());
        criteria.and("fromeid").is(exam.getFromeid());
        criteria.and("etype").is(ExamType.EXERCISE);
        query.addCriteria(criteria);
        long count = mongoTemplate.count(query, Exam.class);
        List<Exam> examList = mongoTemplate.find(query.with(hMapper.parsePage()), Exam.class);
        PageImpl<Exam> page = new PageImpl<>(examList, hMapper.parsePage(), count);
        return page;
    }

    /**
     * 获取巩固练习列表
     *
     * @param page
     * @return
     */
    public List<Map<String, Object>> getExamList(Page<Exam> page) {
        return page.getContent().stream().collect(ArrayList<Map<String, Object>>::new, (list, tmpexam) -> {
            List<Question> questionList = getQuestionListByEid(tmpexam.getEid());
            Map<String, Object> tmp = new HashMap();
            tmp.put("esubject", tmpexam.getEsubject());
            tmp.put("dateline", tmpexam.getDateline());
            tmp.put("quecount", questionList.size());
            List<UserAnswer> userAnswerSet = userAnswerService.getUserAnswerListByEid(tmpexam.getEid());
            if (userAnswerSet.size() > 0) {
                Long count = userAnswerSet.stream().filter(ua -> ua.getStatus() == 1).count();
                if (count > 0) {
                    tmp.put("status", Constant.FINISHED);
                } else {
                    tmp.put("status", Constant.FINISHING);
                }
            } else {
                tmp.put("status", Constant.UNFINISHED);
            }
            Set set = new HashSet();
            for (Question question : questionList) {
                List<QuestionRelation> questionRelationList = questionRelationDao.getQuestionRelationListByQid(question.getQid());
                List<QuestionRelation> qrlist = questionRelationList.stream().filter(questionRelation -> questionRelation.getTtype() == RelationType.CHAPTER && !StringUtils.isEmpty(questionRelation.getPath())).collect(Collectors.toList());
                set.add(qrlist.get(0).getPath());
            }
            tmp.put("chapterstrcount", set.size());
            tmp.put("eid", tmpexam.getEid());
            list.add(tmp);
        }, ArrayList::addAll);
    }

    /**
     * 获取作业详细信息
     *
     * @param exam
     * @return
     */
    public Map<String, Object> getExam(Exam exam) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("eid", exam.getEid());
        map.put("dtag", exam.getDtag());
        map.put("version", exam.getVersion());
        map.put("ansendtime", exam.getAnsendtime());
        map.put("ansstarttime", exam.getAnsstarttime());
        map.put("canpusherror", exam.getCanpusherror());
        map.put("canreexam", exam.getCanreexam());
        map.put("crid", exam.getCrid());
        map.put("data", exam.getData());
        map.put("dateline", exam.getDateline());
        map.put("eorder", exam.getEorder());
        map.put("estype", exam.getEtype());
        map.put("esubject", exam.getEsubject());
        map.put("etype", exam.getEtype());
        map.put("examendtime", exam.getExamendtime());
        map.put("examstarttime", exam.getExamstarttime());
        map.put("examtotalscore", exam.getExamtotalscore());
        map.put("limittime", exam.getLimittime());
        map.put("status", exam.getStatus());
        map.put("stucancorrect", exam.getStucancorrect());
        map.put("uid", exam.getUid());
        return map;
    }

    /**
     * 作业分析
     *
     * @param exam
     * @param params
     * @param tag
     * @return
     */
    public List<Map<String, Object>> efenxi(Exam exam, HMapper params, int tag) {
        if (tag == 0) {
            return efenxiEachQue(exam, params);
        } else if (tag == 1) {
            return efenxiRelation(exam, params);
        } else if (tag == 2) {
            return efenxiLevel(exam, params);
        } else if (tag == 3) {
            return efenxiQuetype(exam, params);
        } else {
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> efenxiLevel(Exam exam, HMapper params) {
        return examDao.efenxiRank(exam, params);
    }

    /**
     * 根据题型分析一份作业
     *
     * @param exam
     * @param hMapper
     * @return
     */
    public List<Map<String, Object>> efenxiQuetype(Exam exam, HMapper hMapper) {
        return examDao.efenxiQtype(exam, hMapper);
    }

    /**
     * 针对一张试卷的每个题目进项分析
     *
     * @param exam
     * @param params
     * @return
     */
    public List<Map<String, Object>> efenxiEachQue(Exam exam, HMapper params) {
        List<Map<String, Object>> efenxi = new ArrayList<>();
        List<Question> questionList = examDao.getQuestionListByEid(exam.getEid());
        List<Question> queList = questionList.stream().filter(question -> question.getQueType() != QueType.Z && question.getQueType() != QueType.G).collect(Collectors.toList());
        for (int i = 0, size = queList.size(); i < size; i++) {
            Map<String, Object> qfenxi = questionService.qfenxi(queList.get(i), params);
            qfenxi.put("quecount", i + 1);
            efenxi.add(qfenxi);
        }
        return efenxi;
    }

    /**
     * 针对一张试卷的所有知识点进行分析
     *
     * @param exam
     * @param params
     * @return
     */
    public List<Map<String, Object>> efenxiRelation(Exam exam, HMapper params) {
        return examDao.doAnalysis(exam, params);
    }

    /**
     * 是否可以生成新的学生智能作业
     *
     * @param exam
     * @param params
     * @return 可以生成返回null，不可以生成返回旧作业
     */

    public Exam canNew(Exam exam, HMapper params) {
        Criteria criteria = new Criteria();
        Query query = new Query();
        Long uid = params.getLong("uid");
        criteria.and("uid").is(uid);
        criteria.and("fromeid").is(exam.getEid());
        criteria.and("dtag").is(0);
        List<Exam> exams = mongoTemplate.find(query.addCriteria(criteria), Exam.class);
        if (!ObjectUtils.isEmpty(exams)) {
            Exam lastExam = exams.get(exams.size() - 1);
            params.put("status", 1);
            UserAnswer examUserAnswer = userAnswerService.findUserAnswerByUidAndEid(params.getLong("uid"), lastExam.getEid());
            if (ObjectUtils.isEmpty(examUserAnswer)) {
                return lastExam;
            }
        }
        return null;
    }

    public List<Map<String, Object>> getQuestionList(long eid) {
        List<Question> questions = getQuestionListByEid(eid);
        return questionService.getQuestionInfo(questions);
    }


    public Exam getsmartExam(Exam exam) {
        List<Exam> examList = examDao.getSmartExam(exam);
        if (examList.size() > 0) {
            return examList.get(0);
        }
        return null;
    }

    /**
     * 获取巩固练习
     *
     * @param exam
     * @return
     */
    public List<Exam> getExerciseExam(Exam exam) throws Exception {
        List<Exam> examList = examDao.getExerciseExam(exam);
        return examList;
    }

    public List<ExamRelation> getExamRelationByEid(long eid) {
        return examRelationDao.getExamRelationList(eid);
    }
}
