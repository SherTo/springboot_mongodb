package net.ebh.exam.jpa;

import net.ebh.exam.bean.ErrorBook;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by zkq on 2016/6/16.
 * 错题集持久接口
 */
@Transactional
@Repository
public interface ErrorBookRepository extends MongoRepository<ErrorBook, String>, PagingAndSortingRepository<ErrorBook, String> {
    ErrorBook findByErrorid(long errorid);

    List<ErrorBook> findErrorBookByUidAndDqidAndQid(long uid, long dqid, long qid);

    List<ErrorBook> getListByQid(Long qid);
}
