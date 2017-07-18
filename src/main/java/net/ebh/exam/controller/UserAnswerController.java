package net.ebh.exam.controller;

import net.ebh.exam.TempVo.User;
import net.ebh.exam.base.ExamType;
import net.ebh.exam.base.QueType;
import net.ebh.exam.base.RelationType;
import net.ebh.exam.bean.*;
import net.ebh.exam.dao.ExamRelationDao;
import net.ebh.exam.service.*;
import net.ebh.exam.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zkq on 2016/5/26.
 * 用户答案控制器
 */
@RestController
@RequestMapping(value = "/useranswer", method = {RequestMethod.GET, RequestMethod.POST})
public class UserAnswerController {
    @Autowired
    private UserAnswerService userAnswerService;
    @Autowired
    private ExamService examService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    ErrorBookService errorBookService;
    @Autowired
    BlankService blankService;
    @Autowired
    AnswerDetailService answerDetailService;
    @Autowired
    ExamRelationDao examRelationDao;

    /**
     * 上传答案(status=1则批改)
     */
    @RequestMapping(path = "/save", method = {RequestMethod.POST})
    public CheckResult saveAnswer(@RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Long eid = params.getLong("eid");
        if (ObjectUtils.isEmpty(eid)) {
            throw new CException(ErrorCode.EXAM_EID_ISNULL);
        }
        Exam exam = examService.getExamByEid(eid);
        if (ObjectUtils.isEmpty(exam) || exam.getDtag() == 1) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }

        params.put("uid", user.getUid());

        UserAnswer dbUserAnswer = userAnswerService.findUserAnswerByUidAndEid(user.getUid(), exam.getEid());

        if (!ObjectUtils.isEmpty(dbUserAnswer) && dbUserAnswer.getStatus() == 1) {
            throw new CException(ErrorCode.ANSWER_IS_EXIST);
        }

        if (exam.getExamstarttime() > 0) {
            if (exam.getExamstarttime() > CUtil.getUnixTimestamp()) {
                throw new CException(ErrorCode.EXAM_TIME_NOT_TO);
            }
        }

        if (exam.getExamendtime() > 0) {
            if (exam.getExamendtime() < CUtil.getUnixTimestamp()) {
                throw new CException(ErrorCode.EXAM_TIME_IS_UP);
            }
        }

        UserAnswer userAnswer = params.getObject("userAnswer", UserAnswer.class);
        if (exam.getEtype() == ExamType.EXERCISE) {
            userAnswer.setEid(exam.getEid());
            userAnswer.setFromeid(exam.getFromeid());
        } else if (exam.getEtype() == ExamType.TSMART) {
            userAnswer.setFromeid(exam.getEid());
            userAnswer.setEid(examService.getsmartExam(exam).getEid());
        } else {
            userAnswer.setEid(exam.getEid());
            userAnswer.setFromeid(exam.getEid());
        }
        if (user.getUid() != userAnswer.getUid()) {
            throw new CException(ErrorCode.ANSWER_ISNOT_BELONG);
        }

        userAnswer.setAnstotalscore(0);
        if (ObjectUtils.isEmpty(userAnswer)) {
            throw new CException(ErrorCode.USERANSWER_ISNOT_EXIST);
        }

        if (ObjectUtils.isEmpty(userAnswer.getAid()) &&
                (!ObjectUtils.isEmpty(dbUserAnswer) && dbUserAnswer.getStatus() == 0)) {
            throw new CException(ErrorCode.ANSWER_IS_EXIST);
        }

        CheckResult checkResult = CheckResult.newInstance();

