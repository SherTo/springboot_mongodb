package net.ebh.exam.dao;

import net.ebh.exam.base.RelationType;
import net.ebh.exam.bean.Question;
import net.ebh.exam.bean.QuestionRelation;
import net.ebh.exam.jpa.QuestionRelationRepository;
import net.ebh.exam.util.HMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Root;

/**
 * Created by admin on 2017/2/20.
 */
@Service
public class QuestionRelationDao {
    @Autowired
    QuestionRelationRepository questionRelationRepository;
    @Autowired
    MongoTemplate mongoTemplate;

    public QuestionRelationRepository getQuestionRelationRepository() {
        return questionRelationRepository;
    }

    public List<QuestionRelation> getQuestionRelationListByQid(long qid) {
        return questionRelationRepository.findQuestionRealtionByQid(qid);
    }

    public List<QuestionRelation> getAll(HMapper hMapper) {

        Query query = new Query();
        Criteria criteria = new Criteria();
        String qid = hMapper.getString("qid");
        if (!StringUtils.isEmpty(qid)) {
            criteria.and("qid").is(qid);
        }
        RelationType ttype = hMapper.getObject("ttype", RelationType.class);
        if (!ObjectUtils.isEmpty(ttype)) {
            criteria.and("ttype").is(ttype);
        }
        query.addCriteria(criteria);
        return mongoTemplate.find(query, QuestionRelation.class);
    }
}
