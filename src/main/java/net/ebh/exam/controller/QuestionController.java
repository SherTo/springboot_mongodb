package net.ebh.exam.controller;


import net.ebh.exam.TempVo.User;
import net.ebh.exam.bean.AnswerQueDetail;
import net.ebh.exam.bean.Question;
import net.ebh.exam.bean.UserAnswer;
import net.ebh.exam.dao.AnswerBlankDetailDao;
import net.ebh.exam.service.*;
import net.ebh.exam.util.CException;
import net.ebh.exam.util.CheckResult;
import net.ebh.exam.util.ErrorCode;
import net.ebh.exam.util.HMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@RestController
@CrossOrigin
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    QuestionService questionService;
    @Autowired
    AnswerQueDetailService answerQueDetailService;
    @Autowired
    UserAnswerService userAnswerService;
    @Autowired
    AnswerBlankDetailService answerBlankDetailService;
    @Autowired
    BlankService blankService;

    /**
     * 根据试题qid获取试题详情
     */
    @PostMapping(path = "/detail/{qid}")
    public CheckResult getQuestionByQid(@PathVariable long qid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Question question = questionService.getQuestionByQid(qid);
        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("question", questionService.getInfoForEdit(question));
        return checkResult;
    }


    /**
     * 获取一道试题下所有的答题详情
     *
     * @return
     */
    @PostMapping(path = "/aqd/{qid}")
    public CheckResult getAnswerQueDetailList(@PathVariable long qid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Question question = questionService.getQuestionByQid(qid);
        List<AnswerQueDetail> aqdList = answerQueDetailService.getAnswerQueDetailListByQid(qid);
        Long correctcount = aqdList.stream().filter(answerQueDetail -> answerQueDetail.getStatus() == 1).count();
        if (ObjectUtils.isEmpty(question)) {
            throw new CException(ErrorCode.QUESTION_ISNOT_EXIST);
        }

        CheckResult checkResult = CheckResult.newInstance();

        Page<AnswerQueDetail> page = answerQueDetailService.getPageByQuestionAndParam(question, params);
        List<Map<String, Object>> answerQueDetailList = page.getContent().stream().collect(ArrayList::new, (list, answerQueDetail) -> {
            Map tmp = new HashMap();
            UserAnswer userAnswer = userAnswerService.getUserAnswerByAid(answerQueDetail.getAid());
            tmp.put("usedtime", userAnswer.getUsedtime());
            tmp.put("ansdateline", userAnswer.getAnsdateline());
            tmp.put("dqid", answerQueDetail.getDqid());
            tmp.put("totalscore", answerQueDetail.getTotalscore());
            tmp.put("answerBlankDetails", answerBlankDetailService.getBlankListByDqid(answerQueDetail.getDqid()));
            tmp.put("choicestr", answerQueDetail.getChoicestr());
            tmp.put("data", answerQueDetail.getData());
            tmp.put("allright", answerQueDetail.getAllright());
            tmp.put("status", answerQueDetail.getStatus());
            tmp.put("uid", answerQueDetail.getUid());
            tmp.put("qid", answerQueDetail.getQid());
            tmp.put("aid", answerQueDetail.getAid());
            tmp.put("remark", answerQueDetail.getRemark());
            tmp.put("markuid", answerQueDetail.getMarkuid());
            list.add(tmp);
        }, ArrayList::addAll);
        checkResult.addErrData("question", questionService.getSimpleInfo(question));
        checkResult.addErrData("answercounts", answerQueDetailService.getAnswerQueDetailListByQid(question.getQid()).size());
        checkResult.addErrData("correctcounts", correctcount);
        checkResult.addErrData("blanks", blankService.getBlankListByQid(question.getQid()));
        checkResult.addErrData("aqdList", answerQueDetailList);
        checkResult.addErrData("pageInfo", HMapper.pageRet(page));

        return checkResult;
    }


    /**
     * 修改试题题干
     *
     *
     * @param qid
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(path = "/oeditq/{qid}", method = RequestMethod.POST)
    public CheckResult editQuesOnly(@PathVariable long qid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }

        Question question = questionService.getQuestionByQid(qid);
        if (ObjectUtils.isEmpty(question)) {
            throw new CException(ErrorCode.QUESTION_ISNOT_EXIST);
        }

        if (user.getUid() != question.getUid()) {
            throw new CException(ErrorCode.NOT_AOLLOW_EDIT);
        }

        String qsubject = params.getString("qsubject", true);
        if (!StringUtils.isEmpty(qsubject)) {
            question.setQsubject(qsubject);
        }

        Integer level = params.getInteger("level");
        if (level != null) {
            question.setLevel(level);
        }

        String extdata = params.getString("extdata");
        if (extdata != null) {
            question.setExtdata(extdata);
        }
        return CheckResult.newInstance("0", "操作成功");
    }

    /**
     * 根据qid获取关联试题的知识点
     *
     * @param hMapper
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/chapter", method = RequestMethod.POST)
    public CheckResult getQueschapters(@RequestBody HMapper hMapper) throws Exception {
        User user = User.getUser(hMapper);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        return questionService.getChapters(hMapper);
    }
}
