package net.ebh.exam.jpa;

import net.ebh.exam.bean.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by xh on 2016/5/23.
 */
@Transactional
@Repository
public interface ExamRepository extends MongoRepository<Exam, String> {
    Exam findByEid(long eid);
}
