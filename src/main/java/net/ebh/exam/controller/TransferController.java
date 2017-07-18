package net.ebh.exam.controller;

import net.ebh.exam.TempVo.SequenceId;
import net.ebh.exam.TempVo.User;
import net.ebh.exam.bean.*;
import net.ebh.exam.jpas.*;
import net.ebh.exam.util.CException;
import net.ebh.exam.util.CheckResult;
import net.ebh.exam.util.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by xh on 2017/5/18.
 */
@RestController
@RequestMapping("transfer")
public class TransferController {
    @Autowired
    ExamDaos examDaos;
    @Autowired
    QuestionDaos questionDaos;
    @Autowired
    AnswerBlankDetailDaos answerBlankDetailDaos;
    @Autowired
    AnswerQueDetailDaos answerQueDetailDaos;
    @Autowired
    BlankDaos blankDaos;
    @Autowired
    ErrorBookDaos errorBookDaos;
    @Autowired
    ExamRelationDaos examRelationDaos;
    @Autowired
    KuQuestionDaos kuQuestionDaos;
    @Autowired
    KuQuestionRelationDaos kuQuestionRelationDaos;
    @Autowired
    QuestionRelationDaos questionRelationDaos;
    @Autowired
    UserAnswerDaos userAnswerDaos;
    @Autowired
    MongoTemplate mongoTemplate;
    private static final int size = 2000;


    @RequestMapping("exam/{key}")
    public CheckResult transferExam(@PathVariable String key) throws Exception {
        User user = User.getUser(key);
        if (!ObjectUtils.isEmpty(user)) {
            throw new CException(ErrorCode.USER_VALIDATION_FAILS);
        }
        long b = System.currentTimeMillis();
        CheckResult checkResult = new CheckResult();
        int i = insertExam();
        int i1 = insertQuestion();
        int i2 = insertAnswerBlankDetails();
        int i3 = insertAnswerQueDetails();
        int i4 = insertBlanks();
        int i5 = insertExamRelation();
        int i6 = insertKuQuestion();
        int i7 = insertKuQuestionRelation();
        int i8 = insertQuestionRelation();
        int i9 = insertUserAnswer();
        int i10 = insertErrorBook();
        int ii = i + i1 + i2 + i3 + i4 + i5 + i6 + i7 + i8 + i9 + i10;

        long a = System.currentTimeMillis();
        String data = "总数据：=" + ii + "条数据" + "  耗时：=" + ((a - b) / 1000) + "秒";
        checkResult.addErrData("msg", data);
        return checkResult;
    }

    @Async
    public int insertExam() {
        long count = examDaos.count();
        int pagesize = (int) (count % size == 0 ? (count / size) : (count / size) + 1);
        for (int i = 0; i < pagesize; i++) {
            Page<net.ebh.exam.vo.Exam> page = examDaos.findAll(new PageRequest(i, size));
            page.getContent().forEach(examvo -> {
                Exam exam = transferExam(examvo);
                mongoTemplate.insert(exam);
                mongoTemplate.updateFirst(new Query(new Criteria().and("collName").is("net.ebh.exam.bean.Exam")), Update.update("seqId", examvo.getEid()), SequenceId.class);
            });
        }
        return (int) count;
    }

    @Async
    public int insertQuestion() {
        long count = questionDaos.count();
        int pagesize = (int) (count % size == 0 ? (count / size) : (count / size) + 1);
        for (int i = 0; i < pagesize; i++) {
            Page<net.ebh.exam.vo.Question> page = questionDaos.findAll(new PageRequest(i, size));
            page.getContent().forEach(questionvo -> {
                Question question = transferQuestion(questionvo);
                mongoTemplate.insert(question);
                mongoTemplate.updateFirst(new Query(new Criteria().and("collName").is("net.ebh.exam.bean.Question")), Update.update("seqId", questionvo.getQid()), SequenceId.class);
            });
        }
        return (int) count;
    }

