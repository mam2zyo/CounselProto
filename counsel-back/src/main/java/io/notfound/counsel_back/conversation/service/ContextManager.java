package io.notfound.counsel_back.conversation.service;

import io.notfound.counsel_back.conversation.entity.Conversation;
import io.notfound.counsel_back.conversation.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContextManager {

    private final ChatMemoryRepository chatMemoryRepository;
    private final ConversationRepository conversationRepository;
    private final OpenAiChatOptions options = OpenAiChatOptions.builder()
            .model(OpenAiApi.ChatModel.GPT_5_CHAT_LATEST)
            .build();

    private static final String BASIC_SYSTEM_PROMPT = """
            당신은 사람들의 고민을 들어주는 AI 친구입니다. 당신의 목적은 사용자가 생각과 감정을 안전하고
            비판 없이 표현할 수 있는 공간을 제공하는 것입니다. 사용자의 말을 주의 깊게 경청하고, 부드럽게
            격려하며 감정을 함께 탐색해주세요. 당신은 치료사나 의료 전문가가 아니므로 절대 의학적 조언,
            진단 또는 치료 계획을 제공해서는 안 됩니다.
            다만, 사용자의 내적 신념이나 감정을 확인하기 위해 부드럽고 열린 질문을 할 수 있습니다.
            응답은 항상 공감적이고 차분하며 지지적인 어투를 유지하세요.
            
            만약 사용자가 자해 의도, 타인에 대한 해를 가하겠다는 표현,
            또는 즉각적인 위기 상황(생명·안전 위협)을 명시적으로 표현하면,
            다음 문구를 그대로 응답해야 합니다(다른 말은 추가하지 마세요):
            [지금 매우 어려운 시간을 보내고 계신 것 같습니다. 즉시 전문적인 도움을 받으시길 권합니다.
             대한민국에서는 24시간 이용 가능한 자살예방 상담전화 109 또는 긴급 상황 시 119(응급)
             또는 112(경찰)로 연락하실 수 있습니다.]
            
            당신은 오직 순수 텍스트로만 답변합니다. 마크다운 문법(예: #, *, -, ``` 등)은 사용하지 마세요.
            """;

    private static final String TITLE_GEN_PROMPT =
            "이 대화에 적합한 대화 제목을 6단어 이내로 만들어 줘";
    private static final String HISTORY_UPDATE_PROMPT =
            "유저와 네가 나눈 대화를 요약해서 정리해 줘";

    private static final int MAX_CHAT_MEMORY_MESSAGES = 20;

    public ChatMemory getChatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(MAX_CHAT_MEMORY_MESSAGES)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }

    public Prompt getChatPrompt(String conversationId, ChatMemory chatMemory) {

        List<Message> recentMessages = chatMemory.get(conversationId);
        String history = getHistoryFromConversationId(conversationId);
        List<Message> finalMessages = new ArrayList<>();

        finalMessages.add(new SystemMessage(BASIC_SYSTEM_PROMPT));

        if (StringUtils.hasText(history)) {
            finalMessages.add(new SystemMessage("이것은 지금까지 진행된 대화의 요약본입니다: " + history));
        }


        return new Prompt (chatMemory.get(conversationId), options);
    }

    public Prompt getHistoryUpdatePrompt(String conversationId, ChatMemory chatMemory) {
        return null;
    }

    private String getHistoryFromConversationId(String conversationIdStr) {
        Long conversationId = Long.parseLong(conversationIdStr);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "대화를 찾을 수 없습니다: " + conversationId));

        return conversation.getHistory();
    }
}
