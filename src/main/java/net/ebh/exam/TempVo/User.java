package net.ebh.exam.TempVo;

/**
 * Created by xh on 2017/2/8.
 */

import com.alibaba.fastjson.JSON;
import net.ebh.exam.base.ExamType;
import net.ebh.exam.util.CUtil;
import net.ebh.exam.util.HMapper;
import org.springframework.data.annotation.Id;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public class User {
    private long uid;
    private long crid;
    private long t;

    public Boolean isValid() {
        return isValid(3600);
    }


    public User() {
    }


    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getCrid() {
        return crid;
    }

    public void setCrid(long crid) {
        this.crid = crid;
    }

    public long getT() {
        return t;
    }

    public void setT(long t) {
        this.t = t;
    }

    public Boolean isValid(long expir) {
        return CUtil.getUnixTimestamp() < expir + t;
    }

    /**
     * 根据key获取用户信息
     */
    public static User getUser(String k) {
        if (ObjectUtils.isEmpty(k)) {
            return null;
        }
        String decodeStr = CUtil.authcodeDecode(k);
        if (StringUtils.isEmpty(decodeStr)) {
            return null;
        }
        return JSON.parseObject(decodeStr, User.class);
    }

    /**
     * 根据请求参数获和指定的key获取用户信息
     */
    public static User getUser(HMapper params, String fieldName) {
        return getUser(params.getString(fieldName));
    }

    /**
     * 根据请求参数获取用户信息
     */
    public static User getUser(HMapper params) {
        return getUser(params.getString("k"));
    }

    public static void main(String[] args) {
        String data = "{\\\"blankList\\\":[{\\\"bsubject\\\":\\\"20\\\",\\\"isanswer\\\":\\\"1\\\"},{\\\"bsubject\\\":\\\"40\\\",\\\"isanswer\\\":\\\"1\\\"}],\\\"relationSet\\\":[{\\\"tid\\\":\\\"1272\\\",\\\"ttype\\\":\\\"FOLDER\\\",\\\"relationname\\\":\\\"高一年级数学\\\"}";
        System.out.println(data);
    }

}

