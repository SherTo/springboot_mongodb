package net.ebh.exam.util;

import java.util.HashMap;
import java.util.Map;

public class ChangeChoice {
    public static String choiceToString(String choiceStr) {
        StringBuffer buffer = new StringBuffer();
        char[] chars = choiceStr.toCharArray();
        for (int i = 1; i < chars.length + 1; i++) {
            if (Integer.parseInt(String.valueOf(chars[i - 1])) == 1) {
                buffer.append(getValue(i));
            }
        }
        return buffer.toString();
    }

    public static String getValue(int key) {
        Map<Integer, String> map = new HashMap<>();
        int[] ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        String[] strs = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
        for (int i = 1; i < ints.length + 1; i++) {
            for (int j = 1; j < strs.length + 1; j++) {
                if (i == j) {
                    map.put(i, strs[i - 1]);
                }
            }
        }
        return map.get(key);
    }

    public static void main(String[] args) {
        System.out.println(ChangeChoice.choiceToString("0100"));
    }
}