    @Async
    public int insertAnswerBlankDetails() {
        long count = answerBlankDetailDaos.count();
        int pagesize = (int) (count % size == 0 ? (count / size) : (count / size) + 1);
        for (int i = 0; i < pagesize; i++) {
            Page<net.ebh.exam.vo.AnswerBlankDetail> page = answerBlankDetailDaos.findAll(new PageRequest(i, size));
            page.getContent().forEach(answerBlankDetail -> {
                AnswerBlankDetail blankDetail = transferAnswerBlankDetail(answerBlankDetail);
                mongoTemplate.insert(blankDetail);
                mongoTemplate.updateFirst(new Query(new Criteria().and("collName").is("net.ebh.exam.bean.AnswerBlankDetail")), Update.update("seqId", answerBlankDetail.getDbid()), SequenceId.class);
            });
        }
        return (int) count;
    }

    @Async
    public int insertAnswerQueDetails() {
        long count = answerQueDetailDaos.count();
        int pagesize = (int) (count % size == 0 ? (count / size) : (count / size) + 1);
        for (int i = 0; i < pagesize; i++) {
            Page<net.ebh.exam.vo.AnswerQueDetail> page = answerQueDetailDaos.findAll(new PageRequest(i, size));
            page.getContent().forEach(answerQueDetail -> {
                AnswerQueDetail queDetail = transferAnswerQueDetail(answerQueDetail);
                mongoTemplate.insert(queDetail);
                mongoTemplate.updateFirst(new Query(new Criteria().and("collName").is("net.ebh.exam.bean.AnswerQueDetail")), Update.update("seqId", answerQueDetail.getDqid()), SequenceId.class);
            });
        }
        return (int) count;
    }

    @Async
    public int insertBlanks() {
        long count = blankDaos.count();
        int pagesize = (int) (count % size == 0 ? (count / size) : (count / size) + 1);
        for (int i = 0; i < pagesize; i++) {
            Page<net.ebh.exam.vo.Blank> page = blankDaos.findAll(new PageRequest(i, size));
            page.getContent().forEach(blank -> {
                Blank b = transferBlank(blank);
                mongoTemplate.insert(b);
                mongoTemplate.updateFirst(new Query(new Criteria().and("collName").is("net.ebh.exam.bean.Blank")), Update.update("seqId", blank.getBid()), SequenceId.class);
            });
        }
        return (int) count;
    }

    @Async
    public int insertExamRelation() {
        long count = examRelationDaos.count();
        int pagesize = (int) (count % size == 0 ? (count / size) : (count / size) + 1);
        for (int i = 0; i < pagesize; i++) {
            Page<net.ebh.exam.vo.ExamRelation> page = examRelationDaos.findAll(new PageRequest(i, size));
            page.getContent().forEach(examRelation -> {
                ExamRelation relation = transferExamRelation(examRelation);
                mongoTemplate.insert(relation);
                mongoTemplate.updateFirst(new Query(new Criteria().and("collName").is("net.ebh.exam.bean.ExamRelation")), Update.update("seqId", examRelation.getRelationid()), SequenceId.class);
            });
        }
        return (int) count;
    }

    @Async
    public int insertKuQuestion() {
        long count = kuQuestionDaos.count();
        int pagesize = (int) (count % size == 0 ? (count / size) : (count / size) + 1);
        for (int i = 0; i < pagesize; i++) {
            Page<net.ebh.exam.vo.KuQuestion> page = kuQuestionDaos.findAll(new PageRequest(i, size));
            page.getContent().forEach(kuQuestion -> {
                KuQuestion ku = transferKuQuestion(kuQuestion);
                mongoTemplate.insert(ku);
                mongoTemplate.updateFirst(new Query(new Criteria().and("collName").is("net.ebh.exam.bean.Blank")), Update.update("seqId", kuQuestion.getKuqid()), SequenceId.class);
            });
        }
        return (int) count;
    }

