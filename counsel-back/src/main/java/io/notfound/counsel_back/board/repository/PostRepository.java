package io.notfound.counsel_back.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.notfound.counsel_back.board.entity.Post;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
public interface PostRepository extends JpaRepository<Post, Long> {

    // 최신순 정렬
    List<Post> findAllByOrderByCreatedAtDesc();

    // 조회수 정렬
    List<Post> findAllByOrderByViewsDesc();

    // ⭐️ 추가: 댓글 개수 내림차순 정렬
    List<Post> findAllByOrderByCommentCountDesc();

    @Query("SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.attachments WHERE p.id = :id")
    Optional<Post> findByIdWithAuthorAndAttachments(Long id);
}