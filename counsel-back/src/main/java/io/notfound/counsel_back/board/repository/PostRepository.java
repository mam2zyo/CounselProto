package io.notfound.counsel_back.board.repository;

import io.notfound.counsel_back.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.attachments WHERE p.id = :id")
    Optional<Post> findByIdWithAuthorAndAttachments(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.attachments")
    List<Post> findAllWithAuthorAndAttachments();

    // 💡 [최종 수정] p.content 검색 조건을 제거하여 CLOB 오류 회피
    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.attachments " +
            "WHERE LOWER(p.title) LIKE :searchKeyword") // content 검색 조건 제거!
    List<Post> findByKeywordWithAuthorAndAttachments(@Param("searchKeyword") String searchKeyword);
}
