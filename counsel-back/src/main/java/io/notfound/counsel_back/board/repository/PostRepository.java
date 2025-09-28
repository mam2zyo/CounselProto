package io.notfound.counsel_back.board.repository;

import io.notfound.counsel_back.board.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 단건 조회용 Fetch Join
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author a LEFT JOIN FETCH p.attachments WHERE p.id = :id")
    Optional<Post> findByIdWithAuthorAndAttachments(@Param("id") Long id);

    /**
     * 🌟 최종 수정: 검색 기능이 작동하지 않는 문제 해결 (CONCAT 및 빈 문자열 처리)
     * 검색어가 빈 문자열인 경우 전체 목록 반환, 아니면 제목/내용 검색을 수행합니다.
     */
    @Query(value =
            "SELECT p FROM Post p LEFT JOIN FETCH p.author a LEFT JOIN FETCH p.attachments " +
                    "WHERE (:search = '' " +
                    "OR p.title LIKE CONCAT('%', :search, '%') " +
                    "OR p.content LIKE CONCAT('%', :search, '%'))",

            countQuery =
                    "SELECT COUNT(p) FROM Post p " +
                            "WHERE (:search = '' " +
                            "OR p.title LIKE CONCAT('%', :search, '%') " +
                            "OR p.content LIKE CONCAT('%', :search, '%'))"
    )
    Page<Post> findPostsWithPagingAndSearch(
            @Param("search") String search,
            Pageable pageable
    );
}