package net.ebh.exam.controller;

import com.mongodb.DBObject;
import net.ebh.exam.TempVo.User;
import net.ebh.exam.base.ExamType;
import net.ebh.exam.base.QueType;
import net.ebh.exam.bean.*;
import net.ebh.exam.service.*;
import net.ebh.exam.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by xh on 2017/4/16.
 * 错题集控制器
 */
@RestController
@RequestMapping("/errorbook")
@CrossOrigin
public class ErrorbookController {

    @Autowired
    private ErrorBookService errorBookService;


    /**
     * 获取错题集列表
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/errlist", method = RequestMethod.POST)
    public CheckResult errorList(@RequestBody HMapper params) throws Exception {
        CheckResult checkResult = CheckResult.newInstance();
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        params.put("uid", user.getUid());
        Long eid = params.getLong("eid");
        if (!ObjectUtils.isEmpty(eid)) {
            Exam exam = errorBookService.getExamByEid(eid);
            if (ObjectUtils.isEmpty(exam)) {
                throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
            }
            List<UserAnswer> userAnswerList = errorBookService.getUserAnswerCount(eid);
            List<Long> uids = params.arr2List("uids", Long[].class);
            long answerCount;
            if (!ObjectUtils.isEmpty(uids)) {
                answerCount = userAnswerList.stream().filter(userAnswer -> userAnswer.getStatus() == 1 && uids.contains(userAnswer.getUid())).count();
            } else {
                answerCount = userAnswerList.stream().filter(userAnswer -> userAnswer.getStatus() == 1).count();
            }
            checkResult.addErrData("answercounts", answerCount);
            params.put("dtag", 0);
            if (exam.getEtype() == ExamType.TSMART) {
                exam = errorBookService.getSmartExam(exam);
                params.put("eid", exam.getEid());
            }
        }
        Page<DBObject> page = errorBookService.getErrorList(params);
        List<Map<String, Object>> errbookList = errorBookService.getErrorMapList(page, params);
        checkResult.addErrData("errList", errbookList);
        checkResult.addErrData("pageInfo", HMapper.pageRet(page));
        return checkResult;
    }

    /**
     * 将一个试题关联上答题记录保存到错题集
     *
     * @param dqid   答题明细的id
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/addtobook/{dqid}", method = RequestMethod.POST)
    public CheckResult addToBook(@PathVariable("dqid") long dqid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }

        AnswerQueDetail answerQueDetail = errorBookService.getAnswerQueDetailByDqid(dqid);
        if (answerQueDetail == null) {
            throw new CException(ErrorCode.AQD_ISNOT_EXIST);
        }
        if (answerQueDetail.getUid() != user.getUid()) {
            throw new CException(ErrorCode.AQD_ISTNOT_BELONG);
        }

        Question question = errorBookService.getQuestionByQid(answerQueDetail.getQid());
        if (question == null || question.getDtag() == 1) {
            throw new CException(ErrorCode.QUESTION_ISNOT_EXIST);
        }

        params.put("uid", user.getUid());
        ErrorBook errorbook = errorBookService.findOneByUidAndDqidAndQid(user.getUid(), answerQueDetail.getDqid(), question.getQid());
        long errorid;
        if (ObjectUtils.isEmpty(errorbook)) {
            errorbook = errorBookService.doSave(answerQueDetail, user.getUid());
            errorid = errorbook.getErrorid();
        } else {
            errorbook.setStyle(1);//手动添加标示
            errorbook.setDtag(0);
            errorbook = errorBookService.save(errorbook);
            errorid = errorbook.getErrorid();
        }
        CheckResult checkResult = new CheckResult();
        checkResult.addErrData("errorid", errorid);
        return checkResult;
    }


    /**
     * 判断一个错题是否已经关联上某个答题详情加入了错题集
     *
     * @return
     */
    @RequestMapping(value = "/hasadded", method = RequestMethod.POST)
    public CheckResult hasAdded(@RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Long dqid = params.getLong("dqid");
        if (dqid == 0L) {
            throw new CException("dqid 为空");
        }
        AnswerQueDetail answerQueDetail = errorBookService.getAnswerQueDetailByDqid(dqid);
        if (answerQueDetail == null) {
            throw new CException(ErrorCode.AQD_ISNOT_EXIST);
        }
        if (answerQueDetail.getUid() != user.getUid()) {
            throw new CException(ErrorCode.AQD_ISTNOT_BELONG);
        }

        Question question = errorBookService.getQuestionByQid(answerQueDetail.getQid());
        if (ObjectUtils.isEmpty(question) || question.getDtag() == 1) {
            throw new CException(ErrorCode.QUESTION_ISNOT_EXIST);
        }
        ErrorBook errorbook = errorBookService.findOneByUidAndDqidAndQid(user.getUid(), answerQueDetail.getDqid(), question.getQid());
        String added = "0";
        if (errorbook != null && errorbook.getDtag() == 0) {
            added = "1";
        }
        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("added", added);
        return checkResult;
    }

