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
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final OpenAiChatModel openAiChatModel;
    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ConversationService conversationService;

    private static final int MAX_CHAT_MEMORY_MESSAGES = 20;

    @Transactional
    public Flux<String> completeChat(ChatRequest request, String email) {

        String messageText = request.getMessage();
        boolean isNewConversation = request.getConversationId() == null;

        Conversation conversation;

        if (isNewConversation) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + email)
                    );
            conversation = new Conversation(user);
        } else {
            conversation = conversationRepository.findById(request.getConversationId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "대화를 찾을 수 없습니다: " + request.getConversationId())
                    );
            if (!conversation.getUser().getEmail().equals(email)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 대화에 접근할 권한이 없습니다.");
            }
        }

        // 사용자 메시지 저장
        ChatMessage userMessage = new ChatMessage(Sender.USER, messageText, conversation);
        conversation.addChatMessage(userMessage);

        // 새 대화인 경우, 여기서 conversation이 처음 DB에 저장됩니다.
        conversationRepository.save(conversation);
//        chatMessageRepository.save(userMessage);

        // ChatMemory 로직
        String conversationIdStr = conversation.getId().toString();
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(MAX_CHAT_MEMORY_MESSAGES)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        chatMemory.add(conversationIdStr, new UserMessage(messageText));

        // 프롬프트 및 스트리밍 로직 (기존과 거의 동일)
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(OpenAiApi.ChatModel.GPT_4_1_NANO)
                .build();
        Prompt prompt = new Prompt(chatMemory.get(conversationIdStr), options);

        Flux<String> sharedStream = openAiChatModel.stream(prompt)
                .flatMap(response -> {
                    String token = response.getResult().getOutput().getText();
                    return token != null ? Mono.just(token) : Mono.empty();
                })
                .share();

        sharedStream
                .collect(Collectors.joining(""))
                .flatMap(fullAiResponse -> {
                    if (!fullAiResponse.isEmpty()) {
                        ChatMessage aiMessage = new ChatMessage(Sender.AI, fullAiResponse, conversation);
                        conversation.addChatMessage(aiMessage);
                        chatMessageRepository.save(aiMessage);
                        conversationRepository.save(conversation); // conversation 상태 최종 저장

                        chatMemory.add(conversationIdStr, new AssistantMessage(fullAiResponse));

                        // 새 대화인 경우에만 제목 생성
                        if (isNewConversation) {
                            String firstChat = "user: " + messageText + "ai: " + fullAiResponse;
                            generateAndSetConversationTitle(conversation.getId(), firstChat);
                        }
                    }
                    return Mono.empty();
                })
                .doOnError(e -> System.err.println("DB 저장 또는 제목 생성 중 오류 발생: " + e.getMessage()))
                .subscribe();

        return sharedStream;
    }

    @Transactional
    private void generateAndSetConversationTitle(Long conversationId, String firstChat) {
        String titleGenerationPrompt = firstChat +
                "이 대화에 적합한 대화 제목을 6단어 이내로 만들어 줘";

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(OpenAiApi.ChatModel.GPT_4_1_NANO)
                .build();

        Prompt titlePrompt = new Prompt(titleGenerationPrompt, options);

        String generatedTitle = Objects.requireNonNull(openAiChatModel.call(titlePrompt).getResult().getOutput().getText()).trim();

        conversationService.updateConversationTitle(conversationId, generatedTitle);
    }

}
