package net.ebh.exam.jpas;

import net.ebh.exam.vo.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by xh on 2016/5/23.
 */
public interface ExamDaos extends JpaRepository<Exam, Long> {
}
