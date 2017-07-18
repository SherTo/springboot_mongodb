package net.ebh.exam.jpas;

import net.ebh.exam.vo.QuestionRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2016/8/9.
 */
@Repository
public interface QuestionRelationDaos extends JpaRepository<QuestionRelation, Long> {

}
