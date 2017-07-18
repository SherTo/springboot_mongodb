package net.ebh.exam.dao;

import lombok.Getter;
import net.ebh.exam.TempVo.User;
import net.ebh.exam.base.BaseRelationForResp;
import net.ebh.exam.base.ExamType;
import net.ebh.exam.base.QueType;
import net.ebh.exam.base.RelationType;
import net.ebh.exam.bean.*;
import net.ebh.exam.jpa.ExamRepository;
import net.ebh.exam.service.UserAnswerService;
import net.ebh.exam.util.CUtil;
import net.ebh.exam.util.CheckResult;
import net.ebh.exam.util.HMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by admin on 2017/2/20.
 */
@Service
public class ExamDao {
    @Autowired
    @Getter
    ExamRepository examRepository;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    UserAnswerService userAnswerService;
    @Autowired
    QuestionRelationDao questionRelationDao;
    @Autowired
    AnswerQueDetailDao answerQueDetailDao;
    @Autowired
    QuestionDao questionDao;

    /**
     * 查询relation的eid
     *
     * @param hMapper
     * @return
     */
    public List<Long> getEidsByExamRelation(HMapper hMapper) {
        //查询relation
        List<Long> tids = hMapper.arr2List("tids", Long[].class);
        List<Long> cwids = hMapper.arr2List("cwids", Long[].class);
        List<Long> classid = hMapper.arr2List("classid", Long[].class);
        Query relationQuery = new Query();
        Criteria relationCriteria = new Criteria();
        if (!ObjectUtils.isEmpty(tids) && !ObjectUtils.isEmpty(cwids)) {
            relationCriteria.orOperator(Criteria.where("tid").in(tids).where("ttype").is(RelationType.FOLDER), Criteria.where("tid").in(cwids).where("ttype").is(RelationType.COURSE));
        } else if (!ObjectUtils.isEmpty(tids) && !ObjectUtils.isEmpty(classid)) {
            List<Long> list = getExamList().stream().map(Exam::getEid).collect(Collectors.toList());
            Criteria c1 = Criteria.where("tid").in(tids).and("ttype").is(RelationType.FOLDER).and("eid").in(list);
            Criteria c2 = Criteria.where("classid").in(classid).and("ttype").is(RelationType.CLASS).and("tid").in(tids);
            relationCriteria.orOperator(c1, c2);
        } else if (!ObjectUtils.isEmpty(tids)) {
            relationCriteria.and("tid").in(tids).and("ttype").is(RelationType.FOLDER);
        } else if (!ObjectUtils.isEmpty(cwids)) {
            relationCriteria.and("tid").in(cwids).and("ttype").is(RelationType.COURSE);
        }
        relationQuery.addCriteria(relationCriteria);
        relationQuery.fields().include("eid");
        List<Long> eids = mongoTemplate.find(relationQuery, ExamRelation.class).stream().distinct().map(ExamRelation::getEid).collect(Collectors.toList());
        return eids;
    }

    /**
     * 根据eid集合查询exam
     *
     * @param params
     * @param eids
     * @return
     */
    public CheckResult getExamByEids(HMapper params, Set<Long> eids) {
        //根据relation的ID查询exam
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("eid").in(eids);
        Integer status = params.getInteger("status");
        if (status != null) {
            criteria.and("status").is(status);
        }
        //删除标志判断
        Integer dtag = params.getInteger("dtag");
        if (dtag == null) {
            dtag = 0;
        }
        criteria.and("dtag").is(dtag);
        User user = params.getObject("user", User.class);
        criteria.and("crid").is(user.getCrid());
        List<Long> uids = params.arr2List("uids", Long[].class);
        if (!ObjectUtils.isEmpty(uids)) {
            criteria.and("uid").in(uids);
        }
        String q = params.getString("q");
        if (!StringUtils.isEmpty(q)) {
            criteria.and("esubject").regex(".*?" + q + ".*");
        }
        ExamType etype = params.getObject("etype", ExamType.class);
        if (!ObjectUtils.isEmpty(etype)) {
            criteria.and("etype").is(etype.toString());
        } else {
            criteria.orOperator(criteria.where("etype").is(ExamType.TSMART), criteria.where("etype").is(ExamType.COMMON));
        }
        String estype = params.getString("estype");
        if (!StringUtils.isEmpty(estype)) {
            criteria.and("estype").is(estype);
        }
        query.addCriteria(criteria);
        query.with(new Sort(Sort.Direction.DESC, "eid"));
        long count = mongoTemplate.count(query, Exam.class);
        List<Exam> examList = mongoTemplate.find(query, Exam.class);
        Page<Exam> page = new PageImpl<>(examList, params.parsePage(), count);
        List<Map<String, Object>> maps = getMapForSexamList(examList, user);
//        List<Map<String, Object>> maps = examList.stream().collect(ArrayList<Map<String, Object>>::new, (list, exam) -> list.add(getExamInfo(exam)), ArrayList::addAll);
        CheckResult checkResult = CheckResult.newInstance();
        checkResult.addErrData("examList", maps);
        checkResult.addErrData("pageInfo", HMapper.pageRet(page));
        return checkResult;
    }

