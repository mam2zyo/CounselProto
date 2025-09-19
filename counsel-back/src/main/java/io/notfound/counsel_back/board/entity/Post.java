package io.notfound.counsel_back.board.entity;

import io.notfound.counsel_back.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    private long views = 0; // 조회수 필드 추가

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    @PrePersist
    public void createdAt() {
        this.createdAt = LocalDateTime.now();
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setPost(this); // Comment 엔티티에도 Post를 설정
    }

    public void addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
        attachment.setPost(this);
    }

    // [추가] 첨부파일 제거를 위한 편의 메서드
    public void removeAttachment(Attachment attachment) {
        this.attachments.remove(attachment);
        attachment.setPost(null);
    }

    // [추가] 모든 첨부파일을 제거하고 S3에서도 삭제하기 위해 파일 목록을 반환하는 메서드
    public List<Attachment> clearAttachments() {
        List<Attachment> removedAttachments = new ArrayList<>(this.attachments);
        this.attachments.clear();
        return removedAttachments;
    }

    // 제목, 내용 수정 메서드 (Setter 대체)
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}