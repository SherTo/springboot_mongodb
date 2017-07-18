package net.ebh.exam.jpa;

import net.ebh.exam.bean.ExamRelation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by zkq on 2016/6/2.
 */
@Repository
@Transactional
public interface ExamRelationRepository extends MongoRepository<ExamRelation, String> {
    ExamRelation findByRelationid(long relationid);
    List<ExamRelation> getExamRelationListByEid(long eid);
}
