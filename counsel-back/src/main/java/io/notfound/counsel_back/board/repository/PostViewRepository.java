package io.notfound.counsel_back.board.repository;

import io.notfound.counsel_back.board.entity.PostView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostViewRepository extends JpaRepository<PostView, Long> {

    boolean existsByPostIdAndViewerUser_Id(Long postId, Long viewerUserId);

    boolean existsByPostIdAndFingerprint(Long postId, String fingerprint);

    // ✅ 추가: 해당 글의 뷰 기록 일괄 삭제
    void deleteByPostId(Long postId);
}
