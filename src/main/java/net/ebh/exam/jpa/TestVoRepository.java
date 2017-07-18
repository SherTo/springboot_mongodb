package net.ebh.exam.jpa;

import net.ebh.exam.TempVo.TestVO;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by xh on 2017/5/5.
 */
public interface TestVoRepository extends MongoRepository<TestVO, String> {
}
