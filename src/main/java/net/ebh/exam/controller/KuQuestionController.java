package net.ebh.exam.controller;

import net.ebh.exam.base.QueType;
import net.ebh.exam.bean.Exam;
import net.ebh.exam.bean.KuQuestion;
import net.ebh.exam.TempVo.User;
import net.ebh.exam.service.KuQuestionService;
import net.ebh.exam.util.CException;
import net.ebh.exam.util.CheckResult;
import net.ebh.exam.util.ErrorCode;
import net.ebh.exam.util.HMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by xh on 2017/3/31.
 */
@RestController
@RequestMapping(value = "/ku")
public class KuQuestionController {
    @Autowired
    KuQuestionService kuQuestionService;

    /**
     * 保存作业试题到题库
     */
    @RequestMapping(value = "/addfromque/{qid}", method = {RequestMethod.POST})
    public CheckResult addFromQuestion(@PathVariable("qid") long qid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        KuQuestion kuQuestion = kuQuestionService.addKuFromQuestion(qid, user.getUid());
        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("kuqid", kuQuestion.getKuqid());
        return checkResult;
    }

    /**
     * 从题库删除试题
     */
    @RequestMapping(value = "/delete/{kuqid}", method = {RequestMethod.POST})
    public CheckResult deleteKuQuestion(@PathVariable("kuqid") long kuqid, @RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        kuQuestionService.doDelete(kuqid);
        return CheckResult.newInstance().addErrData("status", "ok");
    }

    /**
     * 获取题库某个作业简单信息
     *
     * @param kuqid
     * @param hMapper
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/kudetail/{kuqid}", method = RequestMethod.POST)
    public CheckResult editKuQuestion(@PathVariable long kuqid, @RequestBody HMapper hMapper) throws Exception {
        User user = User.getUser(hMapper);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        KuQuestion kuQuestion = kuQuestionService.findById(kuqid);
        if (ObjectUtils.isEmpty(kuQuestion)) {
            throw new CException(ErrorCode.KUQUESTION_ISNOT_EXIST);
        }
        return CheckResult.newInstance().addErrData("kuquestion", kuQuestion);
    }

    /**
     * 修改试题到题库/
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.POST})
    public CheckResult save(@RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        KuQuestion kuQuestion = params.getObject("kuQuestion", KuQuestion.class);
        if (ObjectUtils.isEmpty(kuQuestion)) {
            throw new CException(ErrorCode.NOT_FIND_KUQUESTION);
        }

        if (user.getUid() != kuQuestion.getUid()) {
            throw new CException(ErrorCode.KUQUESTION_ISNOT_BELONG);
        }
        kuQuestionService.doSave(kuQuestion);
        return CheckResult.newInstance().addErrData("status", "ok");
    }

    /**
     * 教师布置作业的时候获取题库题库试题列表
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/kulist", method = {RequestMethod.POST})
    public CheckResult getList(@RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        Integer frommy = params.getInteger("frommy");
        if (!ObjectUtils.isEmpty(frommy)) {
            params.put("uid", user.getUid());
        }
        Page<KuQuestion> pageForKu = kuQuestionService.getPageForKu(params);
        List<KuQuestion> kuQuestionList = pageForKu.getContent();
        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("kuqlist", kuQuestionList);
        checkResult.addErrData("pageInfo", HMapper.pageRet(pageForKu));
        return checkResult;
    }

    /**
     * 判断你指定md5code的试题是否已经加入到教师的试题库啦
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/ifinku", method = {RequestMethod.POST})
    public CheckResult ifInKu(@RequestBody HMapper params) throws Exception {
        User user = User.getUser(params);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        String md5code = params.getString("md5code", true);
        if (StringUtils.isEmpty(md5code)) {
            throw new CException(ErrorCode.NOT_FIND_MD5CODE);
        }
        params.put("uid", user.getUid());
        CheckResult checkResult = CheckResult.newInstance();
        KuQuestion kuQuestion = kuQuestionService.ifinku(params);
        String ifinku = "1";
        if (ObjectUtils.isEmpty(kuQuestion) || kuQuestion.getDtag() == 1) {
            ifinku = "0";
        }
        long kuqid = "0".equals(ifinku) ? 0 : kuQuestion.getKuqid();
        checkResult.addErrData("ifinku", ifinku);
        checkResult.addErrData("kuqid", kuqid);
        return checkResult;
    }

    /**
     * 手动添加到题库（判断是否存在）
     *
     * @param hMapper
     * @return
     * @throws Exception
     */

    @RequestMapping(value = "/addtoku", method = RequestMethod.POST)
    public CheckResult addQueToKu(@RequestBody HMapper hMapper) throws Exception {
        User user = User.getUser(hMapper);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        CheckResult checkResult = CheckResult.newInstance();
        Exam exam = hMapper.getObject("exam", Exam.class);
        List<KuQuestion> kuQuestionList = hMapper.parseData2List(exam.getData(), "kuQuestionList", KuQuestion[].class);
        List<KuQuestion> kuqueList = new ArrayList<>();
        for (KuQuestion kuQuestion : kuQuestionList) {
            kuQuestion.setUid(user.getUid());
            kuQuestion.setCrid(user.getCrid());
            kuQuestion.setStatus(1);
            kuQuestion.beforeSave();
            KuQuestion kuque = kuQuestionService.doSave(kuQuestion);
            kuqueList.add(kuque);
        }
        return checkResult.addErrData("kuQuestionList", kuqueList);
    }

    /**
     * 题库分析统计(统计老师题型和题库题型)
     *
     * @param hMapper
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/fenxi", method = RequestMethod.POST)
    public CheckResult Kufenxi(@RequestBody HMapper hMapper) throws Exception {
        User user = User.getUser(hMapper);
        if (ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        hMapper.put("crid", user.getCrid());
        List<QueType> typeList = kuQuestionService.getKuquestionTypeList();
        List<Map<String, Object>> mylist = new ArrayList<>();
        List<Map<String, Object>> kulist = new ArrayList<>();
        CheckResult checkResult = CheckResult.newInstance();
        typeList.stream().filter(queType -> !Arrays.asList(QueType.Z, QueType.G).contains(queType)).forEach(queType -> {
            hMapper.put("quetype", queType);
            Map<String, Object> mymap = new HashMap();
            Long mycount = kuQuestionService.kufenxi(hMapper);
            mymap.put("quetype", queType);
            mymap.put("count", mycount);
            mylist.add(mymap);
            Map<String, Object> kumap = new HashMap<>();
            hMapper.put("uid", user.getUid());
            Long kucount = kuQuestionService.kufenxi(hMapper);
            kumap.put("quetype", queType);
            kumap.put("count", kucount);
            kulist.add(kumap);
        });

        checkResult.addErrData("myfenxi", mylist);
        checkResult.addErrData("kufenxi", kulist);
        return checkResult;
    }
}
