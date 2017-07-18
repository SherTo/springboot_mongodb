package net.ebh.exam.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.ebh.exam.bean.KuQuestionRelation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by zkq on 2016/5/20.
 * 请求参数映射
 */
public class HMapper extends JSONObject {
    public <T> List<T> getList(String name, Class<T> clazz) {
        return parseArray(toJSONString(get(name)), clazz);
    }

    /**
     * 普通对象转json对象
     *
     * @param object
     * @return
     */
    public static JSONObject obj2JsonObj(Object object) {
        return JSON.parseObject(JSON.toJSONString(object));
    }

    /**
     * 从当前对象获取指定数组并转化成集合返回
     *
     * @param name
     * @param clazz
     * @param <T>
     * @return 非空集合
     */
    public <T> List<T> arr2List(String name, Class<T[]> clazz) {
        return toList(getObject(name, clazz));
    }

    /**
     * 从当前对象解析指定类型的对象
     *
     * @param data
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T parseData(String data, String name, Class<T> clazz) {
        try {
            return JSONObject.parseObject(data).getObject(name, clazz);
        } catch (Exception e) {
        }
        return null;
    }

    public static <T> T parseData(byte[] data, String name, Class<T> clazz) {
        return parseData(new String(data), name, clazz);
    }

    /**
     * 解析静态json数据中的指定名字的对象到集合
     *
     * @param data
     * @param name
     * @param clazz
     * @param <T>
     * @return 非空集合
     */
    public static <T> List<T> parseData2List(String data, String name, Class<T[]> clazz) {
        try {
            return toList(JSONObject.parseObject(data).getObject(name, clazz));
        } catch (Exception e) {
        }
        return new ArrayList<>();
    }

    public static <T> List<T> parseData2List(byte[] data, String name, Class<T[]> clazz) {
        try {
            return parseData2List(new String(data), name, clazz);
        } catch (Exception e) {
        }
        return new ArrayList<>();
    }

    /**
     * 解析静态json数据中的指定名字的对象到数组
     *
     * @param data
     * @param name
     * @param clazz
     * @param <T>
     * @return 数组
     */
    public static <T> T[] parseData2Array(String data, String name, Class<T[]> clazz) {
        try {
            return JSONObject.parseObject(data).getObject(name, clazz);
        } catch (Exception e) {
            return null;
        }
    }


    public static <T> T[] parseData2Array(byte[] data, String name, Class<T[]> clazz) {
        try {
            return parseData2Array(new String(data), name, clazz);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 解析分页参数
     *
     * @return
     */
    public Pageable parsePage() {
        int page = getIntValue("page");
        int size = getIntValue("size");
        if (page < 1) {
            page = 1;
        }
        if (size == 0) {
            size = 20;
        }

        String order = getString("order");
        if (!StringUtils.isEmpty(order)) {
            List<Sort.Order> orderList = new ArrayList<>();
            String[] orderarr = order.split(",");
            for (String orderstr : orderarr) {
                String[] split = orderstr.split("\\s");
                if (split.length == 1) {
                    orderList.add(new Sort.Order(Sort.Direction.valueOf("ASC"), StringUtils.trimWhitespace(split[0])));
                } else {
                    orderList.add(new Sort.Order(Sort.Direction.valueOf(StringUtils.trimWhitespace(split[1]).toUpperCase()), StringUtils.trimWhitespace(split[0])));
                }
            }
            return new PageRequest(page - 1, size, new Sort(orderList));
        } else {
            return new PageRequest(page - 1, size);
        }

    }

    public static <T> Map<String, Object> pageRet(Page<T> page) {
        Map<String, Object> pageRet = new HashMap<>();
        pageRet.put("totalElement", page.getTotalElements());
        pageRet.put("totalPages", page.getTotalPages());
        pageRet.put("size", page.getSize());
        pageRet.put("number", page.getNumber() + 1);
        return pageRet;
    }

    /**
     * 私有方法，数组转变长集合
     *
     * @param arr
     * @param <T>
     * @return
     */
    private static <T> List<T> toList(T[] arr) {
        if (arr == null) {
            return new ArrayList<>();
        } else {
            List<T> list = new ArrayList<>();
            list.addAll(Arrays.asList(arr));
            return list;
        }
    }

    /**
     * 获取字符串
     *
     * @param name
     * @param iftrim
     * @return
     */
    public String getString(String name, Boolean iftrim) {
        if (iftrim) {
            return StringUtils.trimWhitespace(getString(name));
        } else {
            return getString(name);
        }
    }


}
