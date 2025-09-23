package io.notfound.counsel_back.board.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class FilteringService {

    // 필터링할 단어 목록 (예시)
    private static final List<String> BAD_WORDS = Arrays.asList("바보", "멍청이", "개새끼", "시발");

    /**
     * 입력된 텍스트에서 부적절한 단어를 필터링합니다.
     *
     * @param text 원본 텍스트
     * @return 필터링된 텍스트
     */
    public String filterText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String filteredText = text;
        for (String badWord : BAD_WORDS) {
            // 정규 표현식을 사용해 대소문자 구분 없이 모든 '나쁜 단어'를 찾아서 대체
            filteredText = filteredText.replaceAll("(?i)" + badWord, "***");
        }
        return filteredText;
    }
}