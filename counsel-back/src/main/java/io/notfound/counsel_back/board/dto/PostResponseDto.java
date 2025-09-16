package io.notfound.counsel_back.board.dto;

import io.notfound.counsel_back.board.entity.Attachment;
import io.notfound.counsel_back.board.entity.Post;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class PostResponseDto {
    private Long postId;
    private String title;
    private String content;
    private List<String> attachmentUrls;

    public PostResponseDto(Post post) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.attachmentUrls = post.getAttachments().stream()
                .map(Attachment::getFileUrl)
                .collect(Collectors.toList());
    }
}
