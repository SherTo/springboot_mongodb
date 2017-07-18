package net.ebh.exam.util;

/**
 * Created by Admin on 2016/10/21.
 */
public class ErrorCode {
    public static final String PARAMS_IS_NULL               = "000001";//参数为空
    public static final String EXAM_ISNOT_EXIST             = "100000";//作业不存在
    public static final String EXAM_ISNOT_SMART            = "100001";//作业不是智能作业
    public static final String EXAM_IS_DRAFT                = "100002";//作业是草稿
    public static final String EXAM_TIME_NOT_TO             = "100003";//作业时间未到
    public static final String EXAM_TIME_IS_UP              = "100004";//作业时间结束
    public static final String EXAM_UID_ISNULL              = "100005";//作业的uid为空
    public static final String EXAM_EID_ISNULL              = "100006";//作业的eid为空
    public static final String EXAM_ISNOT_ANSWER            = "100007";//作业没有答题
    public static final String EXAM_DATA_ISNULL             = "100008";//作业data为空
    public static final String EXAM_IS_EXIST                = "100009";//作业已经存在
    public static final String EXAM_ESTYPE_ISNULL           = "100010";//ESTYPE为空
    public static final String EXAM_ETYPE_ISNULL            = "100011";//ETYPE为空
    public static final String EXAM_SCORE_IS_ZERO           = "100012";//作业分数为0
    public static final String RELATIONSET_IS_NULL          = "100013";//relationSet为空
    public static final String CONDITIONLIST_IS_NULL        = "200000";//没有conditionlist
    public static final String CRID_IS_NULL                 = "200001";//crid不存在
    public static final String BLANK_ISNOT_EXIST            = "200002";//blank空不存在
    public static final String BLANK_IS_NULL                = "200003";//blank空为空
    public static final String BLANK_RIGHT_OR_NOT           = "200004";//没有设置答题点是否是正确答案
    public static final String BLANKSUBJECT_IS_NULL         = "200005";//没有设置答题点是否是正确答案
    public static final String CRID_ISNOT_SAME              = "200006";//crid不相同
    public static final String QUESTION_ISNOT_EXIST         = "300000";//作业question不存在
    public static final String QUESTION_QSUBJECT_ISNULL     = "300001";//问题question标题不存在
    public static final String QUESTION_SCORE_ISNULL        = "300002";//问题分数为空
    public static final String QUESTION_DATA_ISNULL         = "300003";//问题data数据为空
    public static final String QUESTION_QTYPE_ISNULL        = "300004";//问题Qtype为空
    public static final String QUESTION_UID_ISNULL          = "300005";//问题uid为空
    public static final String QUESTION_UID_NOTSAME         = "300006";//问题uid不相同
    public static final String QUESTION_NOT_CORRECTED       = "300007";//试题没有完全批阅完毕
    public static final String QUESTION_CHAPTER_ISNULL      = "300008";//关联的知识点为空（关联的类型为非知识点）
    public static final String AQD_ISNOT_EXIST              = "400000";//答题明细不存在
    public static final String AQD_ISTNOT_BELONG            = "400001";//答题明细不属于当前用户
    public static final String USERANSWER_ISNOT_EXIST       = "500000";//没有答题记录
    public static final String ANSWERER_ISNOT_MATCH         = "500001";//答题和批改的不匹配
    public static final String USERANSWER_UID_ISNULL        = "500002";//答题uid为空
    public static final String USERANSWER_DATA_ISNULL       = "500003";//答题data数据为空
    public static final String ANSWER_IS_EXIST              = "500004";//答案已经上传
    public static final String ANSWER_ISNOT_BELONG          = "500005";//答题用户和当前用户不匹配
    public static final String ANSWERMAP_CANNOT_PARSING     = "500006";//答题无法解析
    public static final String USERANSWER_UID_NOTSAME       = "500007";//答题的uid不匹配
    public static final String USER_VALIDATION_FAILS        = "600000";//用户认证失败
    public static final String NOT_AOLLOW_EDIT              = "600001";//不允许修改他人作业
    public static final String EXAM_IS_ANSWERED             = "600002";//作业已经答题，没法修改
    public static final String NOT_FIND_KUQUESTION          = "700000";//没有kuquestion
    public static final String KUQUESTION_ISNOT_BELONG      = "700001";//kuquestion不属于当前用户
    public static final String KUQUESTION_UID_IS_NULL       = "700002";//kuquestion UId为空
    public static final String KUQUESTION_ISNOT_AOLLOW_EDIT = "700003";//kuquestion不能编辑status已经为1
    public static final String KUQUESTION_QTYPE_ISNULL      = "700004";//kuquestion类型为空
    public static final String KUQUESTION_QSUBJECT_ISNULL   = "700005";//kuquestion题干为空
    public static final String KUQUESTION_IS_EXIST          = "700006";//kuquestion已经存在
    public static final String KUQUESTION_ISNOT_EXIST       = "700007";//kuquestion不存在
    public static final String NOT_FIND_MD5CODE             = "800000";//没有找到md5
    public static final String NOT_FIND_CORRECTLIST         = "800001";//答题用户和当前用户不匹配
    public static final String ABDLANKS_NOT_MATCH           = "800002";//提交的和答题的空不匹配
    public static final String SCORE_ISNOT_ABOVE_TOTALSCORE = "800003";//总分不能高于试卷总分
    public static final String SCORE_ISNOT_ZORE             = "800004";//试题分数不能为0
    public static final String ERRORBOOK_ISNOT_EXIST        = "900000";//错题不存在
    public static final String ERRORBOOK_UID_ISNULL         = "900001";//检测不到用户信息
}
