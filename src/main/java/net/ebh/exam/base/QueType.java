package net.ebh.exam.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2016/5/23.
 */
public enum QueType {
    A("单选题"), B("多选题"), C("填空题"), D("判断题"), E("文字"), F("文本行"), G("插入音频"), H("主观题"), X("答题卡"), XTL("听力题"), XWX("完形填空"), XYD("阅读理解"), XZH("组合题"), Z("标题"), ;
    private String name;

    public String getName() {
        return name;
    }

    QueType(String name) {
        this.name = name;
    }

    public static List<QueType> Xlist() {
        return Arrays.asList(X, XTL, XWX, XYD, XZH);
    }

    public static List<QueType> HGZ() {
        return Arrays.asList(G, H, Z);
    }

    public static void main(String[] args) {
        System.out.println(QueType.HGZ().addAll(QueType.Xlist()));
    }

    public static List<String> addList(List list, List list1) {
        ArrayList<String> allList = new ArrayList<>();
        allList.addAll(list);
        allList.addAll(list1);
        return allList;
    }
}
