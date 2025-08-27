package io.notfound.counsel_back.conversation.controller;

import io.notfound.counsel_back.conversation.dto.ChatRequest;
import io.notfound.counsel_back.conversation.dto.ChatResponse;
import io.notfound.counsel_back.conversation.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        // GeminiService를 호출하여 AI의 답변을 받아옴
        String aiMessage = chatService.getChatCompletion(request.getMessage());
        return new ChatResponse(aiMessage);
    }
}
