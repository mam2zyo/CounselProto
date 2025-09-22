package io.notfound.counsel_back.board.dto;

import io.notfound.counsel_back.board.entity.ReportStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponseDto {
    private Long id;
    private Long postId;
    private Long reporterId;
    private String reason;
    private ReportStatus status;
}
