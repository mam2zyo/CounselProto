package io.notfound.counsel_back.board.dto;

import io.notfound.counsel_back.board.entity.Post;
import io.notfound.counsel_back.common.util.MaskingUtil;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String authorName; // 마스킹된 작성자 이름
    private List<String> attachments;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                // 작성자 이름 마스킹 처리
                .authorName(MaskingUtil.maskName(post.getAuthor().getUserName()))
                .attachments(
                        post.getAttachments().stream()
                                .map(att -> att.getFileUrl())
                                .collect(Collectors.toList())
                )
                .build();
    }
}
