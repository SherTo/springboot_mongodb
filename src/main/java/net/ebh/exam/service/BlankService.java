package net.ebh.exam.service;

import net.ebh.exam.bean.AnswerBlankDetail;
import net.ebh.exam.bean.Blank;
import net.ebh.exam.dao.AnswerBlankDetailDao;
import net.ebh.exam.dao.BlankDao;
import net.ebh.exam.util.CException;
import net.ebh.exam.util.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by zkq on 2016/5/23.
 * 选项或者填空服务
 */
@Service
public class BlankService {
    @Autowired
    private BlankDao blankDao;
    @Autowired
    AnswerBlankDetailDao answerBlankDetailDao;

    /**
     * 判断填空或者选项能否保存
     *
     * @param blank
     * @throws Exception
     */
    public void canSave(Blank blank) throws Exception {
        if (blank == null) {
            throw new CException(ErrorCode.BLANK_ISNOT_EXIST);
        }

        if (StringUtils.isEmpty(blank.getBsubject())) {
            throw new CException(ErrorCode.BLANKSUBJECT_IS_NULL);
        }

        if (!Arrays.asList("0", "1").contains(blank.getIsAnswer())) {
            throw new CException(ErrorCode.BLANK_RIGHT_OR_NOT);
        }
    }

    /**
     * 保存填空
     *
     * @param blank
     * @return
     * @throws Exception
     */
    public Blank doSave(Blank blank) throws Exception {
        canSave(blank);
        return blankDao.doSave(blank);
    }

    /**
     * 根据填空id获取填空实体
     *
     * @param bid
     * @return
     */
    public Blank findById(long bid) {
        if (bid == 0L) {
            return null;
        }
        return blankDao.findById(bid);
    }


    public List<AnswerBlankDetail> getBlanksDetail(long dqid) {
        return answerBlankDetailDao.getBlankListByDqid(dqid);
    }

    public List<Blank> getBlankListByQid(long qid) {
        return blankDao.getBlankListByQid(qid);
    }
    public Blank saveBlank(Blank blank){
        return blankDao.doSave(blank);
    }

}