    /**
     * 查询所有答题信息的eid
     * 无答题记录的和status=0的视为未做
     * 有答题和status=1的
     *
     * @param eids
     * @param uid
     * @return
     */
    public Set<Long> getExamByCondition(List<Long> eids, long uid, String action) {
        //学生没有做的作业
        Set<Long> fordo = new HashSet<>();
        //学生已做作业
        Set<Long> hasdo = new HashSet<>();
        for (Long eid : eids) {
            Exam exam = mongoTemplate.findOne(new Query(new Criteria().and("dtag").is(0).and("status").is(1).and("eid").is(eid)), Exam.class);
            if (!ObjectUtils.isEmpty(exam)) {
                if (exam.getEtype() == ExamType.SSMART || exam.getEtype() == ExamType.EXERCISE) {
                    continue;
                }
                if(exam.getEtype() ==ExamType.TSMART){
                    exam = getSmartExam(exam).get(0);
                }
                Query aquery = new Query(new Criteria().and("eid").is(exam.getEid()).and("uid").is(uid).and("dtag").is(0));

                List<UserAnswer> userAnswerList = mongoTemplate.find(aquery, UserAnswer.class);
                //如果无答题记录和答题为草稿（status=0）则视为未做
//                if (exam.getEtype() == ExamType.SSMART) {
//                    eid = exam.getFromeid();
//                }
                if (userAnswerList.size() == 0) {
                    fordo.add(eid);
                } else {
                    if (exam.getStatus() == 1 && userAnswerList.get(0).getStatus() == 1) {
                        hasdo.add(eid);
                    } else {
                        fordo.add(eid);
                    }
                }
            }
        }
        if ("hasdo".equals(action)) {
            return hasdo;
        } else if ("fordo".equals(action)) {
            return fordo;
        } else {
            return null;
        }
    }

    /**
     * 获取在教师布置的智能作业下所有学生生成的智能作业列表
     *
     * @param exam
     * @param params
     * @return
     */
    public Page<Exam> getAllSmartExamList(Exam exam, HMapper params) {
        //删除标志判断
        Integer dtag = params.getInteger("dtag");
        if (dtag == null) {
            dtag = 0;
        }
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("dtag").is(dtag);
        criteria.and("eorder").is(0);
        criteria.and("fromeid").is(exam.getEid());
        query.addCriteria(criteria);
        long count = mongoTemplate.count(query, Exam.class);
        List<Exam> examList = mongoTemplate.find(query.with(params.parsePage()), Exam.class);
        PageImpl<Exam> page = new PageImpl<>(examList, params.parsePage(), count);
        return page;
    }

    /**
     * 根据老师智能作业查出所有学生智能作业
     *
     * @param exam
     * @return
     */
    public List<Exam> getSmartExam(Exam exam) {
        Criteria criteria = new Criteria();
        criteria.and("dtag").is(exam.getDtag());
        criteria.and("fromeid").is(exam.getEid());
        criteria.and("status").is(exam.getStatus());
        criteria.and("etype").is(ExamType.SSMART);
        return mongoTemplate.find(new Query(criteria), Exam.class);
    }

