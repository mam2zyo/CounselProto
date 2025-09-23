package io.notfound.counsel_back.board.controller;

import io.notfound.counsel_back.board.dto.PostRequest;
import io.notfound.counsel_back.board.dto.PostResponse;
import io.notfound.counsel_back.board.dto.PostUpdateRequest;
import io.notfound.counsel_back.board.service.BoardService;
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
public class BoardController {

    private final BoardService boardService;

    /** 게시글 작성 */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @RequestBody PostRequest request,
            @AuthenticationPrincipal(expression = "username") String email) {
        PostResponse response = boardService.createPost(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 게시글 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        PostResponse response = boardService.getPost(id);
        return ResponseEntity.ok(response);
    }

    /** 게시글 전체 조회 */
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort) {
        List<PostResponse> responses = boardService.getAllPosts(keyword, sort);
        return ResponseEntity.ok(responses);
    }

    /** 게시글 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal(expression = "username") String email) {
        PostResponse response = boardService.updatePost(id, request, email);
        return ResponseEntity.ok(response);
    }

    /** 게시글 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "username") String email) {
        boardService.deletePost(id, email);
        return ResponseEntity.noContent().build();
    }
}