    @Async
    public int insertKuQuestionRelation() {
        long count = kuQuestionRelationDaos.count();
        int pagesize = (int) (count % size == 0 ? (count / size) : (count / size) + 1);
        for (int i = 0; i < pagesize; i++) {
            Page<net.ebh.exam.vo.KuQuestionRelation> page = kuQuestionRelationDaos.findAll(new PageRequest(i, size));
            page.getContent().forEach(kuQuestionRelation -> {
                KuQuestionRelation relation = transferKuQuestionRelation(kuQuestionRelation);
                mongoTemplate.insert(relation);
                mongoTemplate.updateFirst(new Query(new Criteria().and("collName").is("net.ebh.exam.bean.KuQuestionRelation")), Update.update("seqId", kuQuestionRelation.getRelationid()), SequenceId.class);
            });
        }
        return (int) count;
    }

    @Async
    public int insertQuestionRelation() {
        long count = questionRelationDaos.count();
        int pagesize = (int) (count % size == 0 ? (count / size) : (count / size) + 1);
        for (int i = 0; i < pagesize; i++) {
            Page<net.ebh.exam.vo.QuestionRelation> page = questionRelationDaos.findAll(new PageRequest(i, size));
            page.getContent().forEach(questionRelation -> {
                QuestionRelation relation = transferQuestionRelation(questionRelation);
                mongoTemplate.insert(relation);
                mongoTemplate.updateFirst(new Query(new Criteria().and("collName").is("net.ebh.exam.bean.QuestionRelation")), Update.update("seqId", questionRelation.getRelationid()), SequenceId.class);
            });
        }
        return (int) count;
    }

    @Async
    public int insertUserAnswer() {
        long count = userAnswerDaos.count();
        int pagesize = (int) (count % size == 0 ? (count / size) : (count / size) + 1);
        for (int i = 0; i < pagesize; i++) {
            Page<net.ebh.exam.vo.UserAnswer> page = userAnswerDaos.findAll(new PageRequest(i, size));
            page.getContent().forEach(userAnswer -> {
                UserAnswer u = transferUserAnswer(userAnswer);
                mongoTemplate.insert(u);
                mongoTemplate.updateFirst(new Query(new Criteria().and("collName").is("net.ebh.exam.bean.UserAnswer")), Update.update("seqId", userAnswer.getAid()), SequenceId.class);
            });
        }
        return (int) count;
    }

    @Async
    public int insertErrorBook() {
        long count = errorBookDaos.count();
        int pagesize = (int) (count % size == 0 ? (count / size) : (count / size) + 1);
        for (int i = 0; i < pagesize; i++) {
            Page<net.ebh.exam.vo.ErrorBook> page = errorBookDaos.findAll(new PageRequest(i, size));
            page.getContent().forEach(errorBook -> {
                ErrorBook e = transferErrorBook(errorBook);
                mongoTemplate.insert(e);
                mongoTemplate.updateFirst(new Query(new Criteria().and("collName").is("net.ebh.exam.bean.ErrorBook")), Update.update("seqId", errorBook.getErrorid()), SequenceId.class);
            });
        }
        return (int) count;
    }

    private Exam transferExam(net.ebh.exam.vo.Exam examvo) {
        Exam exam = new Exam();
        exam.setEid(examvo.getEid());
        exam.setData(examvo.getData());
        exam.setCrid(examvo.getCrid());
        exam.setCanreexam(examvo.getCanreexam());
        exam.setCanpusherror(examvo.getCanpusherror());
        exam.setAnsstarttime(examvo.getAnsstarttime());
        exam.setAnsendtime(examvo.getAnsendtime());
        exam.setEorder(examvo.getEorder());
        exam.setExamtotalscore(examvo.getExamtotalscore());
        exam.setDateline(examvo.getDateline());
        exam.setEtype(examvo.getEtype());
        exam.setUid(examvo.getUid());
        exam.setEsubject(examvo.getEsubject());
        exam.setEstype(examvo.getEstype());
        exam.setExamendtime(examvo.getExamendtime());
        exam.setExamstarttime(examvo.getExamstarttime());
        exam.setFromeid(examvo.getFromeid());
        exam.setIsclass(examvo.getIsclass());
        exam.setLimittime(examvo.getLimittime());
        exam.setStatus(examvo.getStatus());
        exam.setStucancorrect(examvo.getStucancorrect());
        exam.setDtag(examvo.getDtag());
        exam.setVersion(examvo.getVersion());
        return exam;
    }

