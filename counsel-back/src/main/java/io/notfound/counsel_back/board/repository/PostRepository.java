package io.notfound.counsel_back.board.repository;

import io.notfound.counsel_back.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // (기존) 단건 조회용 Fetch Join
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author a LEFT JOIN FETCH p.attachments WHERE p.id = :id")
    Optional<Post> findByIdWithAuthorAndAttachments(@Param("id") Long id);

    // 🌟 1. 전체 목록 조회용 Fetch Join 쿼리 추가
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author a LEFT JOIN FETCH p.attachments")
    List<Post> findAllWithAuthorAndAttachments();

    // 🌟 2. 검색 조회용 Fetch Join 쿼리 수정
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author a LEFT JOIN FETCH p.attachments WHERE p.title LIKE %:search% OR p.content LIKE %:search%")
    List<Post> searchByTitleOrContentWithAuthorAndAttachments(@Param("search") String search);

}