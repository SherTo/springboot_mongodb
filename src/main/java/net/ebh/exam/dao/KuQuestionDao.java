package net.ebh.exam.dao;

import lombok.Getter;
import net.ebh.exam.TempVo.Condition;
import net.ebh.exam.TempVo.User;
import net.ebh.exam.base.QueType;
import net.ebh.exam.base.RelationType;
import net.ebh.exam.bean.*;
import net.ebh.exam.jpa.KuQuestionRelationRepository;
import net.ebh.exam.jpa.KuQuestionRepository;
import net.ebh.exam.util.CException;
import net.ebh.exam.util.CUtil;
import net.ebh.exam.util.ErrorCode;
import net.ebh.exam.util.HMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/2/21.
 */
@Service
public class KuQuestionDao {

    @Autowired
    @Getter
    KuQuestionRepository kuQuestionRepository;
    @Autowired
    @Getter
    KuQuestionRelationRepository kuQuestionRelationRepository;
    @Autowired
    MongoTemplate mongoTemplate;

    public KuQuestion saveKuquestion(KuQuestion kuQuestion) {
        return kuQuestionRepository.save(kuQuestion);
    }


    /**
     * 根据条件获取
     *
     * @param condition
     * @param
     * @return
     */
    public List<KuQuestion> getExamCountByCondition(Condition condition) {
        List<KuQuestion> list = new ArrayList<>();
        Criteria criteria = new Criteria();
        if (!StringUtils.isEmpty(condition.getTtype())) {
            criteria.and("ttype").is(condition.getTtype());
        }
        if (!StringUtils.isEmpty(condition.getRelationname())) {
            criteria.and("relationname").is(condition.getRelationname());
        }
        if (!ObjectUtils.isEmpty(condition.getTid())) {
            criteria.and("tid").is(condition.getTid());
        }
        if (!StringUtils.isEmpty(condition.getPath())) {
            criteria.and("path").is(condition.getPath());
        }
        List<KuQuestionRelation> kuQuestionRelationList = mongoTemplate.find(new Query(criteria), KuQuestionRelation.class);
        if (ObjectUtils.isEmpty(kuQuestionRelationList)) {
            return list;
        }
        for (KuQuestionRelation kuQuestionRelation : kuQuestionRelationList) {
            Query query = new Query();
            Criteria ctr = new Criteria();
            if (!StringUtils.isEmpty(condition.getQuetype())) {
                ctr.and("queType").is(condition.getQuetype());
            }
            ctr.and("kuqid").is(kuQuestionRelation.getKuqid());
            query.addCriteria(ctr);
            KuQuestion kuQuestion = mongoTemplate.findOne(query, KuQuestion.class);
            if (!ObjectUtils.isEmpty(kuQuestion)) {
                list.add(kuQuestion);
            }
        }
        return list;
    }
//
//    /**
//     * 根据condition和uid,crid查询题库
//     *
//     * @param condition
//     * @param user
//     * @return
//     */
//    public List<KuQuestion> getQuestionListByCondition(Condition condition, User user) {
//        List<KuQuestion> kuQuestionList = new ArrayList<>();
//        KuQuestionRelation relation = new KuQuestionRelation();
//        relation.setPath(condition.getPath());
//        relation.setTid(condition.getTid());
//        relation.setRelationname(condition.getRelationname());
//        relation.setTtype(condition.getTtype());
//        List<KuQuestionRelation> kuQuestionRelations = mongoTemplate.find(new Query(Criteria.byExample(relation)), KuQuestionRelation.class);
//        for (KuQuestionRelation kuQuestionRelation : kuQuestionRelations) {
//            KuQuestion kuQuestion = new KuQuestion();
//            kuQuestion.setUid(user.getUid());
//            kuQuestion.setCrid(user.getCrid());
//            kuQuestion.setQueType(condition.getQuetype());
//            kuQuestion.setKuqid(kuQuestionRelation.getKuqid());
//            kuQuestionList = mongoTemplate.find(new Query(Criteria.byExample(kuQuestion)), KuQuestion.class);
//        }
//        return kuQuestionList;
//    }

