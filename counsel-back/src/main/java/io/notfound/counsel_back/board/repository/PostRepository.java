package io.notfound.counsel_back.board.repository;

import io.notfound.counsel_back.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 최신순 정렬
    List<Post> findAllByOrderByCreatedAtDesc();

    // 조회수 정렬
    List<Post> findAllByOrderByViewsDesc();

    // 댓글 개수 내림차순 정렬
    List<Post> findAllByOrderByCommentCountDesc();

    @Query("SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.attachments WHERE p.id = :id")
    Optional<Post> findByIdWithAuthorAndAttachments(@Param("id") Long id);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.views = p.views + 1 WHERE p.id = :postId")
    int incrementViews(@Param("postId") Long postId);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + :delta WHERE p.id = :postId")
    int changeCommentCount(@Param("postId") Long postId, @Param("delta") int delta);
}
