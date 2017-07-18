package net.ebh.exam.jpa;

import net.ebh.exam.bean.QuestionRelation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2016/8/9.
 */
@Repository
public interface QuestionRelationRepository extends MongoRepository<QuestionRelation, String> {
    QuestionRelation findByRelationid(long relationid);

    List<QuestionRelation> findQuestionRealtionByQid(long oldEid);

    List<QuestionRelation> findByQid(Long qid);
}
