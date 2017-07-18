package net.ebh.exam.controller;

import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import net.ebh.exam.TempVo.Condition;
import net.ebh.exam.TempVo.QueMap;
import net.ebh.exam.TempVo.User;
import net.ebh.exam.base.ExamType;
import net.ebh.exam.base.QueType;
import net.ebh.exam.bean.*;
import net.ebh.exam.dao.AnswerQueDetailDao;
import net.ebh.exam.service.*;
import net.ebh.exam.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Null;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by xh on 2017/4/14.
 */
@RestController
@RequestMapping("/exam")
public class ExamController {
    @Autowired
    ExamService examService;
    @Autowired
    UserAnswerService userAnswerService;
    @Autowired
    QuestionService questionService;
    @Autowired
    AnswerQueDetailService answerQueDetailService;
    @Autowired
    BlankService blankService;

    /**
     * 根据eid获取整个作业概要和作答数量
     *
     * @param eid
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/summary/{eid}")
    public CheckResult findExamByEid(@PathVariable long eid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        CheckResult checkResult = CheckResult.newInstance();
        Exam exam = examService.findExamByEid(eid);
        checkResult.addErrData("exam", exam);
        return checkResult;
    }

    /**
     * 保存作业
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping(path = "/save")
    public CheckResult saveExam(@RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Exam exam = params.getObject("exam", Exam.class);
        exam = examService.doSave(exam, params);
        CheckResult checkResult = CheckResult.newInstance();
        return checkResult.addErrData("exam", exam);
    }

    /**
     * 智能作业数量不足时(选择直接布置作业)
     *
     * @param eid
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/checkexam/{eid}")
    public CheckResult checkExam(@PathVariable long eid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Exam texam = examService.findExamByEid(eid);
        if (ObjectUtils.isEmpty(texam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }
        //只有老师智能作业才能操作
        if (texam.getEtype() != ExamType.TSMART) {
            throw new CException(ErrorCode.EXAM_ISNOT_SMART);
        }
        Exam exam = examService.parseSmartExam(texam, params);
        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("exam", exam);
        return checkResult;
    }


    /**
     * 判断题库是否有足够的试题布置智能作业
     * (题库足够返回1，不足返回condition和题库数量kucount)
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping(value = "cansmart")
    public CheckResult hasEnoughQuestion(@RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Condition[] conditionList = params.getObject("conditionList", Condition[].class);
        if (ObjectUtils.isEmpty(conditionList)) {
            throw new CException(ErrorCode.CONDITIONLIST_IS_NULL);
        }
        List<HMapper> hMapperList = examService.checkKuQuestionByCondition(Arrays.asList(conditionList), user);
        CheckResult checkResult = CheckResult.newInstance();
        //如果题库数量足够返回OK，反之，返回condition和题库的数量
        if (ObjectUtils.isEmpty(hMapperList)) {
            checkResult.addErrData("cansmart", Constant.OK);
        } else {
            checkResult.addErrData("hmapperList", hMapperList);
        }
        return checkResult;
    }

    /**
     * 获取教师作业列表
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/telist")
    public CheckResult ExamListForTeacher(@RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        params.put("user", user);
        return examService.getExamListForTeacher(params);
    }

    /**
     * 学生作业列表
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/selist", method = RequestMethod.POST)
    public CheckResult ExamListForStudent(@RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        params.put("user", user);
        return examService.getExamListForStudent(params);
    }

    /**
     * 分析一份作业
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/efenxi", method = RequestMethod.POST)
    public CheckResult efenxin(@RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Long eid = params.getLong("eid");
        if (eid == null) {
            throw new CException(ErrorCode.EXAM_EID_ISNULL);
        }
        params.put("user", user);
        Exam exam = examService.getExamByEid(eid);
        if (exam.getEtype() == ExamType.TSMART) {
            exam = examService.getsmartExam(exam);
        }
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }

        String bytype = params.getString("bytype");
        if (StringUtils.isEmpty(bytype)) {
            bytype = "que";
        }

        List<Map<String, Object>> efenxi = new ArrayList<>();

        if ("que".equals(bytype)) {
            //按照单个试题分析
            efenxi = examService.efenxi(exam, params, 0);
        } else if ("relationship".equals(bytype)) {
            //按照知识点分析
            efenxi = examService.efenxi(exam, params, 1);
        } else if ("level".equals(bytype)) {
            //按照分数段分析优秀率
            efenxi = examService.efenxi(exam, params, 2);
        } else if ("quetype".equals(bytype)) {
            //按照题型统计
            efenxi = examService.efenxi(exam, params, 3);
        }

        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("efenxi", efenxi);
        return checkResult;
    }

    /**
     * 获取作业详情，提供教师编辑使用
     *
     * @param eid
     * @param params
     * @throws Exception
     */
    @RequestMapping(path = "/detail/foredit/{eid}", method = RequestMethod.POST)
    public CheckResult detailForEdit(@PathVariable("eid") long eid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Exam exam = examService.getExamByEid(eid);
        List<ExamRelation> examRelationList = HMapper.parseData2List(exam.getData(), "relationSet", ExamRelation[].class);
        List<Question> questionList = null;
        List<Condition> conditionList = null;
        params.clear();
        if (exam.getEtype() == ExamType.COMMON) {
            questionList = HMapper.parseData2List(exam.getData(), "questionList", Question[].class);
            int size = questionList.size();
            List<Question> questions = examService.getQuestionListByEid(exam.getEid());
            if (questions.size() > 0) {
                for (int i = 0; i < size; i++) {
                    questionList.get(i).setQid(questions.get(i).getQid());
                }
            }
            params.put("questionList", JSONArray.toJSON(questionList));
        } else if (exam.getEtype() == ExamType.SSMART || exam.getEtype() == ExamType.TSMART) {
            conditionList = HMapper.parseData2List(exam.getData(), "conditionList", Condition[].class);
            params.put("conditionList", JSONArray.toJSON(conditionList));
        }
        params.put("relationSet", JSONArray.toJSON(examRelationList));
        exam.setData(params.toString());
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }
        if (exam.getUid() != user.getUid()) {
            throw new CException(ErrorCode.NOT_AOLLOW_EDIT);
        }

        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("exam", exam);
        checkResult.addErrData("relationSet", examService.getExamRelationByEid(exam.getEid()));
        checkResult.addErrData("examdata", exam.getData());
        return checkResult;
    }

    /**
     * 获取作业信息(学生做作业)
     *
     * @param eid
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(path = "/detail/fordo/{eid}", method = RequestMethod.POST)
    public CheckResult detailForStuDo(@PathVariable("eid") long eid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        CheckResult checkResult = CheckResult.newInstance();
        Exam exam = examService.getExamByEid(eid);
        if (exam.getEtype() == ExamType.TSMART) {
            exam = examService.getsmartExam(exam);
        }
        //写入作业信息
        checkResult.addErrData("exam", examService.getExamSimpleInfo(exam.getEid()));
        //写入作业试题信息
        List<Map<String, Object>> questionList = examService.getQuestionList(exam.getEid());
        checkResult.addErrData("questionList", questionList);

        //如果有答题查看用户答题信息
        UserAnswer userAnswer = userAnswerService.findUserAnswerByUidAndEid(user.getUid(), exam.getEid());
        if (!ObjectUtils.isEmpty(userAnswer)) {
            HMapper ua = userAnswerService.getSimpleInfo(userAnswer);
            if (userAnswer.getStatus() == 1) {
                ua.put("answerQueDetails", examService.getSimpleAQD(userAnswer));
            }
            checkResult.addErrData("userAnswer", ua);
        }
        return checkResult;
    }

    /**
     * 根据教师智能作业编号生成巩固练习
     *
     * @param eid    教师智能作业编号
     * @param params 请求参数 k 必须
     * @return
     * @throws Exception
     */
    @RequestMapping(path = "/getsmart/{eid}", method = RequestMethod.POST)
    public CheckResult getSmartExam(@PathVariable("eid") Long eid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Exam exam = examService.getExamByEid(eid);
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }
        if (exam.getEtype() != ExamType.TSMART) {
            throw new CException(ErrorCode.EXAM_ISNOT_SMART);
        }
        if (exam.getStatus() != 1) {
            throw new CException(ErrorCode.EXAM_IS_DRAFT);
        }
        CheckResult checkResult = CheckResult.newInstance();
        params.put("uid", user.getUid());
        ExamType etype = params.getObject("etype", ExamType.class);
        if (etype == ExamType.EXERCISE) {
            Exam smartExam = examService.getSmartExam(exam, params);
            examService.saveExam(smartExam);
            examService.parseRelation(smartExam);
            List<Condition> conditionList = HMapper.parseData2List(exam.getData(), "conditionList", Condition[].class);
            examService.parseCondition(conditionList, smartExam);
        }
        //写入作业试题信息
        List<Question> questions = examService.getQuestionListByEid(examService.getsmartExam(exam).getEid());
        List questionList = questionService.getQuestionInfo(questions);
        checkResult.addErrData("questionList", questionList);

        //查看用户答题信息
        if (exam.getEtype() == ExamType.TSMART) {
            exam = examService.getsmartExam(exam);
        }
        UserAnswer userAnswer = userAnswerService.findUserAnswerByUidAndEid(params.getLong("uid"), exam.getEid());
        if (!ObjectUtils.isEmpty(userAnswer)) {
            HMapper ua = userAnswerService.simpleInfoWithData(userAnswer);
            if (userAnswer.getStatus() == 1) {
                ua.put("answerQueDetails", examService.getSimpleAQD(userAnswer));
            }
            checkResult.addErrData("userAnswer", ua);
        }
        checkResult.addErrData("exam", examService.getExamSimpleInfo(eid));
        return checkResult;
    }

    /**
     * 获取教师单个智能作业下学生的生成的智能作业列表
     *
     * @param eid
     * @param params
     * @return
     */
    @RequestMapping(value = "/smartexam/list/{eid}", method = RequestMethod.POST)
    public CheckResult smartExamListForStu(@PathVariable("eid") long eid, @RequestBody HMapper params) throws
            Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Exam exam = examService.getExamByEid(eid);
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }
        if (exam.getEtype() == ExamType.TSMART) {
            throw new CException(ErrorCode.EXAM_ISNOT_SMART);
        }
        Page<Exam> page = examService.stuSmartExamList(exam, params);
        List<Exam> examList = page.getContent();
        List<Map<String, Object>> retList = new ArrayList<>();
        Map<String, Object> tmpMap = examService.getTmpMap(examList);
        retList.add(tmpMap);
        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("examList", retList);
        checkResult.addErrData("pageInfo", HMapper.pageRet(page));
        return checkResult;
    }

    /**
     * 教师获取指定智能作业下学生生成的第一份作业
     *
     * @param eid
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/allsmartexam/list/{eid}", method = RequestMethod.POST)
    public CheckResult smartExamListForTeacher(@PathVariable("eid") long eid, @RequestBody HMapper params) throws
            Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Exam exam = examService.getExamByEid(eid);
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }
        if (exam.getEtype() == ExamType.TSMART) {
            throw new CException(ErrorCode.EXAM_ISNOT_SMART);
        }
        Page<Exam> page = examService.stuAllSmartExamList(exam, params);
        List<Exam> examList = page.getContent();
        List<Map<String, Object>> retList = new ArrayList<>();
        Map<String, Object> tmpMap = examService.getTmpMap(examList);
        retList.add(tmpMap);
        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("examList", retList);
        checkResult.addErrData("pageInfo", HMapper.pageRet(page));
        return checkResult;
    }

    /**
     * 获取指定作业简单信息
     *
     * @param eid
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/simpleinfo/{eid}", method = RequestMethod.POST)
    public CheckResult getExamSimpleInfo(@PathVariable("eid") long eid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        CheckResult checkResult = CheckResult.newInstance();
        HMapper info = examService.getExamSimpleInfo(eid);
        checkResult.addErrData("info", info);
        return checkResult;
    }

    /**
     * 删除作业
     *
     * @param eid
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/delete/{eid}")
    public CheckResult doDelete(@PathVariable("eid") long eid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        examService.doDelete(eid);
        return CheckResult.newInstance().addErrData("status", "ok");
    }


    /**
     * 修改作业信息
     *
     * @param eid
     * @param params
     * @return
     */
    @PostMapping(value = "/oedite/{eid}")
    public CheckResult onlyEidtExam(@PathVariable("eid") long eid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Exam exam = examService.getExamByEid(eid);
        if (exam == null) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }
        if (exam.getStatus() == 0) {
            throw new CException(ErrorCode.EXAM_IS_DRAFT);
        }
        examService.canEdit(exam, params);
        Integer isclass = params.getInteger("isclass");
        if (isclass != null) {
            exam.setIsclass(isclass);
        }
        String estype = params.getString("estype");
        if (!StringUtils.isEmpty(estype)) {
            exam.setEstype(estype);
        }
        String data = params.getString("data");
        exam.setData(data);
        CheckResult checkResult = CheckResult.newInstance();
        //如果为老师智能作业
        if (exam.getEtype() == ExamType.TSMART) {
            exam = examService.updateSmartExam(exam, params);
        } else if (exam.getEtype() == ExamType.COMMON) {
            examService.updateExam(exam);
        }
        return checkResult.addErrData("exam", exam);
    }

    /**
     * 获取老师布置的作业的数量
     *
     * @param hMapper
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/texamcount")
    public CheckResult getTexamCount(@RequestBody HMapper hMapper) throws Exception {
        User user = User.getUser(hMapper);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        long countByToday = examService.getExamCountByToday(hMapper, user);
        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("count", countByToday);
        return checkResult;
    }

    /**
     * 题目分析概要老师
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/efenxi/summary")
    public CheckResult getExamSummary(@RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Long eid = params.getLong("eid");
        if (ObjectUtils.isEmpty(eid)) {
            throw new CException(ErrorCode.EXAM_EID_ISNULL);
        }
        Exam exam = examService.getExamByEid(eid);
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }
        if (exam.getEtype() == ExamType.TSMART) {
            exam = examService.getsmartExam(exam);
        }
        CheckResult checkResult = examService.efenxiSummary(exam, params);
        return checkResult;
    }

    /**
     * 分析学生作业概要
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/efenxi/stusummary")
    public CheckResult getExamStuSummary(@RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Long eid = params.getLong("eid");
        if (ObjectUtils.isEmpty(eid)) {
            throw new CException(ErrorCode.EXAM_EID_ISNULL);
        }
        Exam exam = examService.getExamByEid(eid);
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }
        CheckResult checkResult = examService.efenxiStuSummary(exam, user.getUid(), params);
        return checkResult;
    }

    /**
     * 获取巩固练习
     *
     * @param eid
     * @param hMapper
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/getexeinfo/{eid}")
    public CheckResult getexeInfo(@PathVariable long eid, @RequestBody HMapper hMapper) throws Exception {
        User user = User.getUser(hMapper);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        hMapper.put("user", user);
        Exam exam = examService.getExamByEid(eid);
        exam = examService.getsmartExam(exam);
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException(ErrorCode.EXAM_ISNOT_EXIST);
        }

        UserAnswer userAnswer = userAnswerService.getUserAnswerListByEid(exam.getEid()).stream().filter(ua -> ua.getUid() == user.getUid()).findFirst().get();
        if (ObjectUtils.isEmpty(userAnswer)) {
            throw new CException(ErrorCode.EXAM_ISNOT_ANSWER);
        }
        List<QueMap> aqdlist = examService.getAnswerQuestionDetailList(exam, userAnswer);
        Map<String, List<QueMap>> qmap = aqdlist.stream().collect(Collectors.groupingBy(QueMap::getQuetype));
        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("exercise", qmap);
        Page<Exam> page = examService.getexeList(exam, hMapper);
        List<Map<String, Object>> examList = examService.getExamList(page);
        checkResult.addErrData("exelist", examList);
        return checkResult;
    }


    /**
     * 删除巩固练习
     *
     * @param eid
     * @param hMapper
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/delexe/{eid}")
    public CheckResult delexe(@PathVariable long eid, @RequestBody HMapper hMapper) throws Exception {
        User user = User.getUser(hMapper);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Exam exam = examService.getExamByEid(eid);
        if (ObjectUtils.isEmpty(exam)) {
            throw new CException("作业不存在");
        }
        examService.doDelete(exam.getEid());
        return CheckResult.newInstance().addErrData("success", "删除成功");
    }

    /**
     * 学生未完成数量
     *
     * @param hMapper
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/unfinishcount")
    public CheckResult getunfinishcount(@RequestBody HMapper hMapper) throws Exception {
        User user = User.getUser(hMapper);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        hMapper.put("user", user);
        int count = examService.getExamListforstu(hMapper);
        return CheckResult.newInstance().addErrData("unfinishedcount", count);
    }

    /**
     * 获取所有作业（根据个别需求）
     *
     * @param hMap
     * @return
     */
    @PostMapping(value = "/getlist")
    public CheckResult getList(@RequestBody HMapper hMap) throws Exception {
        User user = User.getUser(hMap);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Page<Exam> page = examService.getExamDao().getExamListForTeacher(hMap);
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        page.getContent().forEach(exam -> {
            Map<String, Object> map = examService.getExam(exam);
            list.add(map);
        });
        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("examList", list);
        checkResult.addErrData("pageInfo", HMapper.pageRet(page));
        return checkResult;
    }


    /**
     * 分页统计作业平均得分率
     *
     * @param hMapper
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/avgscorerat")
    public CheckResult avgScoreRat(@RequestBody HMapper hMapper) throws Exception {
        User user = User.getUser(hMapper);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        hMapper.put("uid", user.getUid());
        hMapper.put("dtag", 0);
        hMapper.put("status", 1);
        Page<Exam> page = examService.getExamDao().getExamListForTeacher(hMapper);
        List<Exam> examList = page.getContent();
        ArrayList<Object> list = new ArrayList<>();
        examList.forEach(exam -> {
            List<UserAnswer> userAnswerList = userAnswerService.getUserAnswerListByEid(exam.getEid());
            Double avgscore = 0D;
            if (!ObjectUtils.isEmpty(userAnswerList)) {
                avgscore = userAnswerList.stream().collect(Collectors.averagingDouble(UserAnswer::getAnstotalscore));
            }
            HashMap<Object, Object> map = new HashMap<>();
            Double avgscoreRat = 0D;
            if (exam.getExamtotalscore() > 0) {
                avgscoreRat = avgscore / exam.getExamtotalscore();
            }
            map.put("eid", exam.getEid());
            map.put("esubject", exam.getEsubject());
            map.put("avgscoreRat", avgscoreRat);
            list.add(map);
        });
        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("examList", list);
        checkResult.addErrData("pageInfo", HMapper.pageRet(page));
        return checkResult;
    }

    /**
     * 根据eid分页获取大于eid之后的作业
     *
     * @param hMapper
     * @return
     * @throws Exception
     */
    @PostMapping(value = "allexam")
    public CheckResult getexamPageByEid(@RequestBody HMapper hMapper) throws Exception {
        User user = User.getUser(hMapper);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Page<Exam> page = examService.getExamDao().getExamPageByEid(hMapper);
        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("examList", page.getContent());
        checkResult.addErrData("pageInfo", HMapper.pageRet(page));
        return checkResult;
    }

    /**
     * 根据eid获取整个作业概要
     *
     * @param eid    作业编号
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(path = "/detail/forshow/{eid}", method = RequestMethod.POST)
    public CheckResult findDetailOneByEid(@PathVariable Long eid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Long[] uids = params.getObject("uids", Long[].class);
        CheckResult checkResult = CheckResult.newInstance();
        Exam exam = examService.getExamByEid(eid);

        HMapper mapper = new HMapper();
        mapper.put("eid", exam.getEid());
        mapper.put("etype", exam.getEtype());
        mapper.put("uid", exam.getUid());
        mapper.put("data", exam.getData());
        mapper.put("dateline", exam.getDateline());
        mapper.put("ansendtime", exam.getAnsendtime());
        mapper.put("canpusherror", exam.getCanpusherror());
        mapper.put("ansstarttime", exam.getAnsstarttime());
        mapper.put("examendtime", exam.getExamendtime());
        mapper.put("status", exam.getStatus());
        mapper.put("esubject", exam.getEsubject());
        mapper.put("limittime", exam.getLimittime());
        mapper.put("stucancorrect", exam.getStucancorrect());
        int correct = params.getIntValue("forcorrect");
        if (exam.getEtype() == ExamType.TSMART) {
            exam = examService.getsmartExam(exam);
        }
        List<Question> questionList = examService.getQuestionListByEid(exam.getEid());
        //单题批阅时
        if (correct == 1) {
            List<Map<String, Object>> list = new ArrayList<>();
            questionList.forEach(question -> {
                if (question.getQueType() != QueType.A && question.getQueType() != QueType.B && question.getQueType() != QueType.D) {
                    Map<String, Object> tmpMap = new HashMap<>();
                    tmpMap.put("qid", question.getQid());
                    tmpMap.put("quetype", question.getQueType());
                    Long count;
                    List<AnswerQueDetail> answerQueDetailList = answerQueDetailService.getAnswerQueDetailListByQid(question.getQid());
                    if (!ObjectUtils.isEmpty(uids)) {
                        count = answerQueDetailList.stream().filter(answerQueDetail -> answerQueDetail.getStatus() == 1 && Arrays.asList(uids).contains(answerQueDetail.getUid())).count();
                        tmpMap.put("answercount", answerQueDetailList.stream().filter(answerQueDetail -> Arrays.asList(uids).contains(answerQueDetail.getUid())).count());
                    } else {
                        count = answerQueDetailList.stream().filter(answerQueDetail -> answerQueDetail.getStatus() == 1).count();
                        tmpMap.put("answercount", answerQueDetailList.size());
                    }
                    tmpMap.put("count", count);

                    list.add(tmpMap);
                }
            });
            checkResult.addErrData("correctrat", list);
        }
        checkResult.addErrData("exam", mapper);
        if (exam.getEtype() != ExamType.TSMART) {
            //写入作业试题信息
            ArrayList<Map<String, Object>> questions = questionList.stream().collect(ArrayList<Map<String, Object>>::new, (container, question) -> {
                Map<String, Object> tmpMap = new HashMap<>();
                tmpMap.put("question", questionService.getSimpleInfo(question));
                tmpMap.put("blanks", blankService.getBlankListByQid(question.getQid()));
                container.add(tmpMap);
            }, ArrayList::addAll);
            checkResult.addErrData("questionList", questions);
        }
        return checkResult;
    }


}