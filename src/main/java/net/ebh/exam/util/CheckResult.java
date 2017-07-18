package net.ebh.exam.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zkq on 2016/5/19.
 * 检测结果集
 */
@Data
public class CheckResult {

    private String errCode; //错误码
    private boolean status; //错误状态
    private String errMsg; //错误描述信息

    @JsonProperty("datas")
    private Map<String, Object> errData; //错误结果集

    {
        errCode = "0";
        errMsg = "";
        status = true;
        errData = new HashMap<>();
    }

    public CheckResult addErrData(String key, Object msgBody) {
        errData.put(key, msgBody);
        return this;
    }

    public static CheckResult newInstance() {
        return new CheckResult();
    }

    public static CheckResult newInstance(String errCode, String errMsg) {
        CheckResult checkResult = new CheckResult();
        checkResult.setErrCode(errCode);
        checkResult.setErrMsg(errMsg);
        return checkResult;
    }
}
