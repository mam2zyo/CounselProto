package io.notfound.counsel_back.user.entity;

import io.notfound.counsel_back.board.entity.Post;
import io.notfound.counsel_back.consultation.entity.Consultation;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String userName;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consultation> consultations = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(length = 500)
    private String refreshToken;

    // 🔹 연관관계 편의 메서드
    public void addConsultation(Consultation consultation) {
        consultations.add(consultation);
        if (consultation.getUser() != this) consultation.setUser(this);
    }

    public void addPost(Post post) {
        posts.add(post);
        if (post.getUser() != this) post.setUser(this);
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
