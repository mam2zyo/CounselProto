package io.notfound.counsel_back.board.entity;

import io.notfound.counsel_back.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private LocalDateTime createdAt;

    // 댓글 작성자 (회원)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private User writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    // 🔹 신고 목록
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();

    void setPost(Post post) {
        this.post = post;
    }

    // 🔹 신고 추가 메서드
    public void addReport(Report report) {
        reports.add(report);
        report.setComment(this); // Comment와 Report 연결
    }

    @PrePersist
    public void createdAt() {
        this.createdAt = LocalDateTime.now();
    }

    // 내용 수정을 위한 메서드
    public void update(String content) {
        this.content = content;
    }

    public void setWriter(User writer) {
        this.writer = writer;
    }
}
