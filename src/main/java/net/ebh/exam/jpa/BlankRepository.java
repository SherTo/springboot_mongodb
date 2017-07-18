package net.ebh.exam.jpa;

import net.ebh.exam.bean.Blank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by zkq on 2016/5/23.
 */
@Transactional
@Repository
public interface BlankRepository extends MongoRepository<Blank, String> {
    Blank findByBid(long bid);
    List<Blank> getBlankListByQid(long qid);
}
