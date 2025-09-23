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

    @Column(nullable = false)
    private long views; // ✅ 조회수 필드 (DB 컬럼 매핑)

    private LocalDateTime createdAt;

    // ✅ 댓글
    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // ✅ 첨부파일
    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    // ✅ 신고 (Report)
    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();

    @PrePersist
    public void createdAt() {
        this.createdAt = LocalDateTime.now();
    }

    // ==================== 도메인 메서드 ====================

    // ✅ 제목, 내용 수정
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // ✅ 조회수 증가
    public void incrementViews() {
        this.views++;
    }

    // ✅ 첨부파일 추가
    public void addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
        attachment.setPost(this);
    }

    // ✅ 첨부파일 제거
    public void removeAttachment(Attachment attachment) {
        this.attachments.remove(attachment);
        attachment.setPost(null);
    }

    // ✅ 모든 첨부파일 제거 후 반환
    public List<Attachment> clearAttachments() {
        List<Attachment> removedAttachments = new ArrayList<>(this.attachments);
        this.attachments.clear();
        return removedAttachments;
    }

    // ✅ 댓글 추가
    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setPost(this);
    }

    // ✅ 신고 추가
    public void addReport(Report report) {
        this.reports.add(report);
        report.setPost(this);
    }
}
