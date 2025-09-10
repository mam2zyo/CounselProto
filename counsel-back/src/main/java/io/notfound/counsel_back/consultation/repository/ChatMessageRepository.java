package io.notfound.counsel_back.consultation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.notfound.counsel_back.consultation.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
