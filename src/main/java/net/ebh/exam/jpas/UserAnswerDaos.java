package net.ebh.exam.jpas;

import net.ebh.exam.vo.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by zkq on 2016/5/25.
 */
@Repository
@Transactional
public interface UserAnswerDaos extends JpaRepository<UserAnswer, Long> {
}
