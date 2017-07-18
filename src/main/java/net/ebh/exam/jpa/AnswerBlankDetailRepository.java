package net.ebh.exam.jpa;

import net.ebh.exam.bean.AnswerBlankDetail;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by xh on 2017/4/26.
 */
@Repository
public interface AnswerBlankDetailRepository extends MongoRepository<AnswerBlankDetail, String> {
    List<AnswerBlankDetail> getAnswerBlankDetailByDqid(long dqid);
}
