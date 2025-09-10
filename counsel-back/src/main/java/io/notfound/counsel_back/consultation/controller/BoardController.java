package io.notfound.counsel_back.consultation.controller;

import io.notfound.counsel_back.consultation.service.BoardService;
import io.notfound.counsel_back.consultation.dto.PostRequestDto;
import io.notfound.counsel_back.consultation.dto.PostResponseDto;
import lombok.RequiredArgsConstructor;
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
}
