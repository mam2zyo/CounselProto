package io.notfound.counsel_back.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.notfound.counsel_back.board.entity.PostComment;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> { }

