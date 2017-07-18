package net.ebh.exam.dao;

import net.ebh.exam.base.ExamType;
import net.ebh.exam.bean.Exam;
import net.ebh.exam.bean.UserAnswer;
import net.ebh.exam.jpa.UserAnswerRepository;
import net.ebh.exam.service.ExamService;
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

import java.util.Arrays;
import java.util.List;

/**
 * Created by xh on 2017/4/19.
 */
@Service
public class UserAnswerDao {
    @Autowired
    UserAnswerRepository userAnswerRepository;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    ExamService examService;

    public UserAnswerRepository getUserAnswerRepository() {
        return userAnswerRepository;
    }

    public UserAnswer getUserAnswerByAid(long aid) {
        return userAnswerRepository.findByAid(aid);
    }

    /**
     * 保存用户答案
     */
    public UserAnswer saveUserAnswer(UserAnswer userAnswer) {
        return userAnswerRepository.save(userAnswer);
    }

    /**
     * 根据关联关系获取试题关系分页
     */
    public Page<UserAnswer> findPage(Exam exam, HMapper params) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        //删除标志判断
        Integer dtag = params.getInteger("dtag");
        if (dtag == null) {
            dtag = new Integer(0);
        }
        criteria.and("dtag").is(dtag);

        Long uid = params.getLong("uid");
        if (uid != null) {
            criteria.and("uid").is(uid);
        }

        Long[] uids = params.getObject("uids", Long[].class);
        if (!ObjectUtils.isEmpty(uids)) {
            criteria.and("uid").in(Arrays.asList(uids));
        }


        if (exam.getEtype() == ExamType.TSMART) {
            criteria.and("fromeid").is(exam.getEid());
        } else {
            criteria.and("eid").is(exam.getEid());
        }

        String aid = params.getString("aid");
        if (aid != null) {
            criteria.and("aid").is(aid);
        }

        Long[] aids = params.getObject("aids", Long[].class);
        if (!ObjectUtils.isEmpty(aids)) {
            criteria.and("aid").in(Arrays.asList(aids));
        }

        Integer status = params.getInteger("status");
        if (status != null) {
            criteria.and("status").is(status);
        }

        //等于某个进度
        Integer correctrat = params.getInteger("correctrat");
        if (correctrat != null) {
            criteria.and("correctrat").is(correctrat);
        }

        //小于某个进度
        Integer correctratlt = params.getInteger("correctratlt");
        if (correctratlt != null) {
            criteria.and("correctrat").lte(correctratlt);
        }

        //大于某个进度
        Integer correctratgt = params.getInteger("correctratgt");
        if (correctratgt != null) {
            criteria.and("correctrat").gte(correctratgt);
        }
        query.addCriteria(criteria);
        long count = mongoTemplate.count(query, UserAnswer.class);
        List<UserAnswer> userAnswers = mongoTemplate.find(query.with(params.parsePage()), UserAnswer.class);
        return new PageImpl<>(userAnswers, params.parsePage(), count);

    }

    public long getExamAnswerCountForTeacher(Exam exam) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("status").is(1);
        if (exam.getEtype() == ExamType.TSMART) {
            criteria.and("eid").is(examService.getsmartExam(exam).getEid());
        } else {
            criteria.and("eid").is(exam.getEid());
        }
        long count = mongoTemplate.count(query.addCriteria(criteria), UserAnswer.class);
        return count;
    }
}
