package io.notfound.counsel_back.conversation.service;

import io.notfound.counsel_back.conversation.dto.ChatRequest;
import io.notfound.counsel_back.conversation.entity.Conversation;
import io.notfound.counsel_back.conversation.repository.ConversationRepository;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final OpenAiChatModel openAiChatModel;
    private final ChatMemoryRepository chatMemoryRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    public Flux<String> generateStream(ChatRequest request, String email) {

        Long id = request.getConversationId();
        String message = request.getMessage();
        Conversation conversation = null;

        if (id == null) {
             conversation = createNewConversation(email);
        } else {
            conversation = findExistingConversation(id);
        }

        String conversationId = conversation.getId().toString();

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        chatMemory.add(conversationId, new UserMessage(message));

        // 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-nano")
                .temperature(0.7)
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(chatMemory.get(conversationId), options);

        // 응답 메시지를 저장할 임시 버퍼
        StringBuilder responseBuffer = new StringBuilder();

        // 요청 및 응답
        return openAiChatModel.stream(prompt)
                .mapNotNull(response -> {
                    String token = response.getResult().getOutput().getText();
                    responseBuffer.append(token);
                    return token;
                })
                .doOnComplete(() -> {
                    chatMemory.add(conversationId, new AssistantMessage(responseBuffer.toString()));
                    chatMemoryRepository.saveAll(conversationId, chatMemory.get(conversationId));
                });
    }

    private Conversation createNewConversation(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email)
                );
        Conversation conversation = new Conversation(user);
        return conversationRepository.save(conversation);
    }

    private Conversation findExistingConversation(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("대화를 찾을 수 없습니다: " + conversationId)
                );
    }


}
