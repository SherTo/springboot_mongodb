package net.ebh.exam.service;

import net.ebh.exam.bean.AnswerBlankDetail;
import net.ebh.exam.dao.AnswerBlankDetailDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by xh on 2017/5/3.
 */
@Service
public class AnswerBlankDetailService {
    @Autowired
    AnswerBlankDetailDao AnswerBlankDetailDao;

    public List<AnswerBlankDetail> getBlankListByDqid(long dqid) {
        return AnswerBlankDetailDao.getBlankListByDqid(dqid);
    }
}
