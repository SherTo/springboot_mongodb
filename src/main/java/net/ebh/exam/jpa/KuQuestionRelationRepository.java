package net.ebh.exam.jpa;

import net.ebh.exam.bean.KuQuestionRelation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by zkq on 2016/6/2.
 */
@Repository
@Transactional
public interface KuQuestionRelationRepository extends MongoRepository<KuQuestionRelation, String> {
    KuQuestionRelation findByRelationid(long relationid);
}