    /**
     * 将question添加到题库
     *
     * @param question
     */
    public void addToKuQuestion(Question question) throws Exception {
        if (ObjectUtils.isEmpty(question)) {
            throw new CException(ErrorCode.QUESTION_ISNOT_EXIST);
        }
        KuQuestion kuQuestion = kuQuestionRepository.findByMd5codeAndUidAndCrid(question.getMd5code(), question.getUid(), question.getCrid());
        if (!ObjectUtils.isEmpty(kuQuestion) && kuQuestion.getDtag() == 1) {
            kuQuestion.setDtag(0);//将删除1标识改为0正常
        } else if (!ObjectUtils.isEmpty(kuQuestion) && kuQuestion.getDtag() == 0) {
            return;
        } else {
            kuQuestion = new KuQuestion();
            kuQuestion.setQsubject(question.getQsubject());
            kuQuestion.setCrid(question.getCrid());
            kuQuestion.setData(question.getData());
            kuQuestion.setExtdata(question.getExtdata());
            kuQuestion.setLevel(question.getLevel());
            kuQuestion.setMd5code(question.getMd5code());
            kuQuestion.setQueType(question.getQueType());
            kuQuestion.setQuescore(question.getQuescore());
            kuQuestion.setExtdata(question.getExtdata());
            kuQuestion.setUid(question.getUid());
            kuQuestion.setStatus(1);
            kuQuestion.setDateline(CUtil.getUnixTimestamp());
            kuQuestion.setVersion(question.getVersion());
            kuQuestion.setDtag(question.getDtag());
        }
        //保存kuquestion
        kuQuestion = kuQuestionRepository.save(kuQuestion);
        //解析relation并保存
        List<KuQuestionRelation> kuQuestionRelationList = HMapper.parseData2List(question.getData(), "relationSet", KuQuestionRelation[].class);
        for (KuQuestionRelation kuQuestionRelation : kuQuestionRelationList) {
            kuQuestionRelation.setKuqid(kuQuestion.getKuqid());
            kuQuestionRelationRepository.save(kuQuestionRelation);
        }
    }

    /**
     * 获取教师选题的时候的题库题目列表
     *
     * @param params
     * @return
     */
    public Long getCountForKu(HMapper params) {
        List<KuQuestionRelation> kuQuestionRelationList = null;
        String path = params.getString("path");
        if (!StringUtils.isEmpty(path)) {
            Query query = new Query();
            Criteria criteria = new Criteria();
            criteria.and("path").is(path);
            query.addCriteria(criteria);
            query.fields().include("kuqid");
            kuQuestionRelationList = mongoTemplate.find(query, KuQuestionRelation.class);
        }
        if (ObjectUtils.isEmpty(kuQuestionRelationList)) {
            return null;
        }
        Query query = new Query();
        Criteria criteria = new Criteria();
        //删除标志判断
        Integer dtag = params.getInteger("dtag");
        if (dtag == null) {
            dtag = new Integer(0);
        }
        criteria.and("dtag").is(dtag);
        String queType = params.getString("queType");
        if (!StringUtils.isEmpty(queType)) {
            criteria.and("queType").is(queType);
        }
        Long crid = params.getLong("crid");
        if (!ObjectUtils.isEmpty("crid")) {
            criteria.and("crid").is(crid);
        }
        criteria.and("kuqid").in(kuQuestionRelationList);
        query.addCriteria(criteria);
        return mongoTemplate.count(query, KuQuestion.class);
    }

    /**
     * 逻辑删除，设置dtag=1
     *
     * @param kuqid
     */
    public void deleteKu(long kuqid) {
        Update update = new Update();
        update.set("dtag", 1);
        mongoTemplate.updateFirst(new Query(new Criteria().and("kuqid").is(kuqid)), update, KuQuestion.class);
    }

    public KuQuestion SaveKu(KuQuestion kuQuestion) {
        kuQuestion = kuQuestionRepository.save(kuQuestion);
        List<KuQuestionRelation> kuQuestionRelationList = HMapper.parseData2List(kuQuestion.getData(), "relationSet", KuQuestionRelation[].class);
        for (KuQuestionRelation kuQuestionRelation : kuQuestionRelationList) {
            kuQuestionRelation.setKuqid(kuQuestion.getKuqid());
            kuQuestionRelationRepository.save(kuQuestionRelation);

        }
        return kuQuestion;
    }

