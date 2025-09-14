package io.notfound.counsel_back.conversation.service;

import io.notfound.counsel_back.conversation.dto.ChatRequest;
import io.notfound.counsel_back.conversation.entity.ChatMessage;
import io.notfound.counsel_back.conversation.entity.Conversation;
import io.notfound.counsel_back.conversation.entity.Sender;
import io.notfound.counsel_back.conversation.repository.ChatMessageRepository;
import io.notfound.counsel_back.conversation.repository.ConversationRepository;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final OpenAiChatModel openAiChatModel;
    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;

    @Transactional
    public Flux<String> generate(ChatRequest request, String email) {

        Long conversationIdLong = request.getConversationId();
        String messageText = request.getMessage();

        Conversation conversation =
                conversationRepository.findById(conversationIdLong)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "대화를 찾을 수 없습니다." + conversationIdLong)
                        );

        // request로 받은 message를 통해 ChatMessage 객체 생성 후, conversation 객체에 추가
        // 이후 개별 레파지토리에 저장하여 영속화
        ChatMessage userMessage = new ChatMessage(Sender.USER, messageText, conversation);
        conversation.addChatMessage(userMessage);
        chatMessageRepository.save(userMessage);
        conversationRepository.save(conversation);

        String conversationId = conversationIdLong.toString();

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        chatMemory.add(conversationId, new UserMessage(messageText));

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
                    String fullAiResponse = responseBuffer.toString();

                    // AI 메시지 저장
                    ChatMessage aiMessage = new ChatMessage(Sender.AI, fullAiResponse, conversation);
                    conversation.addChatMessage(aiMessage);
                    chatMessageRepository.save(aiMessage);
                    conversationRepository.save(conversation);

                    chatMemory.add(conversationId, new AssistantMessage(fullAiResponse));
                    chatMemoryRepository.saveAll(conversationId, chatMemory.get(conversationId));
                });
    }

    @Transactional
    public void generateAndSetConversationTitle(Long conversationId, String firstAiResponse) {
        String titleGenerationPrompt =
                "다음 대화 내용에 적합한 대화 제목을 6단어 이내로 만들어 줘" + firstAiResponse;

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-nano")
                .build();

        Prompt titlePrompt = new Prompt(titleGenerationPrompt, options);
        String generatedTitle = Objects.requireNonNull(openAiChatModel.call(titlePrompt).getResult().getOutput().getText()).trim();

        conversationService.updateConversationTitle(conversationId, generatedTitle);
    }

//    private Conversation createNewConversation(String email) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() ->
//                        new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email)
//                );
//        Conversation conversation = new Conversation(user);
//        return conversationRepository.save(conversation);
//    }
//
//    private Conversation findExistingConversation(Long conversationId) {
//        return conversationRepository.findById(conversationId)
//                .orElseThrow(() ->
//                        new IllegalArgumentException("대화를 찾을 수 없습니다: " + conversationId)
//                );
//    }

}
