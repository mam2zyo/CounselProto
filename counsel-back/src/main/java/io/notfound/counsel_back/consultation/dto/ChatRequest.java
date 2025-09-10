package io.notfound.counsel_back.consultation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {

    private Long ConversationId;

    @NotBlank(message = "메시지를 입력해주세요.")
    private String message;
}
