package net.ebh.exam.jpas;

import net.ebh.exam.vo.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by zkq on 2016/5/23.
 */
@Transactional
@Repository
public interface QuestionDaos extends JpaRepository<Question, Long> {
}
