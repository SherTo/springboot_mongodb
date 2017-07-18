package net.ebh.exam.jpas;

import net.ebh.exam.vo.KuQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * Created by zkq on 2016/5/30.
 */
@Repository
@Transactional
public interface KuQuestionDaos extends JpaRepository<KuQuestion, Long> {
}
