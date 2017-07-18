package net.ebh.exam.dao;

import net.ebh.exam.bean.ExamRelation;
import net.ebh.exam.jpa.ExamRelationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by admin on 2017/2/20.
 */
@Service
public class ExamRelationDao {
    @Autowired
    ExamRelationRepository examRelationRepository;

    public ExamRelationRepository getExamRelationRepository() {
        return examRelationRepository;
    }
    public List<ExamRelation> getExamRelationList(long eid){
        return examRelationRepository.getExamRelationListByEid(eid);
    }
}
