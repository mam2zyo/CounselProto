package io.notfound.counsel_back.board.entity;

import io.notfound.counsel_back.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    // 🔹 편의 메서드
    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
        attachment.setPost(this);
    }

    public void setUser(User user) {
        this.user = user;
        if (!user.getPosts().contains(this)) {
            user.getPosts().add(this);
        }
    }
}