    /**
     * 根据老师智能作业查出所有学生巩固练习
     *
     * @param exam
     * @return
     */
    public List<Exam> getExerciseExam(Exam exam) {
        Criteria criteria = new Criteria();
        criteria.and("dtag").is(exam.getDtag());
        criteria.and("fromeid").is(exam.getEid());
        criteria.and("status").is(exam.getStatus());
        criteria.and("etype").is(ExamType.EXERCISE);
        return mongoTemplate.find(new Query(criteria), Exam.class);
    }

    /**
     * 获取作业试题类型和数量
     *
     * @param eid
     * @return
     */


    public List<Question> getQuestionListByEid(long eid) {
        return mongoTemplate.find(new Query(new Criteria().and("eid").is(eid)), Question.class);
    }

    public List<Question> getQuestionListByfromeid(long eid) {
        return mongoTemplate.find(new Query(new Criteria().and("fromeid").is(eid)), Question.class);
    }

    public List<ExamRelation> getExamRelationList(long eid) {
        return mongoTemplate.find(new Query(new Criteria().and("eid").is(eid)), ExamRelation.class);

    }

    public List<Exam> getExamList() {
        return mongoTemplate.find(new Query(new Criteria().and("isclass").is(0).and("dtag").is(0).and("status").is(1)), Exam.class);
    }

    /**
     * 获取学生所有作业总数
     *
     * @param hMapper
     * @return
     */
    public int getExamListForstu(HMapper hMapper) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        List<Long> tids = hMapper.arr2List("tids", Long[].class);
        List<Long> classid = hMapper.arr2List("classid", Long[].class);
        if (!ObjectUtils.isEmpty(tids) && !ObjectUtils.isEmpty(classid)) {
            List<Long> list = getExamList().stream().map(Exam::getEid).collect(Collectors.toList());
            Criteria c1 = Criteria.where("tid").in(tids).and("ttype").is(RelationType.FOLDER).and("eid").in(list);
            Criteria c2 = Criteria.where("classid").in(classid).and("ttype").is(RelationType.CLASS).and("tid").in(tids);
            criteria.orOperator(c1, c2);
        } else if (!ObjectUtils.isEmpty(tids)) {
            criteria.and("tid").in(tids).and("ttype").is(RelationType.FOLDER);
        }
        query.addCriteria(criteria);
        query.fields().include("eid");
        List<ExamRelation> examRelations = mongoTemplate.find(query, ExamRelation.class);
        //删除标志判断
        Integer dtag = hMapper.getInteger("dtag");
        if (dtag == null) {
            dtag = 0;
        }
        query = new Query();
        criteria = new Criteria();
        criteria.and("dtag").is(dtag);

