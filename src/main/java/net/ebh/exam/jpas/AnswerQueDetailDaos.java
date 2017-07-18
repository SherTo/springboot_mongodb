package net.ebh.exam.jpas;

import net.ebh.exam.vo.AnswerQueDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by xh on 2016/6/6.
 */
@Transactional
@Repository
public interface AnswerQueDetailDaos extends JpaRepository<AnswerQueDetail, Long> {
}
