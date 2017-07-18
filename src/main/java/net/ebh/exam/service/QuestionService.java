package net.ebh.exam.service;

import com.alibaba.fastjson.JSON;
import net.ebh.exam.TempVo.Analysis;
import net.ebh.exam.TempVo.User;
import net.ebh.exam.base.BaseRelationForResp;
import net.ebh.exam.base.QueType;
import net.ebh.exam.bean.*;
import net.ebh.exam.dao.AnswerQueDetailDao;
import net.ebh.exam.dao.BlankDao;
import net.ebh.exam.dao.QuestionDao;
import net.ebh.exam.dao.QuestionRelationDao;
import net.ebh.exam.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static net.ebh.exam.util.ChangeChoice.choiceToString;

/**
 * Created by admin on 2017/2/9.
 */
@Service
public class QuestionService {
    @Autowired
    QuestionDao questionDao;
    @Autowired
    AnswerQueDetailDao answerQueDetailDao;
    @Autowired
    BlankDao blankDao;
    @Autowired
    QuestionRelationService questionRelationService;
    @Autowired
    BlankService blankService;

    public QuestionDao getQuestionDao() {
        return questionDao;
    }

    /**
     * 验证question
     *
     * @param question
     * @throws Exception
     */
    public void canSave(Question question) throws Exception {
        if (question == null) {
            throw new CException(ErrorCode.QUESTION_ISNOT_EXIST);
        }
        if (StringUtils.isEmpty(question.getQsubject())) {
            throw new CException(ErrorCode.QUESTION_QSUBJECT_ISNULL);
        }
        //标题Z和音频G要过滤
        if (question.getQueType() == QueType.Z || question.getQueType() == QueType.G) {
            return;
        }
        if (question.getQuescore() == 0) {
            throw new CException(ErrorCode.QUESTION_SCORE_ISNULL);
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
     * 生成MD5code（data,qsubject,quetype,extdata）
     *
     * @param question
     * @return
     */
    public String calcMd5Code(Question question) {
        StringBuffer sb = new StringBuffer();
        sb.append(question.getData()).append(question.getQsubject()).append(question.getQueType()).append(question.getExtdata());
        return CUtil.MD5(sb.toString());
    }

    /**
     * 提取question简单信息
     *
     * @param question
     * @return
     */
    public HMapper getSimpleInfo(Question question) {
        HMapper hMapper = new HMapper();
        if (question == null) {
            return hMapper;
        }
        hMapper.put("qid", question.getQid());
        hMapper.put("dateline", question.getDateline());
        hMapper.put("uid", question.getUid());
        hMapper.put("quetype", question.getQueType());
        hMapper.put("quetypename", question.getQueType().getName());
        hMapper.put("quescore", question.getQuescore());
        hMapper.put("choicestr", question.getChoicestr());
        hMapper.put("crid", question.getCrid());
        hMapper.put("level", question.getLevel());
        hMapper.put("md5code", question.getMd5code());
        hMapper.put("qsubject", question.getQsubject());
        hMapper.put("status", question.getStatus());
        List<QuestionRelation> relationset = HMapper.parseData2List(question.getData(), "relationSet", QuestionRelation[].class);
        hMapper.put("relationSet", relationset.stream().filter(questionRelation -> !StringUtils.isEmpty(questionRelation.getPath())).collect(Collectors.toSet()));
        hMapper.put("extdata", question.getExtdata());
        hMapper.put("data", question.getData());
        return hMapper;
    }

    /**
     * 提取question简单信息
     *
     * @param question
     * @return
     */
    public HMapper getQuestionInfo(Question question) {
        HMapper hMapper = new HMapper();
        hMapper.put("qid", question.getQid());
        hMapper.put("dateline", question.getDateline());
        hMapper.put("uid", question.getUid());
        hMapper.put("queType", question.getQueType());
        hMapper.put("quetypename", question.getQueType().getName());
        hMapper.put("quescore", question.getQuescore());
        hMapper.put("choicestr", question.getChoicestr());
        hMapper.put("crid", question.getCrid());
        hMapper.put("level", question.getLevel());
        hMapper.put("md5code", question.getMd5code());
        hMapper.put("qsubject", question.getQsubject());
        hMapper.put("status", question.getStatus());
        hMapper.put("extdata", question.getExtdata());
        hMapper.put("data", question.getData());
        hMapper.put("blanks", blankDao.getBlankListByQid(question.getQid()));
        return hMapper;
    }

    /**
     * 单题分析
     *
     * @param question
     * @param hMapper
     * @return
     */
    public Map<String, Object> qfenxi(Question question, HMapper hMapper) {
        Map<String, Object> ret = new HashMap<>();
        String action = hMapper.getString("action");
        User user = hMapper.getObject("user", User.class);
        List<Long> uids = hMapper.arr2List("uids", Long[].class);
        List<AnswerQueDetail> aqdList;
        double sumscore;
        long rightcount;
        if (!ObjectUtils.isEmpty(uids)) {
            aqdList = answerQueDetailDao.getAnswerQueDetailListByQid(question.getQid()).stream().filter(answerQueDetail -> uids.contains(answerQueDetail.getUid())).collect(Collectors.toList());
            sumscore = aqdList.stream().filter(answerQueDetail -> uids.contains(answerQueDetail.getUid())).mapToDouble(AnswerQueDetail::getTotalscore).sum();
            rightcount = aqdList.stream().filter(answerQueDetail -> answerQueDetail.getAllright() == 1 && uids.contains(answerQueDetail.getUid())).count();

        } else {
            aqdList = answerQueDetailDao.getAnswerQueDetailListByQid(question.getQid()).stream().collect(Collectors.toList());
            sumscore = aqdList.stream().mapToDouble(AnswerQueDetail::getTotalscore).sum();
            rightcount = aqdList.stream().filter(answerQueDetail -> answerQueDetail.getAllright() == 1).count();
        }
        //总人数
        long usum = aqdList.size();
        //平均分
        double avgscore = 0;
        if (usum > 0) {
            avgscore = sumscore / (double) usum;
            BigDecimal bd = new BigDecimal(String.valueOf(avgscore));
            avgscore = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        Map<String, Long> choiceMap = new HashMap<>();

        StringBuilder sb = new StringBuilder();

        if (Arrays.asList(QueType.A, QueType.B, QueType.D).contains(question.getQueType())) {
            int choicelength = question.getChoicestr().length();
            for (int i = 0; i < choicelength; i++) {
                String key = ChoiceUtil.getABCByIdx(i);
                if ('1' == question.getChoicestr().charAt(i)) {
                    sb.append(key);
                }
                choiceMap.put(key, 0L);
            }
            for (AnswerQueDetail answerQueDetail : aqdList) {
                for (int i = 0, size = answerQueDetail.getChoicestr().length(); i < size; i++) {
                    String key = ChoiceUtil.getABCByIdx(i);
                    String charAt = String.valueOf(answerQueDetail.getChoicestr().charAt(i));
                    if (charAt.equals("1")) {
                        choiceMap.put(key, choiceMap.get(key) + 1);
                    }
                }
            }
        }
        //如果是学生需要统计"我的分数"
        if (action.equals("student")) {
            List<AnswerQueDetail> answerQueDetailList = answerQueDetailDao.getAnswerQueDetailListByQid(question.getQid());
            double myscore = answerQueDetailList.stream().filter(answerQueDetail -> answerQueDetail.getUid() == user.getUid()).mapToDouble(AnswerQueDetail::getTotalscore).sum();
            ret.put("myscore", myscore);
            ret.put("scorerat", myscore / question.getQuescore());
        }
        ret.put("qid", question.getQid());
        ret.put("sumsorce", sumscore);
        ret.put("usercount", usum);
        ret.put("avgscore", avgscore);
        ret.put("rightcount", rightcount);
        ret.put("rightchoice", sb.toString());
        ret.put("choicemap", choiceMap);
        System.out.println(choiceMap);
        float rightrat = rightcount / (float) usum;
        ret.put("rightrat", rightrat);
        ret.put("quescore", question.getQuescore());
        ret.put("qtype", question.getQueType());
        ret.put("relationSet", HMapper.parseData(question.getData(), "relationSet", BaseRelationForResp[].class));
        return ret;
    }

    /**
     * 根据试题编号获取试题
     *
     * @param qid
     * @return
     */
    public Question getQuestionByQid(long qid) {
        return questionDao.getQuestionByQid(qid);
    }

    /**
     * 获取试题的完全信息
     *
     * @param question
     * @return
     */
    public HMapper getInfoForEdit(Question question) {
        HMapper simpleInfo = getSimpleInfo(question);
        if (question != null) {
            simpleInfo.put("blanks", blankDao.getBlankListByQid(question.getQid()));
        }
        return simpleInfo;
    }

    /**
     * 获取知识点
     *
     * @param hMapper
     * @return
     */
    public CheckResult getChapters(HMapper hMapper) {
        String ttype = hMapper.getString("ttype");
        if (!ttype.equals("CHAPTER")) {
            try {
                throw new CException(ErrorCode.QUESTION_CHAPTER_ISNULL);
            } catch (CException e) {
                e.printStackTrace();
            }
        }
        StringBuffer buff = new StringBuffer();
        questionRelationService.findAllQueRelation(hMapper).forEach(questionRelation -> {
            buff.append(questionRelation.getTid() + ",");
        });
        return CheckResult.newInstance().addErrData("tid", buff.substring(0, buff.length() - 1));
    }

    /**
     * 试题统计
     */
    public CheckResult statusticsExam(Question question) {
        HashMap<String, Integer> map = new HashMap<>();
        CheckResult checkResult = CheckResult.newInstance();
        if (question.getStatus() != 1) {
            try {
                throw new CException(ErrorCode.QUESTION_NOT_CORRECTED);
            } catch (CException e) {
                e.printStackTrace();
            }
        }
        List<AnswerQueDetail> answerQueDetailList = answerQueDetailDao.getAnswerQueDetailListByQid(question.getQid());
        //如果是选择题
        if (question.getQueType().equals(QueType.A) || question.getQueType().equals(QueType.B)) {
            answerQueDetailList.forEach(answerQueDetail -> {
                String choiceStr = choiceToString(answerQueDetail.getChoicestr());
                if (map.get(choiceStr) != null) {
                    map.put(choiceStr, map.get(choiceStr) + 1);
                } else {
                    map.put(choiceStr, 1);
                }
            });
        }
        return checkResult.addErrData("choice", JSON.toJSON(map).toString());
    }

    public List getQuestionInfo(List<Question> questions) {
        return questions.stream().collect(ArrayList<Map<String, Object>>::new, (container, question) -> {
            Map<String, Object> tmpMap = new HashMap<>();
            tmpMap.put("question", getSimpleInfo(question));
            tmpMap.put("blanks", blankService.getBlankListByQid(question.getQid()));
            container.add(tmpMap);
        }, ArrayList::addAll);
    }
}
