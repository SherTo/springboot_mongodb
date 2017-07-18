package net.ebh.exam.service;

import com.alibaba.fastjson.JSONObject;
import net.ebh.exam.base.QueType;
import net.ebh.exam.bean.AnswerBlankDetail;
import net.ebh.exam.bean.AnswerQueDetail;
import net.ebh.exam.bean.Blank;
import net.ebh.exam.bean.Question;
import net.ebh.exam.dao.AnswerBlankDetailDao;
import net.ebh.exam.dao.AnswerQueDetailDao;
import net.ebh.exam.dao.BlankDao;
import net.ebh.exam.dao.QuestionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sql.rowset.serial.SerialStruct;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by zkq on 2016/5/19.
 * 单题批改服务
 * 客观题直接给出每空是否正确，填空题，主观题进行全匹配才算正确
 */
@Service
public class ExamCorrectService {
    @Autowired
    AnswerQueDetailDao answerQueDetailDao;
    @Autowired
    AnswerBlankDetailDao answerBlankDetailDao;
    @Autowired
    BlankDao blankDao;

    //批改入口
    public Map<String, Object> correctQues(Question que, List<String> userAnswers) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        AnswerQueDetail answerQueDetail = new AnswerQueDetail();
        answerQueDetail.setData(userAnswers.toString().substring(1, userAnswers.toString().length() - 1));
        answerQueDetail.setQid(que.getQid());
        List<AnswerBlankDetail> correctResult;
        if (que.getQueType() == QueType.A || que.getQueType() == QueType.D) {
            answerQueDetail.setChoicestr(StringUtils.collectionToDelimitedString(userAnswers, ""));
            correctResult = correctAAndDQuestion(que, userAnswers);
        } else if (que.getQueType() == QueType.B) {
            answerQueDetail.setChoicestr(StringUtils.collectionToDelimitedString(userAnswers, ""));
            correctResult = correctBQuestion(que, userAnswers);
        } else if (que.getQueType() == QueType.C) {
            //填空题
            correctResult = correctFillBlank(que, userAnswers);
        } else if (que.getQueType() == QueType.H || que.getQueType() == QueType.G || que.getQueType() == QueType.F) {
            map.put("answerQueDetail", answerQueDetail);
            return map;
        } else if (que.getQueType().toString().startsWith("X")) {
            map.put("answerQueDetail", correctXWX(userAnswers, answerQueDetail));
            return map;
        } else {
            correctResult = correctSubjectiveQue(que, userAnswers);
        }
        answerQueDetailDao.beforeSave(answerQueDetail, correctResult);
        map.put("answerBlankDetailList", correctResult);
        map.put("answerQueDetail", answerQueDetail);
        return map;
    }


    /**
     * 批改完形填空（完型没有blanks只有一条answerquedetails）
     *
     * @param userAnswers
     * @param answerQueDetail
     * @return
     */
    private AnswerQueDetail correctXWX(List<String> userAnswers, AnswerQueDetail answerQueDetail) {
        Map map = JSONObject.parseObject(userAnswers.get(0));
        int score = (int) map.get("uscore");
        int status = (int) map.get("status");
        answerQueDetail.setTotalscore(score);
        answerQueDetail.setStatus(status);
        return answerQueDetail;
    }


    //客观题(单选题(A),判断题(D))机器批改
    private List<AnswerBlankDetail> correctAAndDQuestion(Question que, List<String> userAnswers) throws Exception {

        List<Blank> blanks = blankDao.getBlankListByQid(que.getQid());
        long qid = que.getQid();
        if (blanks.size() != userAnswers.size()) {
            throw new Exception("用户答案和标准答案数量不一致");
        }
        //获取正确答案数字序列
        List<String> okAnswers = new ArrayList<>(blanks.size());
        for (Blank blank : blanks) {
            if (blank.isanswer() == 1) {
                okAnswers.add("1");
            } else {
                okAnswers.add("0");
            }
        }
        List<AnswerBlankDetail> answerBlankDetails = new ArrayList<>();
        for (int i = 0, size = blanks.size(); i < size; i++) {
            long bid = blanks.get(i).getBid();
            double score = blanks.get(i).getScore();
            AnswerBlankDetail answerBlankDetail = new AnswerBlankDetail();
            answerBlankDetail.setBid(bid);
            answerBlankDetail.setContent(userAnswers.get(i));
            answerBlankDetail.setQid(qid);
            if ((blanks.get(i).isanswer() == 1) && okAnswers.get(i).equals(userAnswers.get(i))) {
                answerBlankDetail.setScore(score);
            } else {
                answerBlankDetail.setScore(0);
            }
            answerBlankDetail.setStatus(1);
            answerBlankDetails.add(answerBlankDetail);
        }
        return answerBlankDetails;
    }

    //客观题(多选题(B))机器批改
    private List<AnswerBlankDetail> correctBQuestion(Question que, List<String> userAnswers) throws Exception {
        List<Blank> blanks = blankDao.getBlankListByQid(que.getQid());
        long qid = que.getQid();
        if (blanks.size() != userAnswers.size()) {
            throw new Exception("用户答案和标准答案数量不一致 qid:" + que.getQid());
        }
        List<String> okAnswers = new ArrayList<>(blanks.size());
        for (Blank blank : blanks) {
            if (blank.isanswer() == 1) {
                okAnswers.add("1");
            } else {
                okAnswers.add("0");
            }
        }
        Boolean hasWrong = false;
        StringBuffer stuanswer = new StringBuffer();
        List<AnswerBlankDetail> answerBlankDetails = new ArrayList<>();
        for (int i = 0, size = blanks.size(); i < size; i++) {

            double score = blanks.get(i).getScore();
            long bid = blanks.get(i).getBid();
            AnswerBlankDetail answerBlankDetail = new AnswerBlankDetail();
            answerBlankDetail.setBid(bid);
            answerBlankDetail.setContent(userAnswers.get(i));
            answerBlankDetail.setQid(qid);
            if (blanks.get(i).isanswer() == 1) {
                if (okAnswers.get(i).equals(userAnswers.get(i))) {
                    stuanswer.append(userAnswers.get(i));
                    answerBlankDetail.setScore(score);
                } else {
                    answerBlankDetail.setScore(0);
                }
            } else {
                if ("1".equals(userAnswers.get(i))) {
                    hasWrong = true;
                }
                answerBlankDetail.setScore(0);
            }
            answerBlankDetail.setStatus(1);
            answerBlankDetails.add(answerBlankDetail);
        }
        if (hasWrong == true) {
            answerBlankDetails.forEach(answerDetail ->
                    answerDetail.setScore(0)
            );
        }
        return answerBlankDetails;
    }

    //填空题批改全部匹配则正确
    private List<AnswerBlankDetail> correctFillBlank(Question que, List<String> userAnswers) throws Exception {
        List<Blank> blanks = blankDao.getBlankListByQid(que.getQid());
        long qid = que.getQid();
        if (blanks.size() != userAnswers.size()) {
            throw new Exception("用户答案和标准答案数量不一致");
        }
        List<AnswerBlankDetail> answerBlankDetails = new ArrayList<>();
        for (int i = 0, size = blanks.size(); i < size; i++) {
            long bid = blanks.get(i).getBid();
            double score = blanks.get(i).getScore();
            AnswerBlankDetail answerBlankDetail = new AnswerBlankDetail();
            answerBlankDetail.setBid(bid);
            answerBlankDetail.setContent(userAnswers.get(i));
            answerBlankDetail.setQid(qid);
            //多个答案用#隔开
            if (blanks.get(i).getBsubject().contains("#")) {
                List<String> strings = Arrays.asList(blanks.get(i).getBsubject().split("#"));
                if (strings.contains(userAnswers.get(i))) {
                    answerBlankDetail.setScore(score);
                }
            } else if (blanks.get(i).getBsubject().equals(userAnswers.get(i))) {
                answerBlankDetail.setScore(score);
            } else {
                answerBlankDetail.setScore(0);
            }
            answerBlankDetail.setStatus(1);
            answerBlankDetails.add(answerBlankDetail);
        }

        return answerBlankDetails;
    }

    //主观题批改(全部匹配则正确,暂时和填空题一样
    private List<AnswerBlankDetail> correctSubjectiveQue(Question que, List<String> userAnswers) throws Exception {
        List<Blank> blanks = blankDao.getBlankListByQid(que.getQid());
        long qid = que.getQid();
        if (blanks.size() != userAnswers.size()) {
            throw new Exception("用户答案和标准答案数量不一致");
        }
        int totalScore = 0;
        List<AnswerBlankDetail> answerBlankDetails = new ArrayList<>();
        for (int i = 0, size = blanks.size(); i < size; i++) {
            long bid = blanks.get(i).getBid();
            double score = blanks.get(i).getScore();
            AnswerBlankDetail answerBlankDetail = new AnswerBlankDetail();
            answerBlankDetail.setBid(bid);
            answerBlankDetail.setContent(userAnswers.get(i));
            answerBlankDetail.setQid(qid);
            if (blanks.get(i).getBsubject().equals(userAnswers.get(i))) {
                answerBlankDetail.setScore(score);
                totalScore += score;
            } else {
                answerBlankDetail.setScore(0);
            }
            answerBlankDetail.setStatus(0);
            answerBlankDetails.add(answerBlankDetail);
        }
        return answerBlankDetails;
    }

}
