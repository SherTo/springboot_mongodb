package net.ebh.exam.jpas;

import net.ebh.exam.vo.ErrorBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by zkq on 2016/6/16.
 * 错题集持久接口
 */
@Transactional
@Repository
public interface ErrorBookDaos extends JpaRepository<ErrorBook, Long> {
}
