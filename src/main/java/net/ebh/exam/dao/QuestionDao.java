package net.ebh.exam.dao;

import lombok.Getter;
import net.ebh.exam.TempVo.Analysis;
import net.ebh.exam.bean.AnswerQueDetail;
import net.ebh.exam.bean.Question;
import net.ebh.exam.jpa.QuestionRepository;
import net.ebh.exam.util.HMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * Created by admin on 2017/2/20.
 */
@Service
public class QuestionDao {
    @Autowired
    @Getter
    QuestionRepository questionRepository;
    @Autowired
    MongoTemplate mongoTemplate;


    /**
     * 获取指定题目分析
     *
     * @param question
     */
    public List<Analysis> doAnalysis(Question question, HMapper params) {
        Criteria criteria = new Criteria();
        Integer status = params.getInteger("status");
        if (!ObjectUtils.isEmpty(status)) {
            criteria.and("status").is(status);
        }
        List<Long> uids = params.arr2List("uids", Long[].class);
        if (!ObjectUtils.isEmpty(uids)) {
            criteria.and("uid").in(uids);
        }
        criteria.and("qid").is(question.getQid());
        Aggregation aggregation = newAggregation(
                match(criteria),
                group("choicestr", "allright").sum("totalscore").as("totalscore")
        );
        AggregationResults<Analysis> results = mongoTemplate.aggregate(aggregation, AnswerQueDetail.class, Analysis.class);
        return results.getMappedResults();
    }

    /**
     * 根据试题编号获取试题
     */
    public Question getQuestionByQid(long qid) {
        return questionRepository.findByQid(qid);
    }

}