    /**
     * 获取教师选题的时候的题库题目列表
     *
     * @param params
     * @return
     */
    public Page<KuQuestion> getPageForKu(HMapper params) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        Long tid = params.getLong("tid");
        String ttype = params.getString("ttype");
        String path = params.getString("path");
        if (tid != null) {
            criteria.and("tid").is(tid);
        }
        if (!StringUtils.isEmpty(path)) {
            criteria.and("path").regex(path + ".*");
        }

        if (!ObjectUtils.isEmpty(ttype)) {
            criteria.and("ttype").is(ttype);
        }
        query.fields().include("kuqid");
        query.addCriteria(criteria);
        List<KuQuestionRelation> kuQuestionRelationList = mongoTemplate.find(query, KuQuestionRelation.class);
        List<Long> kuqidList = kuQuestionRelationList.stream().map(KuQuestionRelation::getKuqid).distinct().collect(Collectors.toList());
        Long[] qids = params.getObject("kuqids", Long[].class);
        query = new Query();
        criteria = new Criteria();
        if (!ObjectUtils.isEmpty(qids)) {
            List<Long> kuqids = Arrays.asList(qids);
            kuqidList.retainAll(kuqids);
            criteria.and("kuqid").in(kuqidList);
        } else {
            criteria.and("kuqid").in(kuqidList);
        }
        //删除标志判断
        Integer dtag = params.getInteger("dtag");
        if (dtag == null) {
            dtag = new Integer(0);
        }
        criteria.and("dtag").is(dtag);

        Long uid = params.getLong("uid");
        if (!ObjectUtils.isEmpty(uid)) {
            criteria.and("uid").is(uid);
        }
        String q = params.getString("q");
        if (!StringUtils.isEmpty(q)) {
            criteria.and("qsubject").regex(".*?" + q + ".*");
        }
        Long crid = params.getLong("crid");
        if (!ObjectUtils.isEmpty("crid")) {
            criteria.and("crid").is(crid);
        }

        Integer status = params.getInteger("status");
        if (status != null) {
            criteria.and("status").is(status);
        }

        QueType queType = params.getObject("queType", QueType.class);
        if (!ObjectUtils.isEmpty(queType)) {
            criteria.and("queType").is(queType);
        }
        query.addCriteria(criteria);
        long count = mongoTemplate.count(query, KuQuestion.class);
        List<KuQuestion> kuQuestionList = mongoTemplate.find(query.with(params.parsePage()), KuQuestion.class);
        return new PageImpl<>(kuQuestionList, params.parsePage(), count);
    }

    /**
     * 判断指定md5code的试题是否已经加入了教师的题库
     *
     * @param params
     * @return
     */
    public KuQuestion ifinku(HMapper params) {
        String md5code = params.getString("md5code", true);
        Long uid = params.getLong("uid");
        if (StringUtils.isEmpty(md5code) || uid == null) {
            return null;
        }
        List<KuQuestion> kuQuestionList = mongoTemplate.find(new Query(new Criteria().and("md5code").is(md5code).and("uid").is(uid)), KuQuestion.class);
        return ObjectUtils.isEmpty(kuQuestionList) ? null : kuQuestionList.stream().findFirst().get();
    }


    public List<QueType> getKuquestionTypeList() {
        Query query = new Query();
        query.fields().include("queType");
        List<QueType> quetypeList = mongoTemplate.find(query, KuQuestion.class).stream().map(KuQuestion::getQueType).distinct().collect(Collectors.toList());
        return quetypeList;
    }

    /**
     * 分析题库
     *
     * @param hMapper
     */
    public Long kufenxi(HMapper hMapper) {
        Query query = new Query();
        Criteria criteria = new Criteria();

        long uid = hMapper.getLongValue("uid");
        if (uid != 0) {

        }
        Integer dtag = hMapper.getInteger("dtag");
        if (dtag == null) {
            dtag = 0;
        }
        criteria.and("dtag").is(dtag);
        criteria.and("status").is(1);//分析已经发布的题库（status=1）
        QueType quetype = hMapper.getObject("quetype", QueType.class);
        criteria.and("queType").is(quetype);
        long crid = hMapper.getLongValue("crid");
        criteria.and("crid").is(crid);
        return mongoTemplate.count(query, KuQuestion.class);
    }
}
