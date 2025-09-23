package io.notfound.counsel_back.board.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostUpdateRequest {
    private String title;
    private String content;
    private List<String> deletedAttachmentUrls; // 삭제할 기존 파일 URL 목록
}