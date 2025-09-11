package io.notfound.counsel_back.board.controller;

import io.notfound.counsel_back.board.dto.PostRequestDto;
import io.notfound.counsel_back.board.dto.PostResponseDto;
import io.notfound.counsel_back.board.service.BoardService;
import io.notfound.counsel_back.board.service.CommentService; // CommentService 추가
import io.notfound.counsel_back.board.dto.CommentRequestDto; // CommentRequestDto 추가
import io.notfound.counsel_back.board.dto.CommentResponseDto; // CommentResponseDto 추가

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService; // CommentService 주입

    // 게시글 CRUD API (기존 코드)
    @PostMapping(consumes = {"multipart/form-data"})
    public PostResponseDto createPost(@ModelAttribute PostRequestDto request) {
        return boardService.createPost(request);
    }

    @GetMapping("/{id}")
    public PostResponseDto getPost(@PathVariable Long id) {
        return boardService.getPost(id);
    }

    @GetMapping
    public List<PostResponseDto> getAllPosts() {
        return boardService.getAllPosts();
    }

    @PutMapping("/{id}")
    public PostResponseDto updatePost(@PathVariable Long id, @RequestBody PostRequestDto request) {
        return boardService.updatePost(id, request);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        boardService.deletePost(id);
    }
    
    // --- 댓글(Comment) CRUD API 추가 ---

    // 1. 댓글 작성 (Create)
    // POST /api/board/{postId}/comments
    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto createComment(@PathVariable Long postId, @RequestBody CommentRequestDto request) {
        return commentService.createComment(postId, request);
    }

    // 2. 특정 게시글의 모든 댓글 조회 (Read All)
    // GET /api/board/{postId}/comments
    @GetMapping("/{postId}/comments")
    public List<CommentResponseDto> getCommentsByPostId(@PathVariable Long postId) {
        return commentService.getCommentsByPostId(postId);
    }

    // 3. 댓글 수정 (Update)
    // PUT /api/board/comments/{commentId}
    @PutMapping("/comments/{commentId}")
    public CommentResponseDto updateComment(@PathVariable Long commentId, @RequestBody CommentRequestDto request) {
        return commentService.updateComment(commentId, request);
    }

    // 4. 댓글 삭제 (Delete)
    // DELETE /api/board/comments/{commentId}
    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
    }
}