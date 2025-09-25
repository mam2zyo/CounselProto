package io.notfound.counsel_back.board.entity;

import io.notfound.counsel_back.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

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

    private int views;

    // ⭐️ 추가: 댓글 개수 필드
    @ColumnDefault("0")
    private int commentCount;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    @PrePersist
    public void createdAt() {
        this.createdAt = LocalDateTime.now();
        this.views = 0;
    }

    public void incrementViews() {
        this.views++;
    }

    // ⭐️ 수정: 댓글 추가 시 commentCount 증가
    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setPost(this);
        this.commentCount++;
    }

    // ⭐️ 추가: 댓글 삭제 시 commentCount 감소
    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        this.commentCount--;
    }

    public void addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
        attachment.setPost(this);
    }

    public void removeAttachment(Attachment attachment) {
        this.attachments.remove(attachment);
        attachment.setPost(null);
    }

    public List<Attachment> clearAttachments() {
        List<Attachment> removedAttachments = new ArrayList<>(this.attachments);
        this.attachments.clear();
        return removedAttachments;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}