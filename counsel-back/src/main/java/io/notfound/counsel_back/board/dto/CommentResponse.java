package io.notfound.counsel_back.board.dto;

import io.notfound.counsel_back.board.entity.Comment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentResponse {
    private Long id;
    private String content;
    private String writerEmail; // 이름 대신 이메일 필드로 수정
    private LocalDateTime createdAt;
    private Long postId;

    // 정적 팩토리 메서드
    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .writerEmail(comment.getWriter().getEmail()) // 이름 대신 이메일로 수정
                .createdAt(comment.getCreatedAt())
                .postId(comment.getPost().getId())
                .build();
    }
}
