package io.notfound.counsel_back.board.controller;

import io.notfound.counsel_back.board.dto.CommentRequest;
import io.notfound.counsel_back.board.dto.CommentResponse;
import io.notfound.counsel_back.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class CommentController {

    private final CommentService commentService;

    // 댓글 생성 API (이메일을 사용하여 댓글 작성)
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 현재 로그인한 사용자의 이메일을 가져옴
        String email = userDetails.getUsername();

        // 댓글 생성
        CommentResponse response = commentService.createComment(postId, request, email);

        // 댓글 생성 후 반환 (이메일 포함)
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 특정 게시글의 모든 댓글 조회 API
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(@PathVariable Long postId) {
        // 게시글 ID에 해당하는 모든 댓글 조회
        List<CommentResponse> responses = commentService.getCommentsByPostId(postId);

        // 댓글 리스트 반환 (각 댓글의 이메일 포함)
        return ResponseEntity.ok(responses);
    }

    // 댓글 수정 API (본인 이메일만 수정 가능)
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 현재 로그인한 사용자의 이메일을 가져옴
        String email = userDetails.getUsername();

        // 댓글 수정
        CommentResponse response = commentService.updateComment(commentId, request, email);

        // 수정된 댓글 반환 (이메일 포함)
        return ResponseEntity.ok(response);
    }

    // 댓글 삭제 API (본인 이메일만 삭제 가능)
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 현재 로그인한 사용자의 이메일을 가져옴
        String email = userDetails.getUsername();

        // 댓글 삭제
        commentService.deleteComment(commentId, email);

        // 삭제 후 응답 반환
        return ResponseEntity.noContent().build();
    }
}
