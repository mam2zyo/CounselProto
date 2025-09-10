package io.notfound.counsel_back.conversation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Map;

@Getter
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = @Index(name = "idx_conversation_created_at", columnList = "conversation_id, createdAt"))
public class ChatMessage implements Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @NotNull
    @Override
    public MessageType getMessageType() { return type; }

    @Override
    public String getText() { return text; }

    @Override
    public Map<String, Object> getMetadata() {
        return Map.of();
    }
}
