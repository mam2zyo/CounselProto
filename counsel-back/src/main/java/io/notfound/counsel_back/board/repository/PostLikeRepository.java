package io.notfound.counsel_back.board.repository;

import io.notfound.counsel_back.board.entity.Post;
import io.notfound.counsel_back.board.entity.PostLike;
import io.notfound.counsel_back.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    long countByPost(Post post); // 특정 게시글 좋아요 수 조회

    boolean existsByPostAndUser(Post post, User user); // 해당 유저가 해당 게시글 좋아요 했는지 확인

    void deleteByPostAndUser(Post post, User user); // 좋아요 취소

    void deleteByPost(Post post); // 게시글 삭제 시 관련 좋아요들 삭제 (옵션)
}
