package io.notfound.counsel_back.board.entity;

import io.notfound.counsel_back.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User reporter;   // 신고자

    private Long targetId;   // 대상 ID (게시글/댓글)

    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType; // POST / COMMENT

    private String reason;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;
}
