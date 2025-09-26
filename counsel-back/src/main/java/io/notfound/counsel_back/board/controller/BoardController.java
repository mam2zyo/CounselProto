package io.notfound.counsel_back.board.controller;

import io.notfound.counsel_back.board.dto.PostRequest;
import io.notfound.counsel_back.board.dto.PostResponse;
import io.notfound.counsel_back.board.dto.PostUpdateRequest;
import io.notfound.counsel_back.board.service.BoardService;
import jakarta.servlet.http.HttpServletRequest;
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

    /** 게시글 생성 (로그인 필요, 파일 업로드 지원) */
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<PostResponse> createPost(
            @ModelAttribute PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        final String email = userDetails.getUsername();
        PostResponse response = boardService.createPost(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 게시글 상세 조회 (조회수 증가 없음) */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        PostResponse response = boardService.getPost(id);
        return ResponseEntity.ok(response);
    }

    /** 유니크 조회수 증가 전용 (로그인 사용자만 카운트, 비로그인은 no-op) */
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> recordUniqueView(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        final String email = (userDetails != null) ? userDetails.getUsername() : null;
        boardService.recordUniqueView(id, email, httpRequest);
        return ResponseEntity.ok().build();
    }

    /** 게시글 목록 조회 (latest | views | comments 정렬 지원) */
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(
            @RequestParam(required = false, defaultValue = "latest") String sortBy) {
        List<PostResponse> responses = boardService.getAllPostsSortedBy(sortBy);
        return ResponseEntity.ok(responses);
    }

    /** 게시글 수정 (로그인 필요, 파일 업로드 지원) */
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @ModelAttribute PostUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        final String email = userDetails.getUsername();
        PostResponse response = boardService.updatePost(id, request, email);
        return ResponseEntity.ok(response);
    }

    /** 게시글 삭제 (로그인 필요) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        final String email = userDetails.getUsername();
        boardService.deletePost(id, email);
        return ResponseEntity.noContent().build();
    }
}
