package net.ebh.exam.service;

import net.ebh.exam.bean.AnswerBlankDetail;
import net.ebh.exam.bean.AnswerQueDetail;
import net.ebh.exam.dao.AnswerBlankDetailDao;
import net.ebh.exam.dao.AnswerQueDetailDao;
import net.ebh.exam.util.HMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Created by xh on 2017/4/19.
 */
@Service
public class AnswerDetailService {
    @Autowired
    AnswerQueDetailDao answerDetailDao;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    AnswerBlankDetailDao answerBlankDetailDao;

    public AnswerQueDetailDao getAnswerDetailDao() {
        return answerDetailDao;
    }

    public AnswerQueDetail getAnswerQueDetailByDqid(long dqid) {
        return answerDetailDao.getAnswerQueDetailByDqid(dqid);
    }

    public List<AnswerQueDetail> getAnswerDetailList(long qid) {
        return answerDetailDao.getAnswerQueDetailListByQid(qid);
    }

    /**
     * 获取答题分页
     *
     * @param hMapper
     * @return
     * @throws Exception
     */

    public Page<AnswerQueDetail> getAnswerListBypage(HMapper hMapper) throws Exception {
        Query query = new Query();
        Criteria criteria = new Criteria();
        //删除标志判断
        Integer dtag = hMapper.getInteger("dtag");
        if (dtag == null) {
            dtag = 0;
        }
        List<Long> uids = hMapper.arr2List("uids", Long[].class);
        if (!ObjectUtils.isEmpty(uids)) {
            criteria.and("uid").in(uids);
        }
        criteria.and("dtag").is(dtag);
        String choicestr = hMapper.getString("choicestr");
        if (!StringUtils.isEmpty(choicestr)) {
            criteria.and("choicestr").is(choicestr);
        }
        long qid = hMapper.getLong("qid");
        if (qid != 0) {
            criteria.and("qid").is(qid);
        }
        query.addCriteria(criteria);
        long count = mongoTemplate.count(query, AnswerQueDetail.class);
        List<AnswerQueDetail> answerQueDetailList = mongoTemplate.find(query.with(hMapper.parsePage()), AnswerQueDetail.class);
        return new PageImpl<>(answerQueDetailList, hMapper.parsePage(), count);
    }

    public List<AnswerQueDetail> getAnswerDetailListByAid(long aid) {
        return answerDetailDao.getAnswerQueDetailListByAid(aid);
    }

    public HMapper getAnswerQueDetailMap(long dqid) {
        AnswerQueDetail answerQueDetail = answerDetailDao.getAnswerQueDetailByDqid(dqid);
        HMapper hMapper = new HMapper();
        List<AnswerBlankDetail> answerBlankDetailList = answerBlankDetailDao.getBlankListByDqid(answerQueDetail.getDqid());
        hMapper.put("answerBlankDetails", answerBlankDetailList);
        hMapper.put("aid", answerQueDetail.getAid());
        hMapper.put("allright", answerQueDetail.getAllright());
        hMapper.put("choicestr", answerQueDetail.getChoicestr());
        hMapper.put("data", answerQueDetail.getData());
        hMapper.put("dqid", answerQueDetail.getDqid());
        hMapper.put("totalsocre", answerQueDetail.getTotalscore());
        hMapper.put("uid", answerQueDetail.getUid());
        hMapper.put("status", answerQueDetail.getStatus());
        hMapper.put("remark", answerQueDetail.getRemark());
        return hMapper;
    }
}
