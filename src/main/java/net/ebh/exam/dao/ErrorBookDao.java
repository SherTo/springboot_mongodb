package net.ebh.exam.dao;

import com.mongodb.DBObject;
import lombok.Getter;
import net.ebh.exam.base.QueType;
import net.ebh.exam.base.RelationType;
import net.ebh.exam.bean.ErrorBook;
import net.ebh.exam.bean.Question;
import net.ebh.exam.bean.QuestionRelation;
import net.ebh.exam.jpa.ErrorBookRepository;
import net.ebh.exam.util.HMapper;
import org.codehaus.groovy.runtime.powerassert.SourceText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zkq on 2016/6/16.
 * 错题集dao
 */
@Service
public class ErrorBookDao {
    @Autowired
    MongoTemplate mongoTemplate;
    @Getter
    @Autowired
    ErrorBookRepository errorBookRepository;

    /**
     * 分页获取错题
     *
     * @param params
     * @return
     */
    public Page<DBObject> getErrorPage(HMapper params) {
        long page = params.getLong("page");
        if (page == 0) {
            page = 1;
        }
        long pagesize = params.getLong("pagesize");
        if (pagesize == 0) {
            pagesize = 20;
        }
        Query query = new Query();
        Criteria criteria = new Criteria();
        Long uid = params.getLong("uid");
        String forwho = params.getString("forwho", true);
        if (StringUtils.isEmpty(forwho)) {
            forwho = "student";
        }
        //错题所属的学生
        if (uid != null && forwho.equals("student")) {
            criteria.and("uid").is(uid);
        }
        //错题所属的学生们
        Long[] uids = params.getObject("uids", Long[].class);
        if (!ObjectUtils.isEmpty(uids)) {
            criteria.and("uid").in(Arrays.asList(uids));
        }
        //手动添加1&&自动添加0
        Integer style = params.getInteger("style");
        if (style != null) {
            criteria.and("style").is(style);
        }
        query.addCriteria(criteria);
        query.fields().include("qid");
        List<ErrorBook> errorBookList = mongoTemplate.find(query, ErrorBook.class);
        List<Long> qids = errorBookList.stream().map(ErrorBook::getQid).distinct().collect(Collectors.toList());
        List<Long> qidList = new ArrayList<>();
        query = new Query();
        criteria = new Criteria();
        criteria.and("qid").in(qids);
        Long tid = params.getLong("tid");
        String path = params.getString("path");
        List<QuestionRelation> relationList = null;
        if (tid != null && !StringUtils.isEmpty(path)) {
            criteria.and("tid").is(tid).and("ttype").is(RelationType.FOLDER);
            query.fields().include("qid");
            query.addCriteria(criteria);
            List<QuestionRelation> questionRelations = mongoTemplate.find(query, QuestionRelation.class);
            List<Long> qidlist = questionRelations.stream().map(QuestionRelation::getQid).collect(Collectors.toList());
            Query q = new Query();
            Criteria c = new Criteria();
            c.and("path").regex(path).and("ttype").is(RelationType.CHAPTER).and("qid").in(qidlist);
            q.addCriteria(c);
            q.fields().include("qid");
            relationList = mongoTemplate.find(q, QuestionRelation.class);
        } else if (tid != null) {
            criteria.and("tid").is(tid);
            query.addCriteria(criteria);
            query.fields().include("qid");
            relationList = mongoTemplate.find(query, QuestionRelation.class);
        } else if (!StringUtils.isEmpty(path)) {
            criteria.and("path").regex(path);
            query.addCriteria(criteria);
            query.fields().include("qid");
            relationList = mongoTemplate.find(query, QuestionRelation.class);
        } else {
            qidList = qids;
        }
        if (!ObjectUtils.isEmpty(relationList)) {
            qidList = relationList.stream().map(QuestionRelation::getQid).distinct().collect(Collectors.toList());
        }
        query = new Query();
        criteria = new Criteria();
        criteria.and("qid").in(qidList);
        //错题来源试题类型
        String quetype = params.getString("quetype");
        //错题来源试题关键字
        String q = params.getString("q", true);
        //错题来源试题教室
        Long crid = params.getLong("crid");
        //错题来源试题作业
        Long eid = params.getLong("eid");
        //错题来源试题所属教师
        Long teacherid = params.getLong("teacherid");
        //排除掉需要过滤的题型
        List<QueType> xlist = QueType.Xlist();
        List<QueType> hgz = QueType.HGZ();
        List<String> list = QueType.addList(xlist, hgz);
        if (!ObjectUtils.isEmpty(quetype)) {
            criteria.andOperator(Criteria.where("queType").is(quetype), Criteria.where("queType").nin(list));
        } else {
            criteria.andOperator(Criteria.where("queType").nin(list));
        }
        if (!StringUtils.isEmpty(q)) {
            criteria.and("qsubject").regex(".*?" + q + ".*");
        }
        if (crid != null) {
            criteria.and("crid").is(crid);
        }
        if (eid != null) {
            criteria.and("eid").is(eid);
        }
        if (teacherid != null) {
            criteria.and("uid").is(teacherid);
        }
        query.addCriteria(criteria);
        query.fields().include("qid");
        List<Question> questions = mongoTemplate.find(query, Question.class);
        qids = questions.stream().map(Question::getQid).collect(Collectors.toList());
        Criteria criteria1 = new Criteria().and("qid").in(qids);
        ProjectionOperation project = Aggregation.project("qid", "errorid");
        MatchOperation match = Aggregation.match(criteria1);
        GroupOperation group = Aggregation.group("qid").count().as("count").last("errorid").as("errorid");
        ProjectionOperation projectionOperation = Aggregation.project("errorid", "qid", "count").and("qid").previousOperation();
        SkipOperation skip = Aggregation.skip((page - 1) * pagesize);
        LimitOperation limit = Aggregation.limit(pagesize);
        Aggregation aggregation = Aggregation.newAggregation(project, match, group, Aggregation.sort(Sort.Direction.DESC, "errorid"), skip, limit, projectionOperation);
        AggregationResults<DBObject> aggregate = mongoTemplate.aggregate(aggregation, ErrorBook.class, DBObject.class);
        List<DBObject> results = aggregate.getMappedResults();
        return new PageImpl<>(results, params.parsePage(), qids.size());
    }

    public List<ErrorBook> getErrorListByQid(Long qid) {
        return errorBookRepository.getListByQid(qid);
    }
}
