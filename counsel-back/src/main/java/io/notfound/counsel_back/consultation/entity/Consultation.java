package io.notfound.counsel_back.consultation.entity;

import io.notfound.counsel_back.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String summary;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Consultation(User user) {
        this.user = user;
        if (user != null && !user.getConsultations().contains(this)) user.getConsultations().add(this);
    }

    public void addChatMessage(ChatMessage chatMessage) {
        chatMessages.add(chatMessage);
        if (chatMessage.getConsultation() != this) chatMessage.setConsultation(this);
    }

    public void setUser(User user) {
        this.user = user;
        if (!user.getConsultations().contains(this)) user.getConsultations().add(this);
    }
}
