package io.notfound.counsel_back.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.notfound.counsel_back.board.entity.Post;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.attachments WHERE p.id = :id")
    Optional<Post> findByIdWithAuthorAndAttachments(Long id);
}
