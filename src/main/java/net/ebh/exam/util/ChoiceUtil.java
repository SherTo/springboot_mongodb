package net.ebh.exam.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class ChoiceUtil {
    //存放26个字母序列
    private static List<String> sList;
    //abc 对应的数值
    private static Map<String, Long> snMap;
    //数值对应的 abc
    private static Map<Long, String> nsMap;
    //索引对应的数值
    private static Map<Integer, Long> idxNMap;

    static {
        sList = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
        nsMap = new HashMap<>();
        snMap = new HashMap<>();
        idxNMap = new HashMap<>();
        for (int i = 0, size = sList.size(); i < size; i++) {
            nsMap.put(new Integer(1 << i).longValue(), sList.get(i));
            snMap.put(sList.get(i), new Integer(1 << i).longValue());
            idxNMap.put(i, new Integer(1 << i).longValue());
        }
    }

    /**
     * 根据索引获取对应的A,B,C选项
     */
    public static String getABCByIdx(int idx) {
        return sList.get(idx);
    }

    /**
     * 根据索引获取对应的选项数值 0=》1 ，1=》2 ，2=》4
     */
    public static Long getNByIdx(int idx) {
        return idxNMap.get(idx);
    }

    /**
     * 根据A,B,C获取数值   A=》1 ，B=》2 ，C=》4
     **/
    public static Long getNByABC(String s) {
        return snMap.get(s);
    }

    /**
     * 根据数值获取ABC   1=》A ，2=》B ，4=》C
     **/
    public static String getABCByN(Long n) {
        return nsMap.get(n);
    }

    public static void main(String[] args) {

        System.out.println(getABCByN(1L));
    }
}