    private AnswerBlankDetail transferAnswerBlankDetail(net.ebh.exam.vo.AnswerBlankDetail vo) {

        AnswerBlankDetail answerBlankDetail = new AnswerBlankDetail();
        answerBlankDetail.setDbid(vo.getDbid());
        answerBlankDetail.setBid(vo.getBid());
        answerBlankDetail.setContent(vo.getContent());
        answerBlankDetail.setDqid(vo.getDqid());
        answerBlankDetail.setQid(vo.getQid());
        answerBlankDetail.setScore(vo.getScore());
        answerBlankDetail.setStatus(vo.getStatus());
        answerBlankDetail.setUid(vo.getUid());
        answerBlankDetail.setDtag(vo.getDtag());
        answerBlankDetail.setVersion(vo.getVersion());
        return answerBlankDetail;
    }

    private AnswerQueDetail transferAnswerQueDetail(net.ebh.exam.vo.AnswerQueDetail vo) {
        AnswerQueDetail answerQueDetail = new AnswerQueDetail();
        answerQueDetail.setDqid(vo.getDqid());
        answerQueDetail.setMarkuid(vo.getMarkuid());
        answerQueDetail.setUid(vo.getUid());
        answerQueDetail.setAid(vo.getAid());
        answerQueDetail.setAllright(vo.getAllright());
        answerQueDetail.setChoicestr(vo.getChoicestr());
        answerQueDetail.setData(vo.getData());
        answerQueDetail.setQid(vo.getQid());
        answerQueDetail.setRemark(vo.getRemark());
        answerQueDetail.setStatus(vo.getStatus());
        answerQueDetail.setTotalscore(vo.getTotalscore());
        answerQueDetail.setDtag(vo.getDtag());
        answerQueDetail.setVersion(vo.getVersion());
        return answerQueDetail;
    }

    private Blank transferBlank(net.ebh.exam.vo.Blank vo) {
        Blank blank = new Blank();
        blank.setBid(vo.getBid());
        blank.setScore(vo.getScore());
        blank.setQid(vo.getQid());
        blank.setBsubject(vo.getBsubject());
        blank.setIsanswer(vo.isanswer());
        blank.setDtag(vo.getDtag());
        blank.setVersion(vo.getVersion());
        return blank;
    }

    private ErrorBook transferErrorBook(net.ebh.exam.vo.ErrorBook vo) {
        ErrorBook errorBook = new ErrorBook();
        errorBook.setErrorid(vo.getErrorid());
        errorBook.setQid(vo.getQid());
        errorBook.setDateline(vo.getDateline());
        errorBook.setDqid(vo.getDqid());
        errorBook.setStyle(vo.getStyle());
        errorBook.setUid(vo.getUid());
        errorBook.setDtag(vo.getDtag());
        errorBook.setVersion(vo.getVersion());
        return errorBook;
    }

    private ExamRelation transferExamRelation(net.ebh.exam.vo.ExamRelation vo) {
        ExamRelation examRelation = new ExamRelation();
        examRelation.setRelationid(vo.getRelationid());
        examRelation.setEid(vo.getEid());
        examRelation.setClassid(vo.getClassid());
        examRelation.setDtag(vo.getDtag());
        examRelation.setExtdata(vo.getExtdata());
        examRelation.setPath(vo.getPath());
        examRelation.setRelationname(vo.getRelationname());
        examRelation.setRemark(vo.getRemark());
        examRelation.setTid(vo.getTid());
        examRelation.setTtype(vo.getTtype());
        examRelation.setVersion(vo.getVersion());
        return examRelation;
    }