        Integer status = hMapper.getInteger("status");
        if (status != null) {
            criteria.and("status").is(status);
        }
        User user = hMapper.getObject("user", User.class);
        criteria.and("crid").is(user.getCrid());
        criteria.and("etype").in(Arrays.asList(ExamType.COMMON, ExamType.TSMART));
        criteria.and("eid").in(examRelations.stream().map(ExamRelation::getEid).collect(Collectors.toList()));
        query.addCriteria(criteria);
        query.fields().include("eid");
        List<Exam> examList = mongoTemplate.find(query, Exam.class);
        String action = hMapper.getString("action");
        if (StringUtils.isEmpty(action)) {
            action = "fordo";
        }
        //学生uid
        Set<Long> eids = getExamByCondition(examList.stream().map(Exam::getEid).collect(Collectors.toList()), user.getUid(), action);
        return eids.size();
    }

    /**
     * 获取教师作业列表
     *
     * @param hMap
     * @return
     */
    public Page<Exam> getExamListForTeacher(HMapper hMap) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        String ttype = hMap.getString("ttype");
        long tid = hMap.getLongValue("tid");

        if (!StringUtils.isEmpty(ttype)) {
            criteria.and("ttype").is(ttype);
        }
        criteria.and("tid").is(tid);

        String q = hMap.getString("q");
        if (!StringUtils.isEmpty(q)) {
            criteria.and("esubject").regex(".*?" + q + ".*");
        }
        query.addCriteria(criteria);
        query.fields().include("eid");
        List<ExamRelation> examRelationList = mongoTemplate.find(query, ExamRelation.class);
        query = new Query();
        criteria = new Criteria();
        criteria.and("eid").in(examRelationList.stream().map(ExamRelation::getEid));
        long uid = hMap.getLongValue("uid");
        Integer dtag = hMap.getInteger("dtag");
        if (dtag == null) {
            dtag = 0;
        }
        criteria.and("dtag").is(dtag);
        if (uid != 0L) {
            criteria.and("uid").is(uid);
        }
        long crid = hMap.getLongValue("crid");
        if (crid != 0L) {
            criteria.and("crid").is(crid);
        }
        String etype = hMap.getString("etype");
        if (!StringUtils.isEmpty(etype)) {
            criteria.and("crid").is(crid);
        }
        String estype = hMap.getString("estype");
        if (!StringUtils.isEmpty(estype)) {
            criteria.and("estype").is(estype);
        }
        Integer status = hMap.getInteger("status");
        if (status != null) {
            criteria.and("status").is(status);
        }
        List<ExamType> etypes = Arrays.asList(ExamType.TSMART, ExamType.COMMON);
        criteria.and("etype").in(etypes);
        query.addCriteria(criteria);
        long count = mongoTemplate.count(query, Exam.class);
        List<Exam> examList = mongoTemplate.find(query.with(hMap.parsePage()), Exam.class);
        Page<Exam> page = new PageImpl<>(examList, hMap.parsePage(), count);
        return page;

    }

    public Page<Exam> getExamPageByEid(HMapper hMapper) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        Integer dtag = hMapper.getInteger("dtag");
        criteria.and("dtag").is(dtag);

        Integer status = hMapper.getInteger("status");
        if (status != null) {
            criteria.and("status").is(status);
        }
        Long crid = hMapper.getLong("crid");
        if (crid != null) {
            criteria.and("crid").is(crid);
        }
        Long eid = hMapper.getLongValue("eid");
        if (!ObjectUtils.isEmpty(eid)) {
            criteria.and("eid").gt(eid);
        }
        criteria.and("fromeid").is(null);
        Long[] crids = hMapper.getObject("crids", Long[].class);
        if (!ObjectUtils.isEmpty(crids)) {
            criteria.and("crid").in(Arrays.asList(crids));
        }
        String esubject = hMapper.getString("q");
        if (!StringUtils.isEmpty(esubject)) {
            criteria.and("esubject").regex(".*?" + esubject + ".*");
        }
        Long[] eids = hMapper.getObject("eids", Long[].class);
        if (!ObjectUtils.isEmpty(eids)) {
            criteria.and("eid").in(Arrays.asList(eids));
        }
        query.addCriteria(criteria);
        long count = mongoTemplate.count(query, Exam.class);
        List<Exam> examList = mongoTemplate.find(query.with(hMapper.parsePage()), Exam.class);
        return new PageImpl<>(examList, hMapper.parsePage(), count);
    }

    /**
     * 按知识点分析(教师&学生)
     *
     * @param exam
     * @param hMapper
     * @return
     */
    public List<Map<String, Object>> doAnalysis(Exam exam, HMapper hMapper) {
        User user = hMapper.getObject("user", User.class);
        List<Long> uids = hMapper.arr2List("uids", Long[].class);
        List<Question> questions = getQuestionListByEid(exam.getEid());
        long answercount = 0;//答题人数
        List<UserAnswer> userAnswerList;
        if (exam.getEtype() == ExamType.SSMART) {
            userAnswerList = userAnswerService.getUserAnswerListByEid(exam.getEid());
        } else {
            userAnswerList = userAnswerService.getUserAnswerListByEid(exam.getEid());
        }
        if (!ObjectUtils.isEmpty(uids)) {
            answercount = userAnswerList.stream().filter(userAnswer -> userAnswer.getStatus() == 1 && uids.contains(userAnswer.getUid())).count();
        } else {
            answercount = userAnswerList.stream().filter(userAnswer -> userAnswer.getStatus() == 1).count();
        }
        List<QuestionRelation> questionRelationList = new ArrayList();
        List<Map<String, Object>> maplist = new ArrayList<>();
        for (Question question : questions) {
            List<QuestionRelation> qrList = questionRelationDao.getQuestionRelationListByQid(question.getQid());
            List<QuestionRelation> relationList = qrList.stream().sorted(Comparator.comparing(questionRelation -> questionRelation.getRelationid())).filter(questionRelation -> questionRelation.getTtype() == RelationType.CHAPTER && !StringUtils.isEmpty(questionRelation.getPath())).collect(Collectors.toList());
            if (!ObjectUtils.isEmpty(relationList)) {
                questionRelationList.add(relationList.get(0));
            }
        }
        Map<String, QuestionRelation> map = new HashMap();
        //计算分数，如果知识点相同，需要计算得分，试题总分，将算好的分数放到questionrelation的对象字段中方便便利的取值。
        for (QuestionRelation qr : questionRelationList) {
            if (!map.containsKey(qr.getPath())) {
                map.put(qr.getPath(), qr);
                QuestionRelation questionRelation = map.get(qr.getPath());
                Question question = questionDao.getQuestionByQid(questionRelation.getQid());
                List<AnswerQueDetail> answerQueDetailList = answerQueDetailDao.getAnswerQueDetailListByQid(question.getQid());
                double newstuscore;
                if (!ObjectUtils.isEmpty(uids)) {
                    newstuscore = answerQueDetailList.stream().filter(answerQueDetail -> uids.contains(answerQueDetail.getUid())).mapToDouble(AnswerQueDetail::getTotalscore).sum();
                } else {
                    newstuscore = answerQueDetailList.stream().mapToDouble(AnswerQueDetail::getTotalscore).sum();
                }

                questionRelation.setRemark(String.valueOf(newstuscore));//暂借属性

                int newsquecore = question.getQuescore();
                map.get(qr.getPath()).setDtag(newsquecore);
                //学生
                if (hMapper.getString("action").equals("student")) {
                    double myscore = answerQueDetailList.stream().filter(aqd -> aqd.getUid() == user.getUid()).mapToDouble(AnswerQueDetail::getTotalscore).sum();
                    questionRelation.setExtdata(myscore + "");//学生统计时临时放到data属性中
                }
            } else {
                QuestionRelation relation = map.get(qr.getPath());
                Question question = questionDao.getQuestionByQid(qr.getQid());
                List<AnswerQueDetail> answerQueDetailList = answerQueDetailDao.getAnswerQueDetailListByQid(question.getQid());

                int quescore = relation.getDtag() + question.getQuescore();
                relation.setDtag(quescore);//有重复知识点将每题的quescore加起来暂时放入一个quescore
                double stuscore;
                if (!ObjectUtils.isEmpty(uids)) {
                    stuscore = Double.parseDouble(map.get(qr.getPath()).getRemark()) + answerQueDetailList.stream().filter(answerQueDetail -> uids.contains(answerQueDetail.getUid())).mapToDouble(AnswerQueDetail::getTotalscore).sum();
                } else {
                    stuscore = Double.parseDouble(map.get(qr.getPath()).getRemark()) + answerQueDetailList.stream().mapToDouble(AnswerQueDetail::getTotalscore).sum();
                }
                relation.setRemark(String.valueOf(stuscore));//将学生得分暂时放到remark属性中

                //学生
                if (hMapper.getString("action").equals("student")) {
                    double newmyscore = Double.parseDouble(map.get(qr.getPath()).getExtdata()) + answerQueDetailList.stream().filter(aqd -> aqd.getUid() == user.getUid()).mapToDouble(AnswerQueDetail::getTotalscore).sum();
                    relation.setExtdata(newmyscore + "");
                }
            }
        }
        for (String key : map.keySet()) {
            HashMap<String, Object> rowMap = new HashMap<>();
            //如果为学生
            if (hMapper.getString("action").equals("student")) {
                Double myscore = Double.parseDouble(map.get(key).getExtdata());
                rowMap.put("myscore", myscore);
            }
            int totalquescore = map.get(key).getDtag();
            Double stutotalscore = Double.parseDouble(map.get(key).getRemark());
            Double avgscore;
            if (answercount == 0L) {
                avgscore = 0D;
            } else {
                BigDecimal bd = new BigDecimal(String.valueOf(stutotalscore / answercount));
                avgscore = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            }

            rowMap.put("avgscore", avgscore);
            rowMap.put("quescore", totalquescore);
            rowMap.put("tid", map.get(key).getTid());
            rowMap.put("relationname", map.get(key).getRelationname());
            rowMap.put("path", map.get(key).getPath());
            rowMap.put("answercount", answercount);
            maplist.add(rowMap);
        }
        return maplist;
    }

    /**
     * 作业分析(优秀率)
     *
     * @param exam
     * @param params
     * @return
     */
    public List<Map<String, Object>> efenxiRank(Exam exam, HMapper params) {
        List<Long> uids = params.arr2List("uids", Long[].class);
        int examtotalscore = exam.getExamtotalscore();
        List<Map<String, Object>> ret = new ArrayList<>();
        List<Float> scoreList = new ArrayList<>();
        Map<String, Object> map = new HashMap();
        Set<UserAnswer> set;
        List<UserAnswer> userAnswerList;
        if (exam.getEtype() == ExamType.SSMART) {
            userAnswerList = userAnswerService.getUserAnswerListByEid(exam.getEid());
        } else {
            userAnswerList = userAnswerService.getUserAnswerListByEid(exam.getEid());
        }
        if (!ObjectUtils.isEmpty(uids)) {
            set = userAnswerList.stream().filter(userAnswer -> userAnswer.getStatus() == 1 && uids.contains(userAnswer.getUid())).collect(Collectors.toSet());
        } else {
            set = userAnswerList.stream().filter(userAnswer -> userAnswer.getStatus() == 1).collect(Collectors.toSet());
        }
        for (UserAnswer userAnswer : set) {
            float score = (float) userAnswer.getAnstotalscore() * 100 / (float) examtotalscore;
            scoreList.add(score);
        }
        Long excellent = scoreList.stream().filter(score -> score >= 90 && score <= 100).count();
        Long good = scoreList.stream().filter(score -> score >= 80 && score < 90).count();
        Long pass = scoreList.stream().filter(score -> score >= 60 && score < 80).count();
        Long fail = scoreList.stream().filter(score -> score >= 0 && score < 60).count();
        map.put("excellent", excellent);
        map.put("good", good);
        map.put("pass", pass);
        map.put("fail", fail);
        map.put("totalcount", set.stream().filter(userAnswer -> userAnswer.getStatus() == 1).count());
        ret.add(map);
        return ret;
    }

    /**
     * 按照题型分析
     *
     * @param exam
     * @param hMapper
     * @return
     */
    public List<Map<String, Object>> efenxiQtype(Exam exam, HMapper hMapper) {
        Set<QueType> set = new HashSet();
        List<Map<String, Object>> list = new ArrayList();
        User user = hMapper.getObject("user", User.class);
        String action = hMapper.getString("action");
        List<Long> uids = hMapper.arr2List("uids", Long[].class);
        List<Question> questionList = getQuestionListByEid(exam.getEid());
        questionList.forEach(question -> set.add(question.getQueType()));
        for (QueType quetype : set) {
            if (quetype == QueType.G || quetype == QueType.Z) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            List<Question> questions = questionList.stream().filter(question -> question.getQueType() == quetype).collect(Collectors.toList());
            double totalscore = 0;
            double myscore = 0;
            int totalQuescore = questions.stream().mapToInt(Question::getQuescore).sum();
            for (Question question : questions) {
                List<AnswerQueDetail> answerQueDetailList = answerQueDetailDao.getAnswerQueDetailListByQid(question.getQid());
                if (!ObjectUtils.isEmpty(uids)) {
                    answerQueDetailList = answerQueDetailList.stream().filter(answerQueDetail -> uids.contains(answerQueDetail.getUid())).collect(Collectors.toList());
                }
                totalscore += answerQueDetailList.stream().mapToDouble(AnswerQueDetail::getTotalscore).sum();
                //如果角色是学生需要获取“我的分数”
                if (action.equals("student")) {
                    double mynewscore = answerQueDetailList.stream().filter(aqd -> question.getQueType() == quetype && aqd.getUid() == user.getUid()).mapToDouble(AnswerQueDetail::getTotalscore).sum();
                    BigDecimal bigDecimal = new BigDecimal(String.valueOf(mynewscore)).add(new BigDecimal(myscore));
                    myscore = bigDecimal.doubleValue();
                }
            }
            double averageScore = 0;
            List<UserAnswer> userAnswerList = userAnswerService.getUserAnswerListByEid(exam.getEid());
            if (!ObjectUtils.isEmpty(uids)) {
                userAnswerList = userAnswerList.stream().filter(userAnswer -> userAnswer.getStatus() == 1 && uids.contains(userAnswer.getUid())).collect(Collectors.toList());
            }
            if (userAnswerList.size() == 0) {
                averageScore = 0;
            } else {
                averageScore = totalscore / (double) userAnswerList.size();
                BigDecimal bd = new BigDecimal(String.valueOf(averageScore));
                averageScore = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
            map.put("myscore", myscore);
            map.put("queType", quetype);
            map.put("allScorce", totalQuescore);
            map.put("averageScore", averageScore);
            map.put("scoreRate", averageScore / totalQuescore);
            list.add(map);
        }
        return list;
    }

    public List<Map<String, Object>> getMapForSexamList(List<Exam> list, User user) {
        List<Map<String, Object>> examList = list.stream().collect(ArrayList::new, (ArrayList<Map<String, Object>> container, Exam exam) -> {
            Map<String, Object> tmpMap = new HashMap<>();
            Map<String, Object> examMap = new HashMap<>();
            examMap.put("eid", exam.getEid());
            examMap.put("uid", exam.getUid());
            examMap.put("esubject", exam.getEsubject());
            examMap.put("dateline", exam.getDateline());
            examMap.put("examtotalscore", exam.getExamtotalscore());
            examMap.put("limittime", exam.getLimittime());
            examMap.put("etype", exam.getEtype());
            examMap.put("estype", exam.getEstype());
            examMap.put("examstarttime", exam.getExamstarttime());
            examMap.put("examendtime", exam.getExamendtime());
            examMap.put("ansstarttime", exam.getAnsstarttime());
            examMap.put("ansendtime", exam.getAnsendtime());
            List<BaseRelationForResp> relationSet = HMapper.parseData2List(exam.getData(), "relationSet", BaseRelationForResp[].class);
            examMap.put("relationSet", relationSet);
            examMap.put("answercount", userAnswerService.getExamAnswerCountForTeacher(exam));
            examMap.put("stucancorrect", exam.getStucancorrect());
            examMap.put("canreexam", exam.getCanreexam());
            examMap.put("nowtime", CUtil.getUnixTimestamp());
            UserAnswer userAnswer = null;
            if (exam.getEtype() == ExamType.TSMART) {//智能作业获取第一份
                List<Exam> exams = getSmartExam(exam);
                if (exams.size() > 0)
                    userAnswer = userAnswerService.findUserAnswerByUidAndEid(user.getUid(), exams.get(0).getEid());
            } else {
                userAnswer = userAnswerService.findUserAnswerByUidAndEid(user.getUid(), exam.getEid());
            }
            tmpMap.put("exam", examMap);
            tmpMap.put("userAnswer", userAnswerService.simpleInfo(userAnswer));
            container.add(tmpMap);
        }, ArrayList::addAll);
        return examList;
    }

    public long getExamAnswerCountForTeacher(Exam exam) {
        return userAnswerService.getExamAnswerCountForTeacher(exam);
    }

}
