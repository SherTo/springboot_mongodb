package net.ebh.exam.dao;

import net.ebh.exam.bean.AnswerBlankDetail;
import net.ebh.exam.jpa.AnswerBlankDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by xh on 2017/4/27.
 */
@Service
public class AnswerBlankDetailDao {
    @Autowired
    AnswerBlankDetailRepository answerBlankDetailRepository;

    public AnswerBlankDetailRepository getAnswerBlankDetailRepository() {
        return answerBlankDetailRepository;
    }

    public List<AnswerBlankDetail> getBlankListByDqid(long dqid) {
        return answerBlankDetailRepository.getAnswerBlankDetailByDqid(dqid);
    }

}
