package io.notfound.counsel_back.board.dto;

import io.notfound.counsel_back.board.entity.ReportStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequestDto {
    private String reason; // 신고 사유
}
