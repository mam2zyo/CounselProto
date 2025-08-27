package io.notfound.counsel_back.conversation.repository;

import io.notfound.counsel_back.conversation.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
}
