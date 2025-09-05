package io.notfound.counsel_back.conversation.controller;

import io.notfound.counsel_back.conversation.dto.ChatRequest;
import io.notfound.counsel_back.conversation.dto.ChatResponse;
import io.notfound.counsel_back.conversation.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ChatResponse chat(@RequestBody ChatRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        ChatResponse response = chatService.getChatCompletion(request, userEmail);

        return response;
    }
}
