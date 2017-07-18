package net.ebh.exam.dao;

import lombok.Getter;
import net.ebh.exam.TempVo.UpdateUtil;
import net.ebh.exam.base.QueType;
import net.ebh.exam.bean.AnswerBlankDetail;
import net.ebh.exam.bean.AnswerQueDetail;
import net.ebh.exam.bean.Question;
import net.ebh.exam.jpa.AnswerBlankDetailRepository;
import net.ebh.exam.jpa.AnswerQueDetailRepository;
import net.ebh.exam.util.HMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xh on 2017/4/19.
 */
@Service
public class AnswerQueDetailDao {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    QuestionDao questionDao;
    @Autowired
    BlankDao blankDao;
    @Autowired
    @Getter
    AnswerQueDetailRepository answerQueDetailRepository;

    public List<AnswerQueDetail> getAnswerQueDetailListByAid(long aid) {
        return mongoTemplate.find(new Query(new Criteria().and("aid").is(aid)), AnswerQueDetail.class);
    }

    public AnswerQueDetail getAnswerQueDetailByDqid(long dqid) {
        return answerQueDetailRepository.findByDqid(dqid);
    }

    public List<AnswerQueDetail> getAnswerQueDetailListByQid(long qid) {
        return mongoTemplate.find(new Query(new Criteria().and("qid").is(qid)), AnswerQueDetail.class);
    }

    public AnswerQueDetail beforeSave(AnswerQueDetail answerQueDetail, List<AnswerBlankDetail> answerBlankDetailList) {
        double totalscore = 0;
        int allok = 1;
        Question question = questionDao.getQuestionByQid(answerQueDetail.getQid());
        int qscore = question.getQuescore();
        StringBuffer buffer = new StringBuffer();
        blankDao.getBlankListByQid(answerQueDetail.getQid()).forEach(blank -> buffer.append(blank.getBsubject() + ","));
        String choicestring = buffer.substring(0, buffer.length() - 1);
        //判断多选题和填空在全队的情况下可能造成的精度问题（如果全队得分为题目总分）
        if (question.getQueType() == QueType.B && !StringUtils.isEmpty(question.getChoicestr()) && question.getChoicestr().equals(answerQueDetail.getChoicestr())) {
            answerQueDetail.setTotalscore(qscore);
            answerBlankDetailList.forEach(answerBlankDetail -> answerBlankDetail.setDqid(answerQueDetail.getDqid()));
        } else if (question.getQueType() == QueType.C && choicestring.equals(answerQueDetail.getData().replaceAll(" ", ""))) {
            answerQueDetail.setTotalscore(qscore);
            answerBlankDetailList.forEach(answerBlankDetail -> answerBlankDetail.setDqid(answerQueDetail.getDqid()));
        } else {
            for (AnswerBlankDetail blankDetail : answerBlankDetailList) {
                blankDetail.setDqid(answerQueDetail.getDqid());
                BigDecimal bigDecimal = new BigDecimal(String.valueOf(blankDetail.getScore())).add(new BigDecimal(totalscore));
                totalscore = bigDecimal.doubleValue();
                if (blankDetail.getStatus() == 0) {
                    allok = 0;
                }
            }
            answerQueDetail.setTotalscore(totalscore);
        }
        answerQueDetail.setStatus(allok);
        if (answerQueDetail.getTotalscore() == question.getQuescore()) {
            answerQueDetail.setAllright(1);

        } else {
            answerQueDetail.setAllright(0);
        }
        for (AnswerBlankDetail blankDetail : answerBlankDetailList) {
            mongoTemplate.updateFirst(new Query(new Criteria().and("dbid").is(blankDetail.getDbid())), UpdateUtil.buildBaseUpdate(blankDetail), AnswerBlankDetail.class);
        }
        return answerQueDetail;
    }

    public Page<AnswerQueDetail> getAQDList(Question question, HMapper params) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        //删除标志判断
        Integer dtag = params.getInteger("dtag");
        if (dtag == null) {
            dtag = new Integer(0);
        }
        criteria.and("dtag").is(dtag);

        criteria.and("qid").is(question.getQid());

        Long[] uids = params.getObject("uids", Long[].class);
        if (!ObjectUtils.isEmpty(uids)) {
            criteria.and("uid").in(Arrays.asList(uids));
        }

        Long uid = params.getLong("uid");
        if (uid != null) {
            criteria.and("uid").is(uid);
        }

        Long markuid = params.getLong("markuid");
        if (markuid != null) {
            criteria.and("markuid").is(markuid);
        }

        Integer allright = params.getInteger("allright");
        if (allright != null) {
            criteria.and("allright").is(allright);
        }
        String choicestr = params.getString("choicestr");
        if (!StringUtils.isEmpty(choicestr)) {
            criteria.and("choicestr").is(choicestr);
        }
        Long aid = params.getLong("aid");
        if (aid != null) {
            criteria.and("aid").is(aid);
        }

        Long dqid = params.getLong("dqid");
        if (dqid != null) {
            criteria.and("dqid").is(dqid);
        }
        Integer status = params.getInteger("status");
        if (status != null) {
            criteria.and("status").is(status);
        }
        query.addCriteria(criteria);
        long count = mongoTemplate.count(query, AnswerQueDetail.class);
        List<AnswerQueDetail> list = mongoTemplate.find(query.with(params.parsePage()), AnswerQueDetail.class);
        return new PageImpl<>(list, params.parsePage(), count);
    }

    public List<AnswerQueDetail> getAnswerQueDetailList(long aid) {
        return answerQueDetailRepository.findAnswerQueDetailListByAid(aid);
    }

    public List<AnswerQueDetail> updateAnswerQueDetailList(List<AnswerQueDetail> answerQueDetails) {
        return answerQueDetailRepository.save(answerQueDetails);
    }
}
