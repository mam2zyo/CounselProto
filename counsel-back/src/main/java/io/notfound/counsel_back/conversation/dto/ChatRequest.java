package io.notfound.counsel_back.conversation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {

    // 새로운 대화 시작시 null, 대화 이어가기에서는 기존 Conversation id
//    private Long ConversationId;

    @NotBlank(message = "메시지를 입력해주세요.")
    private String message;
}
