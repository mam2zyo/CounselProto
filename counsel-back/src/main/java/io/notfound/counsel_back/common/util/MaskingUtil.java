package io.notfound.counsel_back.common.util;

public class MaskingUtil {

    // 이름 마스킹 (예: 홍길동 -> 홍*동)
    public static String maskName(String name) {
        if (name == null || name.length() < 2) return name;

        if (name.length() == 2) {
            return name.charAt(0) + "*"; // 길이 2인 경우 (예: 철수 -> 철*)
        }

        // 3글자 이상인 경우 (홍길동 -> 홍*동, 김철수박 -> 김**박)
        StringBuilder sb = new StringBuilder();
        sb.append(name.charAt(0));
        for (int i = 1; i < name.length() - 1; i++) {
            sb.append("*");
        }
        sb.append(name.charAt(name.length() - 1));
        return sb.toString();
    }
}
