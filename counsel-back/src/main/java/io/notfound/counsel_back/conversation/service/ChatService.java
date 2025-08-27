package io.notfound.counsel_back.conversation.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.notfound.counsel_back.conversation.repository.ChatMessageRepository;
import io.notfound.counsel_back.conversation.repository.ConversationRepository;
import io.notfound.counsel_back.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@Transactional
public class ChatService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final String apiKey;
    private final String model;
    private final WebClient webClient;

    public ChatService(
            UserRepository userRepository,
            ConversationRepository conversationRepository,
            ChatMessageRepository chatMessageRepository,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.model}") String model,
            WebClient.Builder webClientBuilder) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.apiKey = apiKey;
        this.model = model;
        this.webClient = webClientBuilder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models")
                .build();
    }

    public String getChatCompletion(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Invalid api key.");
        }

        // Gemini API가 요구하는 요청 본문(Body) 형식으로 변경합니다.
        Map<String, Object> body = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        try {
            JsonNode resp = webClient.post()
                    // 모델 이름과 API 키를 URL 경로와 파라미터로 전달합니다.
                    .uri("/" + model + ":generateContent?key=" + apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (resp == null) {
                return "Gemini로부터 응답을 받지 못했습니다.";
            }

            // Gemini API의 응답 JSON 구조에 맞게 경로를 수정합니다.
            JsonNode contentNode = resp.at("/candidates/0/content/parts/0/text");
            return contentNode.isMissingNode() ? "응답 내용이 비어있습니다." : contentNode.asText();

        } catch (Exception e) {
            // 에러 발생 시 원인을 파악하기 쉽도록 예외를 던집니다.
            throw new RuntimeException("Gemini API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
