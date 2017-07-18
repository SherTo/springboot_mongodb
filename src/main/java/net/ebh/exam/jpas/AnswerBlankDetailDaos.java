package net.ebh.exam.jpas;

import net.ebh.exam.vo.AnswerBlankDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by xh on 2017/4/26.
 */
@Repository
public interface AnswerBlankDetailDaos extends JpaRepository<AnswerBlankDetail, Long> {
}
