package io.notfound.counsel_back.consultation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.notfound.counsel_back.consultation.entity.PostComment;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> { }

