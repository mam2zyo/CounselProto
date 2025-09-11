package io.notfound.counsel_back.board.controller;

import io.notfound.counsel_back.board.dto.PostRequestDto;
import io.notfound.counsel_back.board.dto.PostResponseDto;
import io.notfound.counsel_back.board.service.BoardService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public PostResponseDto createPost(@RequestBody PostRequestDto request) {
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

}
