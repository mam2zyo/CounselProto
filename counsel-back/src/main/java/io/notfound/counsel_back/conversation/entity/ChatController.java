package io.notfound.counsel_back.conversation.entity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    // 1. GeminiService를 주입받기 위한 변수
    private final GeminiService geminiService;

    // 2. 생성자를 통해 Spring이 GeminiService를 자동으로 주입
    public ChatController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    // 3. /api/chat 경로로 POST 요청이 오면 이 메소드가 실행됨
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        // GeminiService를 호출하여 AI의 답변을 받아옴
        String answer = geminiService.getChatCompletion(request.getMessage());
        return new ChatResponse(answer);
    }

    // --- 요청(Request) 데이터를 담기 위한 내부 클래스 ---
    public static class ChatRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    // --- 응답(Response) 데이터를 담기 위한 내부 클래스 ---
    public static class ChatResponse {
        private String response;

        public ChatResponse(String response) {
            this.response = response;
        }

        public String getResponse() {
            return response;
        }
    }

} 