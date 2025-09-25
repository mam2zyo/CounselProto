package io.notfound.counsel_back.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.notfound.counsel_back.board.entity.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.attachments WHERE p.id = :id")
    Optional<Post> findByIdWithAuthorAndAttachments(@Param("id")Long id);

    // [추가] 모든 게시글을 조회할 떄 N+1 문제를 방지하기 위해 Fetch Join 사용
    @Query("SELEECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.attachments")
    List<Post> findAllWithAuthorAndAttachments();

    // [추가] 제목 또는 내용에 검색어가 포함된 게시글을 찾는 메서드 (N+1 문제 방지)
    @Query("SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.attachments WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    List<Post> findByTitleOrContentContainingWithAuthorAndAttachments(@Param("keyword") String keyword);
}