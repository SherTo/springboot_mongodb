package net.ebh.exam.jpa;

import net.ebh.exam.base.QueType;
import net.ebh.exam.bean.Question;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by zkq on 2016/5/23.
 */
@Transactional
@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {
    Question findByQid(long qid);
}
