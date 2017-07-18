package net.ebh.exam.jpa;

import net.ebh.exam.bean.AnswerQueDetail;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by xh on 2016/6/6.
 */
@Transactional
@Repository
public interface AnswerQueDetailRepository extends MongoRepository<AnswerQueDetail, String> {
    AnswerQueDetail findByDqid(long dqid);
    List<AnswerQueDetail> findAnswerQueDetailListByAid(long aid);
}
