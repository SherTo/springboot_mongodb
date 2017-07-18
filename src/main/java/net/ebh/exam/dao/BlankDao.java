package net.ebh.exam.dao;

import net.ebh.exam.bean.Blank;
import net.ebh.exam.jpa.BlankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by zkq on 2016/5/23.
 * 填空或者选项dao
 */
@Service
public class BlankDao {
    @Autowired
    private BlankRepository blankRepository;

    public BlankRepository getBlankRepository() {
        return blankRepository;
    }

    public Blank doSave(Blank blank) {
        return blankRepository.save(blank);
    }

    public Blank findById(long bid) {
        return blankRepository.getBlankListByQid(bid).get(0);
    }

    public List<Blank> getBlankListByQid(long qid) {
        return blankRepository.getBlankListByQid(qid);
    }
}
