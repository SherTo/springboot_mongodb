package net.ebh.exam.jpas;

import net.ebh.exam.vo.KuQuestionRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by zkq on 2016/6/2.
 */
@Repository
@Transactional
public interface KuQuestionRelationDaos extends JpaRepository<KuQuestionRelation, Long> {
}
