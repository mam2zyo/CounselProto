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

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<PostResponse> createPost(
            @ModelAttribute PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        PostResponse response = boardService.createPost(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 🌟 추가: 게시글 전체 목록 조회 API (GET /api/board)
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        // searchPosts 메서드는 search 파라미터가 null일 때 전체 목록을 반환하도록 되어 있습니다.
        List<PostResponse> responses = boardService.searchPosts(null);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        PostResponse response = boardService.getPost(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 검색 API
     * GET /api/board/search?search=키워드
     * @param search 검색 키워드 (제목 또는 내용)
     * @return 검색 결과 (PostResponse 리스트)
     */
    @GetMapping("/search")
    public ResponseEntity<List<PostResponse>> searchPosts(
            @RequestParam(required = false) String search) { // required = false로 검색어 없이도 전체 조회가 가능하게 설정
        List<PostResponse> responses = boardService.searchPosts(search);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @ModelAttribute PostUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        PostResponse response = boardService.updatePost(id, request, email);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        boardService.deletePost(id, email);
        return ResponseEntity.noContent().build();
    }
}