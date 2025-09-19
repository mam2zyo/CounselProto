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

    // N+1 문제를 해결하기 위해 fetch join을 사용한 쿼리
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.attachments WHERE p.id = :id")
    Optional<Post> findByIdWithAuthorAndAttachments(@Param("id") Long id);

    // 제목 또는 내용에 키워드가 포함된 게시글을 찾는 메서드
    List<Post> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword);

}