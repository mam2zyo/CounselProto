package io.notfound.counsel_back.consultation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatResponse {
    private Long consultationId;
    private Long chatMessageId;
    private String aiMessage;
}
