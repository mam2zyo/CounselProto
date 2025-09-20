package io.notfound.counsel_back.conversation.service;

import io.notfound.counsel_back.conversation.dto.ChatRequest;
import io.notfound.counsel_back.conversation.entity.ChatMessage;
import io.notfound.counsel_back.conversation.entity.Conversation;
import io.notfound.counsel_back.conversation.entity.Sender;
import io.notfound.counsel_back.conversation.repository.ChatMessageRepository;
import io.notfound.counsel_back.conversation.repository.ConversationRepository;
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

        boolean isFirstUserMessage = conversation.getChatMessages().isEmpty();

        // request로 받은 message를 통해 ChatMessage 객체 생성 후, conversation 객체에 추가
        // 이후 개별 레파지토리에 저장하여 영속화
        ChatMessage userMessage = new ChatMessage(Sender.USER, messageText, conversation);
        conversation.addChatMessage(userMessage);
        chatMessageRepository.save(userMessage);
        conversationRepository.save(conversation);

        // ChatMemory에 userMessage를 저장
        String conversationId = conversationIdLong.toString();

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        chatMemory.add(conversationId, new UserMessage(messageText));

        // 프롬프트 호출을 위한 옵션 설정
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-nano")
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(chatMemory.get(conversationId), options);
        
        // ai 모델에 .stream() 호출 후 컨터롤러 측과 DB 양쪽이 응답을 수신할 수 있도록 .share() 호출
        Flux<String> sharedStream = openAiChatModel.stream(prompt)
                .flatMap(response -> {
                    String token = response.getResult().getOutput().getText();
                    // token이 null이 아니면 Mono.just로 감싸서 반환, null이면 Mono.empty()를 반환하여 필터링
                    return token != null ? Mono.just(token) : Mono.empty();
                })
                .share();
        
        // DB 쓰기 쪽 - 스트림을 모두 수신 후 ChatMessage, Conversation, ChatMemory에 각각 저장
        sharedStream
                .collect(Collectors.joining(""))
                .flatMap(fullAiResponse -> {                    
                    if (!fullAiResponse.isEmpty()) {
                        ChatMessage aiMessage = new ChatMessage(Sender.AI, fullAiResponse, conversation);
                        conversation.addChatMessage(aiMessage);
                        chatMessageRepository.save(aiMessage);
                        conversationRepository.save(conversation);

                        chatMemory.add(conversationId, new AssistantMessage(fullAiResponse));
                        chatMemoryRepository.saveAll(conversationId, chatMemory.get(conversationId));

                        if (isFirstUserMessage) {
                            generateAndSetConversationTitle(conversation.getId(), chatMemory);
                        }
                    }
                    return Mono.empty();
                })
                .doOnError(e -> System.err.println("DB 저장 중 오류 발생: " + e.getMessage()))
                .subscribe();

        // Controller 로 전달되는 스트림 - 이후 프론트엔드에서 fetch + readableStream으로 처리
        return sharedStream;
    }

    @Transactional
    public void generateAndSetConversationTitle(Long conversationId, ChatMemory chatMemory) {
        String titleGenerationPrompt =
                "이 대화에 적합한 대화 제목을 6단어 이내로 만들어 줘";

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-nano")
                .build();

        chatMemory.add(conversationId.toString(), new SystemMessage(titleGenerationPrompt));
        Prompt titlePrompt = new Prompt(chatMemory.get(conversationId.toString()), options);

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
