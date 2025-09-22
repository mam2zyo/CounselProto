package io.notfound.counsel_back.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.notfound.counsel_back.board.entity.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.attachments WHERE p.id = :id")
    Optional<Post> findByIdWithAuthorAndAttachments(@Param("id") Long id);

    List<Post> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword);

    // 조회수 기준으로 내림차순 정렬하여 모든 게시글을 조회
    List<Post> findAllByOrderByViewsDesc();

    // 키워드 검색과 조회수 정렬을 동시에 사용
    List<Post> findByTitleContainingOrContentContainingOrderByViewsDesc(String titleKeyword, String contentKeyword);
}