package io.notfound.counsel_back.board.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String content;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    public void addComment(PostComment comment) {
        this.comments.add(comment);
    }

    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
        attachment.setPost(this);
    }

    // 제목, 내용 수정 메서드 추가 (setter 대체)
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