        userAnswer = userAnswerService.doSave(userAnswer, exam, params);
        List<AnswerQueDetail> answerQueDetailList = answerDetailService.getAnswerDetailListByAid(userAnswer.getAid());
        answerQueDetailList.stream().filter(answerQueDetail -> answerQueDetail.getAllright() != 1).forEach(answerQueDetail -> {
            Question question = questionService.getQuestionByQid(answerQueDetail.getQid());
            if (Arrays.asList(QueType.A, QueType.B, QueType.C, QueType.D).contains(question.getQueType())) {
                errorBookService.addErrorbook(answerQueDetail);
            }
        });
        checkResult.addErrData("userAnswer", userAnswerService.simpleInfo(userAnswer));
        return checkResult;
    }

    /**
     * 查看答题结果
     */
    @RequestMapping(path = "/show/{aid}")
    public CheckResult show(@PathVariable("aid") long aid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        CheckResult checkResult = CheckResult.newInstance();
        UserAnswer userAnswer = userAnswerService.getUserAnswer(aid);
        if (!ObjectUtils.isEmpty(userAnswer) && (user.getUid() != userAnswer.getUid())) {
            throw new CException(ErrorCode.ANSWER_ISNOT_BELONG);
        }
        checkResult.addErrData("details", userAnswerService.simpleInfoWithData(userAnswer));
        return checkResult;
    }

    /**
     * 获取一个作业的学生成绩单
     *
     * @return
     */
    @RequestMapping(path = "/transcript/{eid}", method = RequestMethod.POST)
    public CheckResult transcript(@PathVariable long eid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Exam exam = examService.getExamByEid(eid);
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }
        Page<UserAnswer> page = userAnswerService.findPage(exam, params);
        CheckResult checkResult = CheckResult.newInstance();

        //结果汇总
        ArrayList aList = page.getContent().stream().collect(ArrayList::new, (container, ua) -> {
            Map<String, Object> tmpMap = new HashMap<>();
            tmpMap.put("uid", ua.getUid());
            tmpMap.put("ansdateline", ua.getAnsdateline());
            tmpMap.put("anstotalscore", ua.getAnstotalscore());
            tmpMap.put("usedtime", ua.getUsedtime());
            tmpMap.put("aid", ua.getAid());
            tmpMap.put("eid", examService.findExamByEid(ua.getEid()).getEid());
            container.add(tmpMap);
        }, ArrayList::addAll);

        checkResult.addErrData("aList", aList);
        checkResult.addErrData("pageInfo", HMapper.pageRet(page));
        return checkResult;
    }

    /**
     * 根据作业编号获取学生答案信息
     *
     * @param eid
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(path = "/eanswer/{eid}", method = RequestMethod.POST)
    public CheckResult getAnswerByEid(@PathVariable long eid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Exam exam = examService.getExamByEid(eid);
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }

        Long stuid = params.getLong("stuid");
        UserAnswer userAnswer = userAnswerService.findUserAnswerByUidAndEid(stuid, eid);
        if (ObjectUtils.isEmpty(userAnswer)) {
            throw new CException(ErrorCode.USERANSWER_ISNOT_EXIST);
        }

        HMapper hMapper = userAnswerService.simpleInfoForCorrect(userAnswer);
        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("answer", hMapper);
        return checkResult;

    }

    /**
     * 获取指定作业下的答题记录
     *
     * @param eid
     * @param params
     * @return
     */
    @RequestMapping(path = "/alist/{eid}", method = RequestMethod.POST)
    public CheckResult getExamAnswerList(@PathVariable long eid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Exam exam = examService.getExamByEid(eid);
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }
        Page<UserAnswer> page = userAnswerService.findPage(exam, params);
        ArrayList<HMapper> answerCollect = page.getContent()
                .parallelStream().collect(ArrayList<HMapper>::new, (container, answer) ->
                                container.add(userAnswerService.simpleInfo(answer))
                        , ArrayList::addAll);
        Long tid = 0L;
        List<ExamRelation> examRelationList = examRelationDao.getExamRelationList(exam.getEid());
        for (ExamRelation examRelation : examRelationList) {
            if (examRelation.getTtype() == RelationType.FOLDER) {
                tid = examRelation.getTid();
            }
        }
        Set<Long> set = new HashSet<>();
        int totalusedtime = 0;
        List<UserAnswer> userAnswerList = userAnswerService.getUserAnswerListByEid(exam.getEid());
        List<Long> uids = params.arr2List("uids", Long[].class);
        if (!ObjectUtils.isEmpty(uids)) {
            userAnswerList = userAnswerList.stream().filter(userAnswer -> uids.contains(userAnswer.getUid())).collect(Collectors.toList());
        }
        for (UserAnswer ua : userAnswerList) {
            totalusedtime += ua.getUsedtime();
            if (ua.getStatus() == 1) {
                set.add(ua.getUid());
            }
        }
        int avgusedtime = 0;
        if (new Long(page.getTotalElements()).intValue() > 0)
            avgusedtime = totalusedtime / new Long(page.getTotalElements()).intValue();
        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("uids", set);
        checkResult.addErrData("esuject", exam.getEsubject());
        checkResult.addErrData("dtag", exam.getDtag());
        checkResult.addErrData("status", exam.getStatus());
        checkResult.addErrData("avgusedtime", avgusedtime);
        checkResult.addErrData("examtotalscore", exam.getExamtotalscore());
        checkResult.addErrData("limittime", exam.getLimittime());
        checkResult.addErrData("dateline", exam.getDateline());
        checkResult.addErrData("tid", tid);
        checkResult.addErrData("userAnswerList", answerCollect);
        checkResult.addErrData("pageInfo", HMapper.pageRet(page));
        return checkResult;
    }

    /**
     * 根据aid、uid获取用户答案
     *
     * @param aid
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(path = "/getbyaid/{aid}", method = RequestMethod.POST)
    public CheckResult getAnswerByAid(@PathVariable long aid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        UserAnswer userAnswer = userAnswerService.getUserAnswer(aid);
        if (ObjectUtils.isEmpty(userAnswer)) {
            throw new CException(ErrorCode.USERANSWER_ISNOT_EXIST);
        }

        HMapper answerMap = userAnswerService.simpleInfoForCorrect(userAnswer);
        CheckResult checkResult = CheckResult.newInstance();
        Exam exam = examService.getExamByEid(userAnswer.getEid());
        if (exam.getEtype() == ExamType.TSMART) {
            exam = examService.getsmartExam(exam);
        }
        List<Map<String, Object>> questionList = examService.getQuestionListByEid(exam.getEid()).stream().collect(ArrayList<Map<String, Object>>::new, (container, question) -> {
            Map<String, Object> tmpMap = new HashMap<>();
            tmpMap.put("question", questionService.getSimpleInfo(question));
            tmpMap.put("blanks", blankService.getBlankListByQid(question.getQid()));
            container.add(tmpMap);
        }, ArrayList::addAll);
        checkResult.addErrData("exam", examService.getExamSimpleInfo(exam.getEid()));
        checkResult.addErrData("questionList", questionList);
        checkResult.addErrData("userAnswer", answerMap);
        return checkResult;
    }

    /**
     * 统计每道题下的答题统计
     */
    @RequestMapping(value = "/statistics/{qid}", method = RequestMethod.GET)
    public CheckResult statisticsByqid(@PathVariable long qid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Question question = questionService.getQuestionByQid(qid);
        return questionService.statusticsExam(question);
    }

    @RequestMapping(value = "/deluseranswer/{aid}", method = RequestMethod.POST)
    public CheckResult delUseranswer(@PathVariable long aid, @RequestBody HMapper hMapper) throws Exception {
        User user = User.getUser(hMapper);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        UserAnswer userAnswer = userAnswerService.getUserAnswer(aid);
        if (ObjectUtils.isEmpty(userAnswer)) {
            throw new CException(ErrorCode.USERANSWER_ISNOT_EXIST);
        }
        userAnswer.setDtag(1);
        userAnswerService.dodelete(userAnswer);
        return CheckResult.newInstance().addErrData("status", "ok");
    }

}
