package io.notfound.counsel_back.consultation.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import io.notfound.counsel_back.consultation.dto.ChatRequest;
import io.notfound.counsel_back.consultation.dto.ChatResponse;
import io.notfound.counsel_back.consultation.entity.ChatMessage;
import io.notfound.counsel_back.consultation.entity.Consultation;
import io.notfound.counsel_back.consultation.entity.Sender;
import io.notfound.counsel_back.consultation.repository.ChatMessageRepository;
import io.notfound.counsel_back.consultation.repository.ConsultationRepository;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ChatService {

    private final UserRepository userRepository;
    private final ConsultationRepository consultationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final Client client;

    public ChatService(
            UserRepository userRepository,
            ConsultationRepository consultationRepository,
            ChatMessageRepository chatMessageRepository,
            @Value("${gemini.api.key}") String key) {
        this.userRepository = userRepository;
        this.consultationRepository = consultationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.client = Client.builder().apiKey(key).build();
    }

    @Transactional
    public ChatResponse getChatCompletion(ChatRequest request, String userEmail) {
        String userMessage = request.getMessage();

        Consultation consultation = getOrCreateConsultation(request.getConversationId(), userEmail);

        // 사용자 메시지 저장
        ChatMessage userChatMessage = saveUserMessage(consultation, userMessage);

        // AI 응답 생성 및 저장
        ChatMessage aiChatMessage = generateAndSaveAiResponse(consultation, userMessage);

        return new ChatResponse(consultation.getId(), aiChatMessage.getId(), aiChatMessage.getMessage());
    }

    private Consultation getOrCreateConsultation(Long conversationId, String userEmail) {
        if (conversationId == null) {
            return createNewConsultation(userEmail);
        }
        return findExistingConsultation(conversationId);
    }

    private Consultation createNewConsultation(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));

        Consultation consultation = new Consultation(user);
        return consultationRepository.save(consultation);
    }

    private Consultation findExistingConsultation(Long converrsationId) {
        return consultationRepository.findById(converrsationId)
                .orElseThrow(() -> new IllegalArgumentException("대화를 찾을 수 없습니다: " + converrsationId));
    }

    private ChatMessage saveUserMessage(Consultation consultation, String message) {
        ChatMessage userMessage = ChatMessage.builder()
                .consultation(consultation)
                .sender(Sender.USER)
                .message(message)
                .build();

        return chatMessageRepository.save(userMessage);
    }

    private ChatMessage generateAndSaveAiResponse(Consultation consultation, String userMessage) {
        try {
            String model = "gemini-2.5-flash-lite";
            GenerateContentResponse response = client.models.generateContent(model, userMessage, null);
            String aiMessage = response.text();

            ChatMessage aiChatMessage = ChatMessage.builder()
                    .consultation(consultation)
                    .sender(Sender.AI)
                    .message(aiMessage)
                    .build();

            return chatMessageRepository.save(aiChatMessage);

        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 응답S 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }
}