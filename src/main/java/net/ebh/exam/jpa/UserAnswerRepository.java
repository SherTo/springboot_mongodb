package net.ebh.exam.jpa;

import net.ebh.exam.bean.UserAnswer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by zkq on 2016/5/25.
 */
@Repository
@Transactional
public interface UserAnswerRepository extends MongoRepository<UserAnswer, String> {
    UserAnswer findByAid(long aid);

    List<UserAnswer> findUserAnswerByUidAndEid(long uid, long eid);
}