    /**
     * 获取答题结果集
     *
     * @param qid
     * @param hMapper
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/answercount/{qid}", method = RequestMethod.POST)
    public CheckResult getAnswerCount(@PathVariable long qid, @RequestBody HMapper hMapper) throws Exception {
        CheckResult checkResult = CheckResult.newInstance();
        List<Long> uids = hMapper.arr2List("uids", Long[].class);
        User user = User.getUser(hMapper);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Question question = errorBookService.getQuestionByQid(qid);
        List<AnswerQueDetail> answerDetailList = errorBookService.getAnswerDetailList(qid);
        Map<String, List<AnswerQueDetail>> map;
        if (!ObjectUtils.isEmpty(uids)) {
            map = answerDetailList.stream().filter(answerQueDetail -> uids.contains(answerQueDetail.getUid())).collect(Collectors.groupingBy(AnswerQueDetail::getChoicestr));
        } else {
            map = answerDetailList.stream().collect(Collectors.groupingBy(AnswerQueDetail::getChoicestr));
        }
        List<Map<String, Object>> list = errorBookService.getMapList(map);
        checkResult.addErrData("simpleQue", errorBookService.getSimpleQuestion(question));
        checkResult.addErrData("answerDetaillist", list);
        return checkResult;
    }

    /**
     * 根据qid分页获取答题列表
     *
     * @param hMapper
     * @return
     * @throws Exception
     */
    @RequestMapping(path = "/getanswersbychoice", method = RequestMethod.POST)
    public CheckResult getAnswerListBychoice(@RequestBody HMapper hMapper) throws Exception {
        User user = User.getUser(hMapper);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        CheckResult checkResult = new CheckResult();
        Page<AnswerQueDetail> page = errorBookService.getAnswerListBypage(hMapper);
        List<Map<String, Object>> answerList = errorBookService.getAnswerList(page);
        checkResult.addErrData("answerList", answerList);
        checkResult.addErrData("pageinfo", HMapper.pageRet(page));
        return checkResult;
    }

    /**
     * 根据eid获取作业中的错题
     *
     * @param hMapper
     * @return
     */
    @RequestMapping(value = "/errlistbyexam/{eid}", method = RequestMethod.POST)
    public CheckResult getErrlistByexam(@PathVariable long eid, @RequestBody HMapper hMapper) throws Exception {
        User user = User.getUser(hMapper);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Exam exam = errorBookService.getExamByEid(eid);
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }
        Long uid = user.getUid();

        List<UserAnswer> userAnswers = errorBookService.getUserAnswerListByEid(eid).stream().filter(userAnswer -> userAnswer.getUid() == uid).collect(Collectors.toList());
        if (userAnswers.size() < 1) {
            throw new CException(ErrorCode.USERANSWER_ISNOT_EXIST);
        }
        UserAnswer ua = userAnswers.stream().findFirst().get();
        CheckResult checkResult = CheckResult.newInstance();
        List<AnswerQueDetail> answerQueDetails = errorBookService.getAnswerQueDetailList(ua.getAid());
        List<AnswerQueDetail> answerQueDetailList = answerQueDetails.stream().filter(answerQueDetail -> answerQueDetail.getAllright() != 1 && answerQueDetail.getStatus() == 1).collect(Collectors.toList());
        List<Map<String, Object>> errList = answerQueDetailList.stream().collect(ArrayList<Map<String, Object>>::new, (list, answerQueDetail) -> {
            Map<String, Object> tmpmap = new HashMap<>();
            tmpmap.put("dqid", answerQueDetail.getDqid());
            tmpmap.put("qid", answerQueDetail.getQid());
            tmpmap.put("totalscore", answerQueDetail.getTotalscore());
            tmpmap.put("choicestr", answerQueDetail.getChoicestr());
            tmpmap.put("allright", answerQueDetail.getAllright());
            tmpmap.put("status", answerQueDetail.getStatus());
            tmpmap.put("uid", answerQueDetail.getUid());
            tmpmap.put("remark", answerQueDetail.getRemark());
            tmpmap.put("markid", answerQueDetail.getMarkuid());
            list.add(tmpmap);
        }, ArrayList::addAll);
        ArrayList<Map<String, Object>> questionList = errorBookService.getQuestionListByEid(exam.getEid()).stream().collect(ArrayList<Map<String, Object>>::new, (container, question) -> {
            Map<String, Object> tmpMap = new HashMap<>();
            tmpMap.put("question", errorBookService.getSimpleQuestion(question));
            container.add(tmpMap);
        }, ArrayList::addAll);
        checkResult.addErrData("questionList", questionList);
        checkResult.addErrData("errlist", errList);
        checkResult.addErrData("exam", errorBookService.getExamSimpleInfo(eid));
        return checkResult;
    }

    /**
     * 删除错题
     *
     * @param errorid
     * @param hMapper
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/delete/{errorid}", method = RequestMethod.POST)
    public CheckResult deleteErrorBook(@PathVariable long errorid, @RequestBody HMapper hMapper) throws Exception {
        User user = User.getUser(hMapper);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        ErrorBook errorBook = errorBookService.getErrorBookByErrorId(errorid);
        if (ObjectUtils.isEmpty(errorBook)) {
            throw new CException(ErrorCode.ERRORBOOK_ISNOT_EXIST);
        }
        errorBook.setStyle(1);
        errorBook.setDtag(0);
        errorBookService.updateErrorBook(errorBook);
        return CheckResult.newInstance().addErrData("status", "ok");
    }
}
