package io.notfound.counsel_back.board.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private String author; // 임시로 작성자 필드 추가, 추후 User 엔티티와 연결

    private LocalDateTime createdAt = LocalDateTime.now();

    // 단방향 관계로 변경하여 Post.java의 addComment() 메서드와 충돌 방지
    // 댓글은 게시글에만 종속되므로 단방향 매핑이 더 간결함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;
}