    private KuQuestion transferKuQuestion(net.ebh.exam.vo.KuQuestion vo) {
        KuQuestion kuQuestion = new KuQuestion();
        kuQuestion.setKuqid(vo.getKuqid());
        kuQuestion.setCrid(vo.getCrid());
        kuQuestion.setData(vo.getData());
        kuQuestion.setDateline(vo.getDateline());
        kuQuestion.setExtdata(vo.getExtdata());
        kuQuestion.setLevel(vo.getLevel());
        kuQuestion.setMd5code(vo.getMd5code());
        kuQuestion.setQsubject(vo.getQsubject());
        kuQuestion.setQuescore(vo.getQuescore());
        kuQuestion.setQueType(vo.getQuetype());
        kuQuestion.setStatus(vo.getStatus());
        kuQuestion.setUid(vo.getUid());
        kuQuestion.setDtag(vo.getDtag());
        kuQuestion.setVersion(vo.getVersion());
        return kuQuestion;
    }

    private KuQuestionRelation transferKuQuestionRelation(net.ebh.exam.vo.KuQuestionRelation vo) {
        KuQuestionRelation relation = new KuQuestionRelation();
        relation.setRelationid(vo.getRelationid());
        relation.setKuqid(vo.getKuqid());
        relation.setDtag(vo.getDtag());
        relation.setExtdata(vo.getExtdata());
        relation.setPath(vo.getPath());
        relation.setRelationname(vo.getRelationname());
        relation.setRemark(vo.getRemark());
        relation.setTid(vo.getTid());
        relation.setTtype(vo.getTtype());
        relation.setVersion(vo.getVersion());
        return relation;
    }

    private Question transferQuestion(net.ebh.exam.vo.Question vo) {
        Question question = new Question();
        question.setQid(vo.getQid());
        question.setEid(vo.getEid());
        question.setMd5code(vo.getMd5code());
        question.setChoicestr(vo.getChoicestr());
        question.setCrid(vo.getCrid());
        question.setData(vo.getData());
        question.setDateline(vo.getDateline());
        question.setExtdata(vo.getExtdata());
        question.setLevel(vo.getLevel());
        question.setQsubject(vo.getQsubject());
        question.setQuescore(vo.getQuescore());
        question.setQueType(vo.getQuetype());
        question.setStatus(vo.getStatus());
        question.setUid(vo.getUid());
        question.setDtag(vo.getDtag());
        question.setVersion(vo.getVersion());
        return question;
    }

    private QuestionRelation transferQuestionRelation(net.ebh.exam.vo.QuestionRelation vo) {
        QuestionRelation relation = new QuestionRelation();
        relation.setRelationid(vo.getRelationid());
        relation.setQid(vo.getQid());
        relation.setDtag(vo.getDtag());
        relation.setExtdata(vo.getExtdata());
        relation.setPath(vo.getPath());
        relation.setRelationname(vo.getRelationname());
        relation.setRemark(vo.getRemark());
        relation.setTid(vo.getTid());
        relation.setTtype(vo.getTtype());
        relation.setVersion(vo.getVersion());
        return relation;
    }

    private UserAnswer transferUserAnswer(net.ebh.exam.vo.UserAnswer vo) {
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAid(vo.getAid());
        userAnswer.setAnsdateline(vo.getAnsdateline());
        userAnswer.setAnstotalscore(vo.getAnstotalscore());
        userAnswer.setAorder(vo.getAorder());
        userAnswer.setCorrectrat(vo.getCorrectrat());
        userAnswer.setData(vo.getData());
        userAnswer.setEid(vo.getEid());
        userAnswer.setFromeid(vo.getFromeid());
        userAnswer.setRemark(vo.getRemark());
        userAnswer.setStatus(vo.getStatus());
        userAnswer.setUid(vo.getUid());
        userAnswer.setUsedtime(vo.getUsedtime());
        userAnswer.setDtag(vo.getDtag());
        userAnswer.setVersion(vo.getVersion());
        return userAnswer;
    }
}
