package io.notfound.counsel_back.board.entity;

import io.notfound.counsel_back.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false) // [추가] 작성자 이름
    private String writerName;

    @Column(nullable = false) // [추가] 비밀번호 (해시값 저장)
    private String password;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    void setPost(Post post) {
        this.post = post;
    }

    @PrePersist
    public void createdAt() {
        this.createdAt = LocalDateTime.now();
    }

    // 내용 수정을 위한 메서드
    public void update(String content) {
        this.content = content;
    }
}