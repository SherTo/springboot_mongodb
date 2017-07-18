package net.ebh.exam.jpa;

import net.ebh.exam.TempVo.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by xh on 2017/4/15.
 */
public interface UserRepository extends MongoRepository<User, String> {
}
