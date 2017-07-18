package net.ebh.exam.jpa;

import net.ebh.exam.bean.KuQuestion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * Created by zkq on 2016/5/30.
 */
@Repository
@Transactional
public interface KuQuestionRepository extends MongoRepository<KuQuestion, String> {
    KuQuestion findByKuqid(long kuqid);
    KuQuestion findByMd5codeAndUidAndCrid(String md5code, long uid, long crid);
}
