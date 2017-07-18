package net.ebh.exam.controller;

import net.ebh.exam.TempVo.User;
import net.ebh.exam.base.QueType;
import net.ebh.exam.bean.*;
import net.ebh.exam.dao.QuestionDao;
import net.ebh.exam.service.AnswerQueDetailService;
import net.ebh.exam.service.ExamService;
import net.ebh.exam.service.QuestionService;
import net.ebh.exam.service.UserAnswerService;
import net.ebh.exam.util.CException;
import net.ebh.exam.util.CheckResult;
import net.ebh.exam.util.ErrorCode;
import net.ebh.exam.util.HMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zkq on 2016/5/25.
 * 批改控制器
 */
@RestController
@RequestMapping(value = "/correct")
public class CorrectController {
    @Autowired
    AnswerQueDetailService answerQueDetailService;
    @Autowired
    UserAnswerService userAnswerService;
    @Autowired
    ExamService examService;
    @Autowired
    QuestionService questionService;

    /**
     * 批改单条学生答题
     *
     * @param dqid
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(path = "/one/{dqid}", method = RequestMethod.POST)
    public CheckResult correctQue(@PathVariable long dqid, @RequestBody HMapper params) throws Exception {
        CheckResult checkResult = CheckResult.newInstance();
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        AnswerQueDetail answerQueDetail = answerQueDetailService.getById(dqid);
        if (ObjectUtils.isEmpty(answerQueDetail)) {
            throw new CException(ErrorCode.AQD_ISNOT_EXIST);
        }
        answerQueDetail = answerQueDetailService.correctOne(answerQueDetail, user, params);
        checkResult.addErrData("answerQueDetail", answerQueDetail);
        return checkResult;
    }

    /**
     * 整卷批阅
     *
     * @param hMapper
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/all", method = RequestMethod.POST)
    public CheckResult correctAllQue(@RequestBody HMapper hMapper) throws Exception {
        User user = User.getUser(hMapper);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Long eid = hMapper.getLong("eid");
        Exam exam = examService.getExamByEid(eid);
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }
        List<AnswerQueDetail> list = new ArrayList<>();
        UserAnswer userAnswer = hMapper.getObject("userAnswer", UserAnswer.class);

        if (ObjectUtils.isEmpty(userAnswer)) {
            throw new CException(ErrorCode.USERANSWER_ISNOT_EXIST);
        }
        List<AnswerQueDetail> answerQueDetailList = HMapper.parseData2List(userAnswer.getData(), "answerqueDetailList", AnswerQueDetail[].class);
        if (answerQueDetailList.size() < 1) {
            throw new CException(ErrorCode.AQD_ISNOT_EXIST);
        }
        for (AnswerQueDetail answerQueDetail : answerQueDetailList) {
            AnswerQueDetail dbanswerQueDetail = answerQueDetailService.getAnswerQueDetailById(answerQueDetail.getDqid());
            long dbaid = dbanswerQueDetail.getAid();
            if (dbaid != userAnswer.getAid()) {
                throw new CException(ErrorCode.ANSWERER_ISNOT_MATCH);
            }
            if (StringUtils.isEmpty(answerQueDetail.getData())) {
                answerQueDetail.setData("");
            }
            dbanswerQueDetail.setData(answerQueDetail.getData());
            if (answerQueDetail.getRemark() != null) {
                dbanswerQueDetail.setRemark(answerQueDetail.getRemark());
            }
            if (answerQueDetail.getStatus() != 1) {
                dbanswerQueDetail.setStatus(1);
            }
            dbanswerQueDetail.setTotalscore(answerQueDetail.getTotalscore());
            if (ObjectUtils.isEmpty(answerQueDetail)) {
                throw new CException(ErrorCode.AQD_ISNOT_EXIST);
            }
            Question question = questionService.getQuestionByQid(dbanswerQueDetail.getQid());
            if (question.getQueType().toString().startsWith("X") || question.getQueType() == QueType.H) {
                dbanswerQueDetail.setTotalscore(answerQueDetail.getTotalscore());
                dbanswerQueDetail.setAllright(answerQueDetail.getAllright());
                answerQueDetail = answerQueDetailService.correctQtypeXandH(dbanswerQueDetail, user, hMapper);
            } else {
                answerQueDetail = answerQueDetailService.correctAll(dbanswerQueDetail, user, hMapper);
            }

            list.add(answerQueDetail);
        }
        //获取下一个回答的aid
        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("answerQueDetailList", list);
        List<UserAnswer> answerList = userAnswerService.getUserAnswerListByEid(exam.getEid());
        List<UserAnswer> userAnswerList = answerList.stream().filter(userAnswer1 -> userAnswer1.getCorrectrat() != 100 && userAnswer1.getStatus() == 1).collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(userAnswerList)) {
            checkResult.addErrData("nextAid", userAnswerList.stream().findAny().get().getAid());
        } else {
            checkResult.addErrData("nextAid", 0);
        }

        return checkResult;
    }

}
