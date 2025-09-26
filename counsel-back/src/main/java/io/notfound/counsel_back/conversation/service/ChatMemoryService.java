package io.notfound.counsel_back.conversation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMemoryService {

    private final ChatMemoryRepository chatMemoryRepository;
    private static final int MAX_CHAT_MEMORY_MESSAGES = 20;

    public ChatMemory getChatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(MAX_CHAT_MEMORY_MESSAGES)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }

    public void addUserMessage(ChatMemory chatMemory, String conversationId, String message) {
        chatMemory.add(conversationId, new UserMessage(message));
    }

    public void addAiMessage(ChatMemory chatMemory, String conversationId, String message) {
        chatMemory.add(conversationId, new AssistantMessage(message));
    }

    public void addSystemMesage(ChatMemory chatMemory, String conversationId, String message) {
        chatMemory.add(conversationId, new SystemMessage(message));
    }
}
