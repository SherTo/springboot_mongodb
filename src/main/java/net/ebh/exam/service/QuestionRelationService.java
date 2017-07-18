package net.ebh.exam.service;

import net.ebh.exam.bean.QuestionRelation;
import net.ebh.exam.dao.QuestionRelationDao;
import net.ebh.exam.util.HMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Administrator on 2016/8/9.
 */
@Service
public class QuestionRelationService {
    @Resource
    private QuestionRelationDao questionRelationDao;

    public List<QuestionRelation> findAllQueRelation(HMapper hMapper) {
        return questionRelationDao.getAll(hMapper);
    }
}
