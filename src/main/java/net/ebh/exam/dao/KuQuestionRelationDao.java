package net.ebh.exam.dao;

import net.ebh.exam.jpa.KuQuestionRelationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


/**
 * Created by zkq on 2016/6/2.
 */
@Repository
public class KuQuestionRelationDao {
    @Autowired
    private KuQuestionRelationRepository kuQuestionRelationRepository;

    public KuQuestionRelationRepository getKuQuestionRelationRepository() {
        return kuQuestionRelationRepository;
    }

}
